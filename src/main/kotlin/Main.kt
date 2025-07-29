package com.optimystical.ops

import com.optimystical.ops.connectors.EtsyConnector
import com.optimystical.ops.di.appModule
import com.optimystical.ops.orders.IOrderService
import kotlinx.coroutines.runBlocking
import org.koin.core.context.startKoin

fun main() {
    // Quick Etsy API validation comment out once automated tests reliably pass
    val connector = EtsyConnector()
    runBlocking {
        val orders = connector.fetchOrders()
        println("[Manual Check] Fetched ${orders.size} orders from Etsy.")
    }

    // 1. Start Koin and capture the KoinApplication
    val koinApp = startKoin {
        modules(appModule)
    }

    // 2. Pull your service directly from that application
    val orderService: IOrderService = koinApp.koin.get()

    // 3. Run your business logic
    runBlocking {
        orderService.processNewOrders()
        println("Done processing orders!")
    }
}
