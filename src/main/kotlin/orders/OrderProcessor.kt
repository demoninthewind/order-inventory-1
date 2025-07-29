package com.optimystical.ops.orders

import com.optimystical.ops.connectors.IPlatformConnector
import com.optimystical.ops.db.OrderRepository
import com.optimystical.ops.inventory.InventoryManager
import com.optimystical.ops.messaging.RabbitConfig
import com.optimystical.ops.model.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import com.rabbitmq.client.MessageProperties
import com.rabbitmq.client.AMQP
import com.rabbitmq.client.AMQP.*
import com.rabbitmq.client.Channel

class OrderProcessor(
    private val connector: IPlatformConnector,
    private val repo: OrderRepository,
    private val inventoryManager: InventoryManager
) : IOrderService {

    override suspend fun processNewOrders() = withContext(Dispatchers.IO) {
        connector.authenticate()
        val orders = connector.fetchOrders()
        for (order in orders) {
            handleOrder(order)
        }
    }

    /** Handle one order: validate, persist, enqueue for fulfillment, then update status */
    private suspend fun handleOrder(order: Order) {
        if (!validateOrder(order)) {
            connector.updateOrderStatus(order.id, "invalid")
            return
        }

        // 1) Persist to DB
        val internalId = repo.insert(order)

        // 2) Enqueue for fulfillment via RabbitMQ
        enqueueForFulfillment(order.copy(id = internalId))

        // 3) Acknowledge receipt back on the platform
        connector.updateOrderStatus(internalId, "received")
    }

    override fun validateOrder(order: Order): Boolean =
        order.items.all { it.quantity > 0 }

    /**
     * Publishes the given order to the "orders" exchange with routing key "new".
     * Uses kotlinx.serialization.Json to encode the Order to JSON.
     */
    override suspend fun enqueueForFulfillment(order: Order) {
        // 1) Create a channel (declares exchange/queue if needed)
        val channel: Channel = RabbitConfig.createChannel()

        // 2) Serialize the order to JSON
        val payload: ByteArray = Json.encodeToString(order).toByteArray(Charsets.UTF_8)

        // 3) Publish to exchange "orders" with routing key "new"
        channel.basicPublish(
            "orders",
            "new",
            MessageProperties.PERSISTENT_TEXT_PLAIN,
            payload
        )

        // 4) Clean up
        channel.close()
    }
}
