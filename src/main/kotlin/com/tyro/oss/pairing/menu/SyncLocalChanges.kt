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
package com.tyro.oss.pairing.menu

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.vcs.changes.ChangeListManager
import com.tyro.oss.pairing.PairingToolInitializer
import com.tyro.oss.pairing.handler.FileSinkEvent

class SyncLocalChanges : AnAction("Sync _Local Changes") {

    companion object {
        const val PUSH_LOCAL_CHANGES_ID = "Myplugin.SyncLocalChanges"
    }
    override fun actionPerformed(e: AnActionEvent) {
        e.project?.let { project ->
            ChangeListManager.getInstance(project).affectedFiles.forEach {
                val fileSinkEvent = FileSinkEvent(
                    it.url.replace(
                        "file://${project.basePath}",
                        ""
                    ),
                    String(it.contentsToByteArray()),
                    project.name
                )

                PairingToolInitializer.publishFileSinkEventToQueue?.let { func -> func(fileSinkEvent) }
            }
        }
    }
}