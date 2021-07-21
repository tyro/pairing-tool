/**
 * Copyright 2021 Tyro Payments Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tyro.oss.pairing

import com.intellij.execution.ExecutionManager
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.util.messages.MessageBus
import com.tyro.oss.pairing.PairingToolInitializer.PairingToolState
import com.tyro.oss.pairing.handler.CustomEvent
import com.tyro.oss.pairing.handler.FileSinkEvent
import com.tyro.oss.pairing.listeners.*
import com.tyro.oss.pairing.queue.Event
import com.tyro.oss.pairing.queue.FileEventQueue
import com.tyro.oss.pairing.server.ServerType
import com.tyro.oss.pairing.server.WebSocketServer
import com.tyro.oss.pairing.service.EventProcessorService
import com.tyro.oss.pairing.service.GsonFactory
import java.net.InetAddress
import java.util.*
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

@State(name = "PairingToolState", storages = [Storage("PairingToolState.xml")])
class PairingToolInitializer : PersistentStateComponent<PairingToolState> {
    companion object {
        fun getInstance(): PairingToolInitializer? =
            ApplicationManager.getApplication().getComponent(PairingToolInitializer::class.java)

        private val LOG = Logger.getInstance(PairingToolInitializer::class.java)
        var publishFileSinkEventToQueue: ((FileSinkEvent) -> Unit)? = null
    }

    data class PairingToolState(
        var serverType: ServerType = ServerType.Kafka,
        var serverUrl: String? = null,
        var workspaceName: String? = null
    )

    private var state: PairingToolState = PairingToolState()

    private var server: ServerInterface? = null

    private var firstRun = true
    private val outgoingQueue = FileEventQueue()
    private val incomingQueue = FileEventQueue()
    private val filesTouched: BlockingQueue<Pair<String, String>> = LinkedBlockingQueue()
    private lateinit var projectName: String
    lateinit var project: Project
    private lateinit var documentListener: MyDocumentListener
    private lateinit var caretListener: MyCaretListener
    private lateinit var selectionListener: MySelectionListener
    private lateinit var messageBus: MessageBus
    private val editorService = EventProcessorService()
    private val hostName = "${InetAddress.getLocalHost().hostName}_${Random().nextInt(100)}"
    private val userName = System.getProperty("user.name")
    private val eventMulticaster = EditorFactory.getInstance().eventMulticaster

    private val gson = GsonFactory.getInstance().gson

    private fun initThreads() {
        publishFileSinkEventToQueue = { event ->
            outgoingQueue.putEvent(
                Event(
                    hostName,
                    userName,
                    "ProjectSyncEvent",
                    gson.toJson(event)
                )
            )
        }

        ApplicationManager.getApplication().executeOnPooledThread {
            while (true) {
                val event = incomingQueue.getEvent()
                LOG.info("editorService.applyEvent | " + gson.toJson(event))
                editorService.applyEvent(event)
            }
        }

        ApplicationManager.getApplication().executeOnPooledThread {
            while (true) {
                if (server != null) {
                    server?.produce(outgoingQueue.getEvent())
                } else {
                    Thread.sleep(1000)
                }
            }
        }

        ApplicationManager.getApplication().executeOnPooledThread {
            while (true) {
                try {
                    server?.consume()?.let { list ->
                        LOG.info("Incoming : ${list.size}")

                        list.filter { it.originHost != hostName }
                            .forEach { incomingQueue.putEvent(it) }
                    } ?: Thread.sleep(1000)
                } catch (e: Exception) {
                    LOG.error("Something went wrong consuming from websocket. Auto recovering", e)
                }
            }
        }

        ApplicationManager.getApplication().executeOnPooledThread {
            while (true) {
                while (ApplicationManager.getApplication().idleTime < 1500) {
                    LOG.debug("ApplicationManager.getApplication().idleTime : ${ApplicationManager.getApplication().idleTime}")
                    Thread.sleep(200)
                }

                pollFileTouched()?.let {
                    val (projectName, fileUrl) = it
                    ApplicationManager.getApplication().runReadAction {
                        publishFileContents(projectName, fileUrl)
                    }
                }
            }
        }
    }

    private fun initListeners() {
        project = ProjectManager.getInstance().openProjects.filter { it.name == this.projectName }[0]
        documentListener = MyDocumentListener({
            pushEventToOutgoingQueue(it)
            notifyFileModification(it.projectName, it.file)
        }, project)
        caretListener = MyCaretListener { pushEventToOutgoingQueue(it) }
        selectionListener =
            MySelectionListener { pushEventToOutgoingQueue(it) }
        messageBus = project.messageBus

        eventMulticaster.addDocumentListener(documentListener, project)
        eventMulticaster.removeDocumentListener(documentListener)
        eventMulticaster.addDocumentListener(documentListener, project)
        eventMulticaster.addCaretListener(caretListener, project)
        eventMulticaster.addSelectionListener(selectionListener, project)
        messageBus.connect().subscribe(
            FileEditorManagerListener.FILE_EDITOR_MANAGER,
            MyFileEditorManagerListener(
                { pushEventToOutgoingQueue(it) },
                project
            )
        )
        messageBus.connect().subscribe(
            VirtualFileManager.VFS_CHANGES,
            MyBulkFileListener(
                { pushEventToOutgoingQueue(it) },
                project
            )
        )
        messageBus.connect().subscribe(
            ExecutionManager.EXECUTION_TOPIC,
            MyExecutionListener { pushEventToOutgoingQueue(it) })
    }

    private fun pushEventToOutgoingQueue(it: CustomEvent) {
        outgoingQueue.putEvent(Event(hostName, userName, it::class.java.name.substringAfterLast('.'), gson.toJson(it)))
    }

    private fun destroyListeners() {
        EditorFactory.getInstance().eventMulticaster.removeDocumentListener(documentListener)
        EditorFactory.getInstance().eventMulticaster.removeCaretListener(caretListener)
        EditorFactory.getInstance().eventMulticaster.removeSelectionListener(selectionListener)
        messageBus.connect().disconnect()
    }

    private fun notifyFileModification(
        projectName: String,
        fileName: String
    ) {
        if (!filesTouched.contains(projectName to fileName)) {
            synchronized(filesTouched) {
                if (!filesTouched.contains(projectName to fileName)) {
                    filesTouched.offer(projectName to fileName)
                }
            }
        }
    }

    fun startPairing() {
        when (this.state.serverType) {
            ServerType.Kafka -> {
                this.state.serverUrl?.let { kafkaUrl ->
                    this.state.workspaceName?.let { workspaceName ->
                        this.server = KafkaClient(kafkaUrl, workspaceName)
                    }
                } ?: LOG.warn("Not enough information to start. ${ServerType.Kafka} ${gson.toJson(this.state)}")
            }
            ServerType.WebSocket -> {
                this.state.serverUrl?.let { kafkaUrl ->
                    this.state.workspaceName?.let { workspaceName ->
                        this.server = WebSocketServer(kafkaUrl, workspaceName)
                    }
                } ?: LOG.warn("Not enough information to start. ${ServerType.Kafka} ${gson.toJson(this.state)}")
            }
        }

        initListeners()
        if (firstRun) {
            initThreads()
        }
        incomingQueue.clear()
        outgoingQueue.clear()
        incomingQueue.open()
        outgoingQueue.open()
        firstRun = false
    }

    fun stopPairing() {
        server?.close()
        server = null
        incomingQueue.clear()
        outgoingQueue.clear()
        incomingQueue.close()
        outgoingQueue.close()
        destroyListeners()
    }

    fun getKafkaUrl() = this.state.serverUrl

    fun setKafkaUrl(kafkaUrl: String) {
        this.state.serverUrl = kafkaUrl
    }

    fun getServerType() = this.state.serverType

    fun setServerType(serverType: ServerType) {
        this.state.serverType = serverType
    }

    fun getWorkspaceName() = this.state.workspaceName

    fun setWorkspaceName(workspaceName: String) {
        this.state.workspaceName = workspaceName
    }

    fun isPairing(): Boolean = (server != null)

    fun putOutgoingQueueEvent(event: Event) = outgoingQueue.putEvent(event)

    fun pollFileTouched(): Pair<String, String>? = filesTouched.poll(5, TimeUnit.SECONDS)

    override fun getState(): PairingToolState = this.state

    override fun loadState(state: PairingToolState) {
        this.state = state
    }

    fun setProject(name: String) {
        this.projectName = name
    }

    private fun publishFileContents(
        projectName: String,
        fileUrl: String
    ) {
        try {
            val fileContents = getFileContents(projectName, fileUrl)
            if (fileContents.isNotEmpty()) {
                putOutgoingQueueEvent(
                    Event(
                        hostName,
                        userName,
                        "FileSinkEvent",
                        GsonFactory.getInstance().gson.toJson(
                            FileSinkEvent(
                                fileUrl,
                                fileContents,
                                projectName
                            )
                        )
                    )
                )
            } else {
                LOG.warn("Either failed to find the file or it was empty $fileUrl")
            }
        } catch (e: Exception) {
            LOG.error(e)
        }
    }

    private fun getFileContents(projectName: String, fileUrl: String): String {
        val project = ProjectManager.getInstance().openProjects.find { it.name == projectName }!!

        return VirtualFileManager.getInstance().findFileByUrl("file://${project.basePath}$fileUrl")?.let {
            FileDocumentManager.getInstance().getDocument(it)?.text ?: ""
        } ?: ""
    }

    fun openOutgoing() {
        outgoingQueue.open()
    }

    fun closeOutgoing() {
        outgoingQueue.close()
    }
}
