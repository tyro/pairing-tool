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

import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.WindowManager
import com.intellij.ui.awt.RelativePoint
import com.tyro.oss.pairing.PairingToolInitializer
import java.util.*

class NotificationBalloonService {

    private lateinit var statusBar: StatusBar
    var balloon: Balloon? = null
    var timer: Timer = Timer()

    fun displayBalloonNotification(userName: String, timeout: Long) {
        statusBar = WindowManager.getInstance()
            .getStatusBar(PairingToolInitializer.getInstance()!!.project)

        if (balloon == null || (balloon != null && balloon!!.isDisposed)) {
            balloon = JBPopupFactory.getInstance()
                .createHtmlTextBalloonBuilder("$userName is active", MessageType.INFO, null)
                .createBalloon()
            balloon!!
                .show(
                    RelativePoint.getCenterOf(statusBar.component),
                    Balloon.Position.atRight
                )
        }
        timer.cancel()
        timer = Timer()
        timer.schedule(HideBalloon(balloon!!), timeout)
    }

    class HideBalloon(balloon: Balloon) : TimerTask() {
        private val localBalloon = balloon
        override fun run() {
            localBalloon.hide()
        }
    }
}
