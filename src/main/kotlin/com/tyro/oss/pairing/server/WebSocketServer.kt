package com.tyro.oss.pairing.server

import com.google.gson.Gson
import com.intellij.openapi.diagnostic.Logger
import com.tyro.oss.pairing.KafkaClient
import com.tyro.oss.pairing.ServerInterface
import com.tyro.oss.pairing.queue.Event
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.client.Socket.*
import java.net.URI
import java.util.concurrent.*

class WebSocketServer(
    hostUrl: String,
    private val sessionName: String,
    private val gson: Gson = Gson()
) : ServerInterface {

    private val socket: Socket = IO.socket(URI.create(hostUrl))
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()
    private val blockingQueue: BlockingQueue<String> = LinkedBlockingQueue()

    companion object {
        private val LOG = Logger.getInstance(KafkaClient::class.java)
    }

    init {
        executor.execute {
            try {
                socket.connect().let { socket ->
                    LOG.info("Web socket connected.")

                    socket.on("FromServer") { params ->
                        LOG.info("FromServer: ${params.size}")
                        if (params.size == 1) {
                            (params[0] as? String)?.let { eventStr ->
                                blockingQueue.offer(eventStr)
                            }
                        }
                    }

                    socket.on(EVENT_DISCONNECT) {
                        LOG.info("Disconnected from server. . $hostUrl")
                    }

                    socket.on(EVENT_CONNECT_ERROR) {
                        LOG.warn("Could not connect to server. $hostUrl")
                    }

                    socket.on(EVENT_CONNECT) {
                        LOG.info("Connected to $hostUrl")
                    }

                    socket.emit("JoinServer", sessionName)
                }
            } catch (e: Exception) {
                LOG.error("Failed to connect to websocket", e)
            }
        }
    }

    override fun consume(): List<Event> {
        if (!socket.connected()) {
            LOG.warn("Socket not open. Not consuming change.")
        }

        return listOf(blockingQueue.poll(10, TimeUnit.SECONDS))
            .map { gson.fromJson(it, Event::class.java) }
    }

    override fun produce(event: Event) {
        if (!socket.connected()) {
            LOG.warn("Socket not open. Not publishing change.")
            return
        }

        socket.emit("Event", sessionName, gson.toJson(event))
    }

    override fun close() {
        LOG.info("Closing socket.")
        socket.disconnect()
        socket.off()
    }
}
