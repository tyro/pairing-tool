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
package com.tyro.oss.pairing.queue

import com.intellij.openapi.diagnostic.Logger
import java.util.concurrent.LinkedBlockingQueue

class FileEventQueue {
    companion object {
        private val LOG = Logger.getInstance(FileEventQueue::class.java)
    }

    private val queue = LinkedBlockingQueue<Event>()
    private var open = false

    fun getEvent(): Event {
        return queue.take()
    }

    fun putEvent(event: Event) {
        if (open) {
            queue.offer(event)
        }
    }

    fun clear() {
        queue.clear()
    }

    fun open() {
        open = true
    }

    fun close() {
        open = false
    }
}

data class Event(
    val originHost : String,
    val type: String,
    val payload: String
)
