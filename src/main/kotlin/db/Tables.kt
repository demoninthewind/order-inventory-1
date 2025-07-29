package com.optimystical.ops.db

import org.jetbrains.exposed.dao.id.IntIdTable

object Orders : IntIdTable("orders") {
    val platform    = varchar("platform", 32)
    val externalId  = varchar("external_id", 64)
    val status      = varchar("status", 16)
}

object OrderItems : IntIdTable("order_items") {
    val order      = reference("order_id", Orders)
    val productId  = integer("product_id")
    val quantity   = integer("quantity")
}

object InventoryLevels : IntIdTable("inventory_levels") {
    val productId      = integer("product_id").uniqueIndex()
    val stockAvailable = integer("stock_available")
    val reorderThreshold = integer("reorder_threshold")
}
