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

import com.intellij.openapi.editor.event.SelectionEvent
import com.intellij.openapi.editor.event.SelectionListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.tyro.oss.pairing.handler.SelectionChangeEvent
import org.apache.commons.logging.LogFactory


class MySelectionListener(val selectionChangedCallback: (SelectionChangeEvent) -> Unit) : SelectionListener {
    companion object {
        private val LOG = LogFactory.getLog(MySelectionListener::class.java)
    }

    override fun selectionChanged(e: SelectionEvent) {
        try {
            val document = e.editor.document
            val project = e.editor.project!!

            FileDocumentManager.getInstance().getFile(document)?.let {
                val projectFilePrefix = "file://" + project.basePath.toString()
                if (it.url.startsWith(projectFilePrefix)) {
                    selectionChangedCallback(
                        SelectionChangeEvent(
                            e.editor.caretModel.caretsAndSelections.filter { it.selectionStart != null },

                            it.url.replace(
                                projectFilePrefix,
                                ""
                            ),
                            project.name
                        )
                    )
                }
            }
        } catch (e: Exception) {
            LOG.error(e)
        }
    }
}
