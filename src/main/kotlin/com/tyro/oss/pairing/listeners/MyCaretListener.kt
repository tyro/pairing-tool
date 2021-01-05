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
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.tyro.oss.pairing.handler.CursorChangeEvent


class MyCaretListener(
    private val cursorChangedCallback: (CursorChangeEvent) -> Unit
) : CaretListener {
    companion object {
        private val LOG = Logger.getInstance(MyCaretListener::class.java)
    }

    override fun caretPositionChanged(event: CaretEvent) {
        val editor = event.editor
        try {
            val document = editor.document
            editor.project?.let{ project ->
                FileDocumentManager.getInstance().getFile(document)?.let {
                    val projectFilePrefix = "file://" + project.basePath.toString()
                    if (it.url.startsWith(projectFilePrefix)) {
                        cursorChangedCallback(
                            CursorChangeEvent(
                                "",
                                event.newPosition,
                                it.url.replace("file://" + project.basePath.toString(), ""),
                                project.name
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            LOG.error(e)
        }
    }
}
