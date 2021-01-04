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
import com.intellij.openapi.wm.WindowManager
import com.tyro.oss.pairing.PairingToolInitializer

// If you register the action from Java code, this constructor is used to set the menu item name
// (optionally, you can specify the menu description and an icon to display next to the menu item).
// You can omit this constructor when registering the action in the plugin.xml file.
class StopPairing : AnAction("Stop _Pairing") {

    companion object {
        const val STOP_PAIRING_ID = "Myplugin.StopPairing"
    }

    override fun actionPerformed(event: AnActionEvent) {
        event.getData(PlatformDataKeys.PROJECT)?.let { project ->


            PairingToolInitializer.getInstance()?.stopPairing()

            Notifications.Bus.notify(
                Notification(
                    "MyPlugin.Stopped",
                    "Pairing Stopped",
                    "Successfully disconnected from pairing workspace!",
                    NotificationType.INFORMATION
                )
            )
            WindowManager.getInstance().getStatusBar(project).info = "You are not connected to a pairing workspace"
        }
    }
}
