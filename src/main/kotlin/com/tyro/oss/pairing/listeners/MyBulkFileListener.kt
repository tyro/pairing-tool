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
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.*
import com.tyro.oss.pairing.handler.*

class MyBulkFileListener(
    private val fileOperationCallback: (FileOperationEvent) -> Unit,
    private val project: Project
) : BulkFileListener {
    companion object {
        private val LOG = Logger.getInstance(MyBulkFileListener::class.java)
    }

    override fun before(events: List<VFileEvent?>) {
        events.forEach {
            when(it) {
                is VFileMoveEvent -> fileOperationCallback(
                    FileMoveEvent(
                        project.name,
                        it.file.url.replace("file://" + project.basePath.toString(), ""),
                        it.oldParent.url.replace("file://" + project.basePath.toString(), ""),
                        it.newParent.url.replace("file://" + project.basePath.toString(), "")
                    )
                )
                is VFileCopyEvent -> fileOperationCallback(
                    FileCopyEvent(
                        project.name,
                        it.file.url.replace("file://" + project.basePath.toString(), ""),
                        it.newParent.url.replace("file://" + project.basePath.toString(), ""),
                        it.newChildName
                    )
                )
                is VFileDeleteEvent -> fileOperationCallback(
                    FileDeleteEvent(
                        project.name,
                        it.file.url.replace("file://" + project.basePath.toString(), "")
                    )
                )
                is VFilePropertyChangeEvent -> fileOperationCallback(
                    FileRenameEvent(
                        project.name,
                        it.file.url.replace("file://" + project.basePath.toString(), ""),
                        it.newValue.toString()
                    )
                )
            }
        }
    }

    override fun after(events: List<VFileEvent?>) {

    }
}
