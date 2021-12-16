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
package com.tyro.oss.pairing.toolwindow

import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.Separator
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.CollectionComboBoxModel
import com.intellij.ui.TextFieldWithHistory
import com.intellij.ui.layout.panel
import com.tyro.oss.pairing.PairingToolInitializer
import com.tyro.oss.pairing.listeners.KeyChangeListener
import com.tyro.oss.pairing.server.ServerType
import java.awt.Dimension
import java.util.*
import javax.swing.JPanel
import javax.swing.JTextField

class PairingToolWindow {

    companion object {
        private const val DEFAULT_WIDTH = 250
    }

    fun getContent(): JPanel = SimpleToolWindowPanel(
        true
    ).apply {
        setToolbar(ActionManager.getInstance().createActionToolbar("Pairing.Toolbar", createActions(), true).component)
        setContent(mainContent())
    }

    private fun createActions(): ActionGroup =
        DefaultActionGroup().apply {
            add(ToggleKafkaConnectionButton())
            add(SyncAllFileChangesButton())
            add(Separator.create("|"))
        }

    private fun mainContent() =
        panel {
            row(" Server Type:") {
                cell {
                    comboBox(
                        CollectionComboBoxModel(
                            listOf(ServerType.Kafka, ServerType.WebSocket),
                            PairingToolInitializer.getInstance()?.getServerType() ?: ServerType.Kafka
                        )
                    ) {
                        PairingToolInitializer.getInstance()?.setServerType(it)
                    }()
                }
            }
            row(" Host Url") {
                cell {
                    defaultTextFieldWithHistory(
                        PairingToolInitializer.getInstance()?.getKafkaUrl() ?: ""
                    ) {
                        PairingToolInitializer.getInstance()?.setKafkaUrl(it)
                    }()
                }
            }
            row(" Session Name") {
                cell {
                    defaultTextFieldWithHistory(
                        PairingToolInitializer.getInstance()?.getWorkspaceName() ?: UUID.randomUUID().toString()
                    ) {
                        PairingToolInitializer.getInstance()?.setWorkspaceName(it)
                    }()
                }
            }
        }

    private fun comboBox(model: CollectionComboBoxModel<ServerType>, callback: ((text: ServerType) -> Unit)?) =
        ComboBox(model, DEFAULT_WIDTH).apply {
            preferredSize = Dimension(DEFAULT_WIDTH, preferredSize.height)
            callback?.let {
                addActionListener { event ->
                    (event.source as? ComboBox<ServerType>)?.let {
                        callback(it.item)
                    }
                }
            }
        }

    private fun defaultTextFieldWithHistory(vararg historyItems: String, callback: ((text: String) -> Unit)?) =
        TextFieldWithHistory().apply {
            history = historyItems.asList()
            preferredSize = Dimension(DEFAULT_WIDTH, preferredSize.height)
            callback?.let {
                addActionListener {
                    callback((it.source as TextFieldWithHistory).text)
                }
                addKeyboardListener(KeyChangeListener {
                    it?.let { callback((it.source as JTextField).text) }
                })
            }
        }

}
