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
package com.tyro.oss.pairing

import com.google.gson.Gson
import com.intellij.openapi.diagnostic.Logger
import com.tyro.oss.pairing.queue.Event
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.errors.WakeupException
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import java.time.Duration
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean


class KafkaClient(
    machineName: String,
    private val topic: String
) {

    companion object {
        private val LOG = Logger.getInstance(KafkaClient::class.java)
        const val URL_ID = "kafkaUrl"
        const val WORKSPACE_ID = "worspaceId"
    }

    private val serializer = StringSerializer::class.java
    private val deserializer = StringDeserializer::class.java
    private val propsConsumer = Properties()
    private val propsProducer = Properties()
    private val gson = Gson()
    private val producer: KafkaProducer<String, String>
    private val consumer: KafkaConsumer<String, String>
    private val id = UUID.randomUUID().toString()
    private val closed = AtomicBoolean(false)

    init {
        propsConsumer["enable.auto.commit"] = "true";
        propsConsumer["auto.commit.interval.ms"] = "1000";
        propsConsumer["session.timeout.ms"] = "30000";
        propsConsumer["bootstrap.servers"] = "$machineName:9092"
        propsProducer["bootstrap.servers"] = "$machineName:9092"
        propsConsumer["key.deserializer"] = deserializer;
        propsConsumer["value.deserializer"] = deserializer;
        propsConsumer["key.serializer"] = serializer;
        propsConsumer["value.serializer"] = serializer;
        propsConsumer["client.id"] = "consumer-$id";
        propsConsumer["group.id"] = id;
        propsProducer["key.deserializer"] = deserializer;
        propsProducer["value.deserializer"] = deserializer;
        propsProducer["key.serializer"] = serializer;
        propsProducer["value.serializer"] = serializer;

        producer = KafkaProducer(propsProducer)
        consumer = KafkaConsumer(propsConsumer)

        consumer.subscribe(listOf(topic))
    }

    fun consume(): List<Event> {
        try {
            val records = consumer.poll(Duration.ofMillis(10000))
            return records.mapNotNull {
                gson.fromJson(it.value(), Event::class.java)
            }
        } catch (e: WakeupException) {
            // Ignore exception if closing
            LOG.info("Closing consumer", e)
            consumer.close()
        } catch (e: Exception) {
            LOG.error(e)
        }
        return emptyList()
    }


    fun produce(event: Event) {
        producer.send(ProducerRecord<String, String>(topic, gson.toJson(event)))
        LOG.info("sent message ${gson.toJson(event)}")
    }

    fun close() {
        closed.set(true)
        consumer.wakeup()
    }
}
