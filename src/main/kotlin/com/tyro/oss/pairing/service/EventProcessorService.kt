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
package com.tyro.oss.pairing.service

import com.google.gson.Gson
import com.intellij.execution.ProgramRunnerUtil
import com.intellij.execution.RunManager
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.editor.impl.DocumentImpl
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.util.LocalTimeCounter
import com.intellij.util.io.exists
import com.tyro.oss.pairing.handler.*
import com.tyro.oss.pairing.queue.Event
import com.tyro.oss.pairing.queue.PingPongBlocker
import java.nio.file.Paths

class EventProcessorService {
    companion object {
        private val LOG = Logger.getInstance(EventProcessorService::class.java)
        private const val TIMEOUT_IN_MILLIS = 3000L
    }

    private val gson: Gson = Gson()
    private val pingPongBlocker = PingPongBlocker()
    private val notificationBalloonService = NotificationBalloonService()

    fun applyEvent(event: Event) {
        pingPongBlocker.closeOutgoingQueueTemporarily(TIMEOUT_IN_MILLIS)
        notificationBalloonService.displayBalloonNotification(event.originHost, TIMEOUT_IN_MILLIS)
        try {
            when (event.type) {
                "CursorChangeEvent" -> {
                    processCursorEvent(gson.fromJson(event.payload, CursorChangeEvent::class.java))
                }
                "EditorChangeEvent" -> {
                    processKeyPressedEvent(gson.fromJson(event.payload, EditorChangeEvent::class.java))
                }
                "DocumentChangeEvent" -> {
                    processDocumentChangeEvent(gson.fromJson(event.payload, DocumentChangeEvent::class.java))
                }
                "SelectionChangeEvent" -> {
                    processSelectionEvent(gson.fromJson(event.payload, SelectionChangeEvent::class.java))
                }
                "FileSinkEvent" -> {
                    processFileSyncEvent(gson.fromJson(event.payload, FileSinkEvent::class.java))
                }
                "ProjectSyncEvent" -> {
                    processProjectSyncEvent(gson.fromJson(event.payload, FileSinkEvent::class.java))
                }
                "FileNavigationEvent" -> {
                    processFileNavigationEvent(gson.fromJson(event.payload, FileNavigationEvent::class.java))
                }
                "FileMoveEvent" -> {
                    processFileOperationEvent(gson.fromJson(event.payload, FileMoveEvent::class.java))
                }
                "FileCopyEvent" -> {
                    processFileOperationEvent(gson.fromJson(event.payload, FileCopyEvent::class.java))
                }
                "FileDeleteEvent" -> {
                    processFileOperationEvent(gson.fromJson(event.payload, FileDeleteEvent::class.java))
                }
                "FileRenameEvent" -> {
                    processFileOperationEvent(gson.fromJson(event.payload, FileRenameEvent::class.java))
                }
                "ExecutionOperationEvent" -> {
                    processExecutionOperationEvent(gson.fromJson(event.payload, ExecutionOperationEvent::class.java))
                }
            }
        } catch (t: Throwable) {
            LOG.error(t)
        }
    }

    private fun processSelectionEvent(selectionChangeEvent: SelectionChangeEvent) {
        ProjectManager.getInstance().openProjects.find { it.name == selectionChangeEvent.projectName }?.let { project ->
            if (!Paths.get(project.basePath + selectionChangeEvent.file).exists()) return

            WriteCommandAction.runWriteCommandAction(project) {
                val fileEditorManager = FileEditorManager.getInstance(project)
                fileEditorManager.openFile(
                    VirtualFileManager.getInstance().refreshAndFindFileByUrl("file://" + project.basePath + selectionChangeEvent.file)!!,
                    true
                )

                val editor = fileEditorManager.selectedTextEditor!!

                editor.caretModel.caretsAndSelections = selectionChangeEvent.selections
                editor.scrollingModel.scrollToCaret(ScrollType.CENTER)
            }
        } ?: LOG.warn("Could not find ${selectionChangeEvent.projectName}")
    }

    private fun processFileSyncEvent(fileSinkEvent: FileSinkEvent) {
        ProjectManager.getInstance().openProjects.find { it.name == fileSinkEvent.projectName }?.let { project ->
            if (!tryEnsureFileExists(project.basePath + fileSinkEvent.file)) return

            fileSinkEvent.file.toVirtualFile(project)?.let { virtualFile ->
                WriteCommandAction.runWriteCommandAction(project) {
                    val fileEditorManager = FileEditorManager.getInstance(project)
                    fileEditorManager.openFile(
                        virtualFile,
                        true
                    )
                    fileEditorManager.selectedTextEditor?.document?.setText(fileSinkEvent.fileContent)
                }
            }
        } ?: LOG.warn("Could not find ${fileSinkEvent.projectName}")
    }

    private fun processProjectSyncEvent(fileSinkEvent: FileSinkEvent) {
        ProjectManager.getInstance().openProjects.find { it.name == fileSinkEvent.projectName }
            ?.let { project ->
                if (!tryEnsureFileExists(project.basePath + fileSinkEvent.file)) return

                WriteCommandAction.runWriteCommandAction(project) {
                    fileSinkEvent.file.toVirtualFile(project)
                        ?.setBinaryContent(fileSinkEvent.fileContent.toByteArray())
                }
            }
            ?: LOG.warn("Could not find ${fileSinkEvent.projectName}")
    }

