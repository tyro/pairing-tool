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
package com.tyro.oss.pairing.handler

import com.intellij.openapi.editor.CaretState
import com.intellij.openapi.editor.LogicalPosition

interface CustomEvent

data class EditorChangeEvent(
    val actionName: String,
    val change: String,
    val offset: LogicalPosition,
    val file: String,
    val projectName: String
) : CustomEvent

data class DocumentChangeEvent(
    val file: String,
    val offset: Int,
    val oldStringLength: Int,
    val newString: String,
    val projectName: String
) : CustomEvent

data class CursorChangeEvent(
    val actionName: String,
    val offset: LogicalPosition,
    val file: String,
    val projectName: String
) : CustomEvent

data class SelectionChangeEvent(
    val selections: List<CaretState>,
    val file: String,
    val projectName: String
) : CustomEvent

data class FileSinkEvent(
    val file : String,
    val fileContent: String,
    val projectName: String
) : CustomEvent

data class FileNavigationEvent(
    val file: String,
    val projectName: String
) : CustomEvent

interface FileOperationEvent : CustomEvent {
    val projectName: String
    val file: String
}

data class FileMoveEvent(
    override val projectName: String,
    override val file: String,
    val oldParent: String,
    val newParent: String): FileOperationEvent

data class FileCopyEvent(
    override val projectName: String,
    override val file: String,
    val newParent: String,
    val newName: String): FileOperationEvent

data class FileDeleteEvent(
    override val projectName: String,
    override val file: String): FileOperationEvent

data class FileRenameEvent(
    override val projectName: String,
    override val file: String,
    val newName: String): FileOperationEvent

data class ExecutionOperationEvent(
    val projectName: String,
    val configurationName: String
) : CustomEvent