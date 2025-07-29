package com.optimystical.ops.di

import com.optimystical.ops.connectors.EtsyConnector
import com.optimystical.ops.connectors.IPlatformConnector
import com.optimystical.ops.db.OrderRepository
import com.optimystical.ops.db.InventoryRepository
import com.optimystical.ops.inventory.InventoryManager
import com.rabbitmq.client.Channel
import com.optimystical.ops.messaging.RabbitConfig
import com.optimystical.ops.orders.IOrderService
import com.optimystical.ops.orders.OrderProcessor
import org.koin.dsl.module

val appModule = module {
    // 1) All connectors
    single<IPlatformConnector> { EtsyConnector() }
    // TODO (add FacebookConnector, TikTokConnector, etc. here)

    // 2) Data repositories
    single { OrderRepository() }
    single { InventoryRepository() }

    // 3) --- Messaging Channel ---
    // Creates and declares exchanges/queues at startup
    single { RabbitConfig.createChannel() }

    // 4) InventoryManager needs the InventoryRepository, all connectors, and channel
    single { InventoryManager(get(), getAll(), get()) }

    // 5) OrderProcessor needs: IPlatformConnector, OrderRepository, InventoryManager
    single<IOrderService> { OrderProcessor(get(), get(), get()) }
}