    private fun processCursorEvent(changeEvent: CursorChangeEvent) {
        ProjectManager.getInstance().openProjects.find { it.name == changeEvent.projectName }?.let { project ->
            if (!Paths.get(project.basePath + changeEvent.file).exists()) return

            WriteCommandAction.runWriteCommandAction(project) {
                val fileEditorManager = FileEditorManager.getInstance(project)
                fileEditorManager.openFile(
                    VirtualFileManager.getInstance().refreshAndFindFileByUrl("file://" + project.basePath + changeEvent.file)!!,
                    true
                )

                fileEditorManager.selectedTextEditor?.let { editor ->
                    editor.caretModel.currentCaret.moveToLogicalPosition(changeEvent.offset)
                    editor.scrollingModel.scrollToCaret(ScrollType.CENTER)
                }
            }
        } ?: LOG.warn("Could not find ${changeEvent.projectName}")
    }

    private fun processDocumentChangeEvent(changeEvent: DocumentChangeEvent) {
        ProjectManager.getInstance().openProjects.find { it.name == changeEvent.projectName }?.let { project ->
            if (!tryEnsureFileExists(project.basePath + changeEvent.file)) return

            WriteCommandAction.runWriteCommandAction(project) {

                val fileEditorManager = FileEditorManager.getInstance(project)
                changeEvent.file.toVirtualFile(project)?.let { virtualFile ->
                    fileEditorManager.openFile(virtualFile, true)

                    val editor = fileEditorManager.selectedTextEditor!!

                    editor.caretModel.currentCaret.moveToOffset(changeEvent.offset)
                    val document = editor.document as DocumentImpl
                    document.replaceString(
                        changeEvent.offset,
                        changeEvent.offset + changeEvent.oldStringLength,
                        changeEvent.newString
                    )
                    editor.caretModel.currentCaret.moveToOffset(changeEvent.offset + changeEvent.newString.length)
                } ?: LOG.info("Could not find fine ${changeEvent.file}")
            }
        } ?: LOG.warn("Could not find ${changeEvent.projectName}")
    }

    private fun processKeyPressedEvent(changeEvent: EditorChangeEvent) {
        ProjectManager.getInstance().openProjects.find { it.name == changeEvent.projectName }?.let { project ->
            if (!Paths.get(project.basePath + changeEvent.file).exists()) return

            WriteCommandAction.runWriteCommandAction(project) {

                val fileEditorManager = FileEditorManager.getInstance(project)
                changeEvent.file.toVirtualFile(project)?.let { virtualFile ->
                    fileEditorManager.openFile(virtualFile, true)

                    val editor = fileEditorManager.selectedTextEditor!!

                    editor.caretModel.currentCaret.moveToLogicalPosition(changeEvent.offset)
                    editor.document.replaceString(
                        editor.caretModel.currentCaret.visualLineStart,
                        editor.caretModel.currentCaret.visualLineEnd,
                        changeEvent.change
                    )
                    editor.caretModel.currentCaret.moveToLogicalPosition(changeEvent.offset)
                } ?: LOG.info("Could not find fine ${changeEvent.file}")
            }
        } ?: LOG.warn("Could not find ${changeEvent.projectName}")
    }

    private fun processFileNavigationEvent(event: FileNavigationEvent) {
        ProjectManager.getInstance().openProjects.find { it.name == event.projectName }?.let { project ->
            if (!Paths.get(project.basePath + event.file).exists()) return

            WriteCommandAction.runWriteCommandAction(project) {
                val fileEditorManager = FileEditorManager.getInstance(project)
                fileEditorManager.openFile(
                    VirtualFileManager.getInstance()
                        .refreshAndFindFileByUrl("file://" + project.basePath + event.file)!!,
                    true
                )
            }
        } ?: LOG.warn("Could not find ${event.projectName}")
    }

    private fun processFileOperationEvent(event: FileOperationEvent) {
        ProjectManager.getInstance().openProjects.find { it.name == event.projectName }?.let { project ->
            if (!Paths.get(project.basePath + event.file).exists()) return
            WriteCommandAction.runWriteCommandAction(project) {
                when (event) {
                    is FileMoveEvent -> event.file.toVirtualFile(project)?.move(
                        this,
                        event.newParent.toVirtualFile(project)!!
                    )
                    is FileCopyEvent -> event.file.toVirtualFile(project)?.copy(
                        this,
                        event.newParent.toVirtualFile(project)!!,
                        event.newName
                    )
                    is FileDeleteEvent -> event.file.toVirtualFile(project)?.delete(this)
                    is FileRenameEvent -> event.file.toVirtualFile(project)?.rename(this, event.newName)
                }
            }
        } ?: LOG.warn("Could not find ${event.projectName}")
    }

    private fun processExecutionOperationEvent(event: ExecutionOperationEvent) {
        ProjectManager.getInstance().openProjects.find { it.name == event.projectName }?.let { project ->
            val config = RunManager.getInstance(project)
                .allSettings.last { it.name == event.configurationName }
            ApplicationManager.getApplication().invokeLater {
                ProgramRunnerUtil.executeConfiguration(config, DefaultRunExecutor.getRunExecutorInstance())
            }
        }
    }

    private fun String.toVirtualFile(project: Project): VirtualFile? =
        VirtualFileManager.getInstance().refreshAndFindFileByUrl("file://" + project.basePath + this)

    private fun tryEnsureFileExists(path: String): Boolean {
        val file = Paths.get(path)
        if (file.toString().contains("file:/")) return false // Probably a scratch file (as they are outside of the project).
        if (file.toString().contains("mock:/")) return false // Probably a commit window file.
        if (file.exists()) return true

        if (!file.parent.exists()) file.parent.toFile().mkdirs()

        return file.toFile().createNewFile()
    }
}
