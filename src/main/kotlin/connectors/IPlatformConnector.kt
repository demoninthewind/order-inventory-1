package com.optimystical.ops.connectors

import com.optimystical.ops.model.Order

interface IPlatformConnector {
    /** Authenticate with the storefront API (e.g., OAuth2) */
    suspend fun authenticate()

    /** Fetch new orders from the platform */
    suspend fun fetchOrders(): List<Order>

    /** Update the stock level for one product */
    suspend fun updateInventory(productId: Int, newQty: Int)

    /** Update an order’s status (e.g., “shipped”) */
    suspend fun updateOrderStatus(orderId: Int, status: String)
}
