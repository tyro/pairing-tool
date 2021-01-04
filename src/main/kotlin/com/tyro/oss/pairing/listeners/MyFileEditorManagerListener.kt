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
package com.tyro.oss.pairing.listeners

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileEditor.FileEditorManagerEvent
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.Project
import com.tyro.oss.pairing.handler.FileNavigationEvent

class MyFileEditorManagerListener(
    private val fileNavigationCallback: (FileNavigationEvent) -> Unit,
    private val project: Project
) : FileEditorManagerListener {
    companion object {
        private val LOG = Logger.getInstance(MyFileEditorManagerListener::class.java)
    }

    override fun selectionChanged(event: FileEditorManagerEvent) {
        val fileUrl = event.newFile!!.url
        val projectFilePrefix = "file://" + project.basePath.toString()
        if (fileUrl.startsWith(projectFilePrefix)) {
            fileNavigationCallback(
                FileNavigationEvent(
                    fileUrl.replace(
                        projectFilePrefix,
                    ""
                ),
                project.name)
            )
        }
    }
}
