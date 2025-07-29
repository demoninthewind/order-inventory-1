package com.optimystical.ops.api

import com.optimystical.ops.db.DatabaseFactory
import com.optimystical.ops.di.appModule
import com.optimystical.ops.orders.IOrderService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.Netty
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.runBlocking
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin

fun main() {
    // Initialize the database
    DatabaseFactory.init()

    embeddedServer(Netty, port = 8080) {
        install(Koin) {
            modules(appModule)
        }

        routing {
            get("/health") {
                call.respond(HttpStatusCode.OK, "OK")
            }
            get("/orders/process") {
                val orderService: IOrderService by this@routing.inject<IOrderService>()
                runBlocking {
                    orderService.processNewOrders()
                }
                call.respond(HttpStatusCode.Accepted, "Order processing started.")

            }
        }
    }.start(true)
        println("🚀 Server has started!")   // You should never actually see this until the server stops
}

//private fun Routing.start(wait: Boolean) {}

