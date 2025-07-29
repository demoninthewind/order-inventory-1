package com.optimystical.ops.model

data class Order(
    val id: Int,
    val platform: String,
    val externalId: String,
    val items: List<OrderItem>,
    var status: String
)
