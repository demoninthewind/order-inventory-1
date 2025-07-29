package com.optimystical.ops.inventory

import com.optimystical.ops.connectors.IPlatformConnector
import com.optimystical.ops.db.InventoryRepository
import com.optimystical.ops.db.InventoryLevel
import com.optimystical.ops.model.Order
import com.rabbitmq.client.Channel
import com.rabbitmq.client.DeliverCallback
import com.rabbitmq.client.CancelCallback
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class InventoryManager(
    private val repo: InventoryRepository,
    private val connectors: List<IPlatformConnector>,
    private val channel: Channel
) {

    /**
     * Adjusts local stock in the database and broadcasts the new level
     * to all connected storefronts.
     * Wraps the suspend call to updateInventory in runBlocking so it compiles.
     */
    fun adjustStock(productId: Int, newQty: Int) {
        // 1) DB update or insert
        val updated = repo.updateStockLevel(productId, newQty)
        if (!updated) {
            repo.insertInventory(productId, newQty, reorderThreshold = 5)
        }

        // 2) Broadcast to each platform (updateInventory is suspend)
        connectors.forEach { connector ->
            runBlocking {
                connector.updateInventory(productId, newQty)
            }
        }
    }

    /** Retrieves all inventory entries at or below their reorder threshold. */
    fun fetchLowStockItems(): List<InventoryLevel> =
        repo.findLowStockItems()

    /** If any items are low, invokes the given callback with the list. */
    fun alertLowStock(onAlert: (List<InventoryLevel>) -> Unit) {
        val low = fetchLowStockItems()
        if (low.isNotEmpty()) onAlert(low)
    }

    /**
     * Starts a RabbitMQ consumer on the "fulfillment" queue.
     * For each incoming Order, deducts quantities and broadcasts updates.
     */
    fun startListening() {
        val deliverCallback = DeliverCallback { _, delivery ->
            val jsonText = String(delivery.body, Charsets.UTF_8)
            val order: Order = Json.decodeFromString(jsonText)

            // Decrement stock for each item
            order.items.forEach { item ->
                val current = repo.findByProduct(item.productId)?.stockAvailable ?: 0
                val newQty = current - item.quantity
                adjustStock(item.productId, newQty)
            }
        }

        // CancelCallback only takes the consumerTag parameter
        val cancelCallback = CancelCallback { _ -> /* no-op */ }

        channel.basicConsume(
            "fulfillment",
            true,
            deliverCallback,
            cancelCallback
        )
    }
}
