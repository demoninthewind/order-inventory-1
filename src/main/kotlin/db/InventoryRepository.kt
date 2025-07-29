// src/main/kotlin/com/optimystical/ops/db/InventoryRepository.kt
package com.optimystical.ops.db

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Domain model for inventory levels.
 */
data class InventoryLevel(
    val id: Int,
    val productId: Int,
    val stockAvailable: Int,
    val reorderThreshold: Int
)

class InventoryRepository {

    /**
     * Inserts a new inventory row and returns its generated ID.
     */
    fun insertInventory(
        productId: Int,
        initialStock: Int,
        reorderThreshold: Int
    ): Int = transaction {
        InventoryLevels.insertAndGetId { row ->
            row[InventoryLevels.productId] = productId
            row[InventoryLevels.stockAvailable] = initialStock
            row[InventoryLevels.reorderThreshold] = reorderThreshold
        }.value
    }

    /**
     * Retrieves the inventory record for the given product,
     * or null if none exists.
     */
    fun findByProduct(productId: Int): InventoryLevel? = transaction {
        InventoryLevels
            .select { InventoryLevels.productId eq productId }
            .map { row ->
                InventoryLevel(
                    id               = row[InventoryLevels.id].value,
                    productId        = row[InventoryLevels.productId],
                    stockAvailable   = row[InventoryLevels.stockAvailable],
                    reorderThreshold = row[InventoryLevels.reorderThreshold]
                )
            }
            .singleOrNull()
    }

    /**
     * Updates the stock level for a product.
     * Returns true if a row was actually updated.
     */
    fun updateStockLevel(productId: Int, newLevel: Int): Boolean = transaction {
        InventoryLevels
            .update({ InventoryLevels.productId eq productId }) { row ->
                row[InventoryLevels.stockAvailable] = newLevel
            } > 0
    }

    /**
     * Returns all inventory items that are at or below
     * their reorder threshold.
     */
    fun findLowStockItems(): List<InventoryLevel> = transaction {
        InventoryLevels
            .select { InventoryLevels.stockAvailable lessEq InventoryLevels.reorderThreshold }
            .map { row ->
                InventoryLevel(
                    id               = row[InventoryLevels.id].value,
                    productId        = row[InventoryLevels.productId],
                    stockAvailable   = row[InventoryLevels.stockAvailable],
                    reorderThreshold = row[InventoryLevels.reorderThreshold]
                )
            }
    }
}
