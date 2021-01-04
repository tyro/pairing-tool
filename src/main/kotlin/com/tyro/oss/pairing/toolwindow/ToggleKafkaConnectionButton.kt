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

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.ui.AnActionButton
import com.tyro.oss.pairing.PairingToolInitializer
import com.tyro.oss.pairing.menu.StartPairing
import com.tyro.oss.pairing.menu.StopPairing
import javax.swing.Icon

class ToggleKafkaConnectionButton : AnActionButton(START_TEXT, START_ICON) {
    companion object {
        const val START_TEXT = "Start Pairing"
        const val STOP_TEXT = "Stop Pairing"
        val START_ICON: Icon =
            AllIcons.Actions.Execute
        val STOP_ICON: Icon =
            AllIcons.Actions.Cancel
    }
    override fun actionPerformed(e: AnActionEvent) {
        val app = ApplicationManager.getApplication().getComponent(PairingToolInitializer::class.java)
        if(app?.isPairing() == true) {
            ActionManager.getInstance().getActionOrStub(
                StopPairing.STOP_PAIRING_ID
            )?.actionPerformed(e)
        } else {
            ActionManager.getInstance().getActionOrStub(
                StartPairing.START_PARING_ID
            )?.actionPerformed(e)
        }

        updateButtonLook(app?.isPairing() ?: false, e)
    }

    override fun updateButton(e: AnActionEvent) {
        val app = ApplicationManager.getApplication().getComponent(PairingToolInitializer::class.java)
        updateButtonLook(app?.isPairing() ?: false, e)
    }

    private fun updateButtonLook(running: Boolean, e: AnActionEvent) {
        e.presentation.icon = if (running)
            STOP_ICON else
            START_ICON
        e.presentation.text = if (running)
            STOP_TEXT else
            START_TEXT
    }
}
