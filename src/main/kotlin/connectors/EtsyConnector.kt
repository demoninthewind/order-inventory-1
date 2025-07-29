package com.optimystical.ops.connectors

import com.optimystical.ops.model.Order
import com.optimystical.ops.model.OrderItem
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.header
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable

class EtsyConnector : IPlatformConnector {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }
    private val baseUrl = "https://api.etsy.com/v3/application"
    private val apiKey: String = System.getenv("ETSY_API_KEY")
        ?: error("ETSY_API_KEY environment variable not set")

    override suspend fun authenticate() {
        // Etsy uses API keys, so no explicit auth step is required here.
    }

    override suspend fun fetchOrders(): List<Order> {
        val response: EtsyOrdersResponse = client.get("$baseUrl/shops/YourShop/receipts") {
            header(HttpHeaders.Authorization, "Bearer $apiKey")
            header(HttpHeaders.Accept, ContentType.Application.Json)
        }.body()

        return response.results.map { r ->
            Order(
                id = r.receiptId,
                platform = "Etsy",
                externalId = r.receiptId.toString(),
                items = r.transactions.map { t ->
                    OrderItem(
                        productId = t.productId,
                        quantity = t.quantity
                    )
                },
                status = if (r.wasShipped) "shipped" else "pending"
            )
        }
    }

    override suspend fun updateInventory(productId: Int, newQty: Int) {
        client.put("$baseUrl/listings/$productId/inventory") {
            header(HttpHeaders.Authorization, "Bearer $apiKey")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(
                mapOf(
                    "products" to listOf(
                        mapOf(
                            "product_id" to productId,
                            "offerings" to listOf(
                                mapOf("quantity" to newQty)
                            )
                        )
                    )
                )
            )
        }
    }

    override suspend fun updateOrderStatus(orderId: Int, status: String) {
        client.put("$baseUrl/receipts/$orderId") {
            header(HttpHeaders.Authorization, "Bearer $apiKey")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(mapOf("was_shipped" to (status == "shipped")))
        }
    }

    @Serializable
    private data class EtsyOrdersResponse(
        val results: List<EtsyOrder>
    )

    @Serializable
    private data class EtsyOrder(
        val receiptId: Int,
        val wasShipped: Boolean,
        val transactions: List<EtsyTransaction>
    )

    @Serializable
    private data class EtsyTransaction(
        val productId: Int,
        val quantity: Int
    )
}