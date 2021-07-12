package com.tyro.oss.pairing

import com.tyro.oss.pairing.queue.Event

interface ServerInterface {
    fun consume(): List<Event>
    fun produce(event: Event)
    fun close()
}
