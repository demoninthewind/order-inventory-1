package com.optimystical.ops.orders

import com.optimystical.ops.model.Order

interface IOrderService {
    /** Fetch, validate, and enqueue new orders */
    suspend fun processNewOrders()

    /** Return true if this order meets all requirements */
    fun validateOrder(order: Order): Boolean

    /** Send a valid order to the fulfillment queue */
    suspend fun enqueueForFulfillment(order: Order)
}
