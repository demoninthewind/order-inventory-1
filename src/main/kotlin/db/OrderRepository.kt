package com.optimystical.ops.db

import com.optimystical.ops.model.Order
import com.optimystical.ops.model.OrderItem
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class OrderRepository {

    fun insert(order: Order): Int = transaction {
        // insertAndGetId() returns EntityID<Int>, so we grab .value
        val id = Orders.insertAndGetId {
            it[platform]   = order.platform
            it[externalId] = order.externalId
            it[status]     = order.status
        }.value

        // use the raw Int id for the foreign‑key ref
        order.items.forEach { item ->
            OrderItems.insert {
                it[OrderItems.order]   = id          // reference expects Int here
                it[OrderItems.productId] = item.productId
                it[OrderItems.quantity]  = item.quantity
            }
        }
        id
    }

    fun updateStatus(id: Int, status: String): Int = transaction {
        Orders.update({ Orders.id eq id }) {
            it[Orders.status] = status
        }
    }

    fun findAllPending(): List<Order> = transaction {
        (Orders innerJoin OrderItems)
            .select { Orders.status eq "pending" }
            .groupBy(Orders.id, Orders.platform, Orders.externalId, Orders.status)
            .map { row ->
                // row[Orders.id] is EntityID<Int>; pull .value for your data class
                val orderId = row[Orders.id].value

                // fetch items by matching the EntityID
                val items = OrderItems
                    .select { OrderItems.order eq row[Orders.id] }
                    .map { iRow ->
                        OrderItem(
                            productId = iRow[OrderItems.productId],
                            quantity  = iRow[OrderItems.quantity]
                        )
                    }

                Order(
                    id         = orderId,
                    platform   = row[Orders.platform],
                    externalId = row[Orders.externalId],
                    items      = items,
                    status     = row[Orders.status]
                )
            }
    }
}
