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
package com.tyro.oss.pairing.menu

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.ui.Messages
import com.tyro.oss.pairing.PairingToolInitializer
import java.util.*


// If you register the action from Java code, this constructor is used to set the menu item name
// (optionally, you can specify the menu description and an icon to display next to the menu item).
// You can omit this constructor when registering the action in the plugin.xml file.
class StartPairing : AnAction("Start _Pairing") {

    companion object {
        private val LOG = Logger.getInstance(StartPairing::class.java)
        const val START_PARING_ID = "Myplugin.StartPairing"
    }

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.getData(PlatformDataKeys.PROJECT)
        val machineName = PairingToolInitializer.getInstance()?.getKafkaUrl() ?: Messages.showInputDialog(
            project,
            "Please enter machine name",
            "Input machine name",
            Messages.getQuestionIcon(),
            null,
            null
        )

        val sessionId = UUID.randomUUID().toString()
        val workspaceName = PairingToolInitializer.getInstance()?.getWorkspaceName() ?: Messages.showInputDialog(
            project, "Please enter workspace name", "Input workspace name", Messages.getQuestionIcon(),
            sessionId, null
        )
        var error = false
        try {
            PairingToolInitializer.getInstance()?.let {
                it.setKafkaUrl(machineName!!)
                it.setWorkspaceName(workspaceName!!)
                it.setProject(project!!.name)
                it.startPairing()
            }
        } catch (e: Exception) {
            LOG.error(e)
            Notifications.Bus.notify(
                Notification(
                    "MyPlugin.Failure",
                    "Pairing Failed",
                    "Unable to connect to $machineName on workspace: $workspaceName",
                    NotificationType.WARNING
                )
            )
            error = true
        }
        if (!error) {

            Notifications.Bus.notify(
                Notification(
                    "MyPlugin.Success",
                    "You are now pairing",
                    "Successfully connected to $machineName  on workspace: $workspaceName!",
                    NotificationType.INFORMATION
                )
            )
            LOG.info("You are connected to  $machineName  on workspace: $workspaceName!")
        }
    }
}
