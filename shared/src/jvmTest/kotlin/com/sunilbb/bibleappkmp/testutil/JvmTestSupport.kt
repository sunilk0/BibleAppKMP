package com.sunilbb.bibleappkmp.testutil

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.sunilbb.bibleappkmp.data.api.BibleApiService
import com.sunilbb.bibleappkmp.data.database.BibleLocalCache
import com.sunilbb.bibleappkmp.db.BibleDatabase
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Builds a [BibleLocalCache] backed by an in-memory SQLDelight database.
 * Each call yields a fresh, isolated database — no shared state between tests.
 */
fun inMemoryCache(): BibleLocalCache {
    val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    BibleDatabase.Schema.create(driver)
    return BibleLocalCache(BibleDatabase(driver))
}

/** A [BibleApiService] whose HTTP layer is a Ktor [MockEngine] returning [json]. */
fun mockApiService(
    status: HttpStatusCode = HttpStatusCode.OK,
    json: String,
): BibleApiService {
    val engine = MockEngine {
        respond(
            content = json,
            status = status,
            headers = headersOf("Content-Type", ContentType.Application.Json.toString()),
        )
    }
    return BibleApiService(jsonHttpClient(engine))
}

/** A [BibleApiService] whose HTTP layer always fails — exercises offline / error paths. */
fun failingApiService(status: HttpStatusCode = HttpStatusCode.InternalServerError): BibleApiService {
    val engine = MockEngine { respondError(status) }
    return BibleApiService(jsonHttpClient(engine))
}

private fun jsonHttpClient(engine: MockEngine): HttpClient = HttpClient(engine) {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true; isLenient = true })
    }
    defaultRequest { url("https://bible-api.com") }
}
