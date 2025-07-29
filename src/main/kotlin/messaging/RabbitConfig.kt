package com.optimystical.ops.messaging

import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.Channel

object RabbitConfig {
    private val factory = ConnectionFactory().apply {
        host     = System.getenv("RABBIT_HOST") ?: "localhost"
        port     = System.getenv("RABBIT_PORT")?.toInt() ?: 5672
        username = System.getenv("RABBIT_USER") ?: "guest"
        password = System.getenv("RABBIT_PASS") ?: "guest"
    }

    fun createChannel(): Channel =
        factory.newConnection().createChannel().apply {
            // Declare exchanges/queues as needed
            exchangeDeclare("orders", "direct", true)
            queueDeclare("fulfillment", true, false, false, null)
            queueBind("fulfillment", "orders", "new")
        }
}
