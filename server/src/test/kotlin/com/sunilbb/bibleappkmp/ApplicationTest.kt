package com.sunilbb.bibleappkmp

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.*

class ApplicationTest {

    @Test
    fun testRoot() = testApplication {
        application {
            module()
        }
        val response = client.get("/")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("Ktor: ${Greeting().greet()}", response.bodyAsText())
    }

    @Test
    fun rootResponseStartsWithKtorPrefix() = testApplication {
        application {
            module()
        }
        val body = client.get("/").bodyAsText()
        assertTrue(body.startsWith("Ktor: "))
    }

    @Test
    fun unknownRouteReturnsNotFound() = testApplication {
        application {
            module()
        }
        val response = client.get("/does-not-exist")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun serverPortConstantIsExpectedValue() {
        assertEquals(8080, SERVER_PORT)
    }
}