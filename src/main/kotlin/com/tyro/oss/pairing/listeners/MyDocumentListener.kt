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
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.tyro.oss.pairing.handler.DocumentChangeEvent

class MyDocumentListener(
    private val documentChangedCallback: (DocumentChangeEvent) -> Unit,
    private val project: Project
) : DocumentListener {
    companion object {
        private val LOG = Logger.getInstance(MyDocumentListener::class.java)
    }
    
    override fun documentChanged(event: DocumentEvent) {
        val document = event.document
        if (!event.isWholeTextReplaced) {
            FileDocumentManager.getInstance().getFile(document)?.url?.replace(
                "file://" + project.basePath.toString(),
                ""
             )?.let {
                documentChangedCallback(
                    DocumentChangeEvent(
                        it,
                        event.offset,
                        event.oldLength,
                        event.newFragment.toString(),
                        project.name
                    )
                )
            }
        }
    }
}
