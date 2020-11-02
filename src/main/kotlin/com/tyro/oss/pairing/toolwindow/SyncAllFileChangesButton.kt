/**
 * Copyright 2020 Tyro Payments Limited
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
import com.tyro.oss.pairing.menu.SyncLocalChanges
import javax.swing.Icon

class SyncAllFileChangesButton : AnActionButton(ICON_TEXT, ICON) {
    companion object {
        const val ICON_TEXT = "Push all local changes"
        val ICON: Icon = AllIcons.Actions.Upload
    }

    override fun actionPerformed(e: AnActionEvent) {
        ActionManager.getInstance()
            .getActionOrStub(SyncLocalChanges.PUSH_LOCAL_CHANGES_ID)?.actionPerformed(e)
    }

    override fun updateButton(e: AnActionEvent) {
        ApplicationManager.getApplication().getComponent(PairingToolInitializer::class.java).let {
            e.presentation.isEnabled = it?.isPairing() ?: false
        }
    }
}