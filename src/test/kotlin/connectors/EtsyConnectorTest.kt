package com.optimystical.ops.connectors

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class EtsyConnectorTest {
    private val connector = EtsyConnector()

    @Test
    fun `test fetchOrders does not throw and returns list`() = runBlocking {
        // Assuming ETSY_API_KEY and shop URL are set correctly
        val orders = connector.fetchOrders()
        assertNotNull(orders, "fetchOrders should return a non-null list")
        println("Fetched ${orders.size} orders successfully.")
    }

    @Test
    fun `test shop ping responds OK`() = runBlocking {
        // Simple ping to shop details endpoint
        val apiKey = System.getenv("ETSY_API_KEY") ?: error("ETSY_API_KEY not set")
        val client = connector.client
        val response = client.get("${connector.baseUrl}/shops/YourShopName") {
            header("Authorization", "Bearer $apiKey")
        }
        assert(response.status == HttpStatusCode.OK) { "Ping failed with ${response.status}" }
    }
}