package com.sunilbb.bibleappkmp.data.api

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class BibleApiServiceTest {

    private fun jsonClient(handler: MockEngine): HttpClient = HttpClient(handler) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true; isLenient = true })
        }
        defaultRequest { url("https://bible-api.com") }
    }

    private fun respondJson(body: String, status: HttpStatusCode = HttpStatusCode.OK) =
        MockEngine { _ ->
            respond(
                content = body,
                status = status,
                headers = headersOf("Content-Type", ContentType.Application.Json.toString()),
            )
        }

    @Test
    fun `getBooks deserializes the books response`() = runTest {
        // Arrange
        val engine = respondJson(
            """{"books":[{"id":"john","name":"John","abbreviation":"Joh","testament":"New Testament"}]}""",
        )
        val service = BibleApiService(jsonClient(engine))

        // Act
        val response = service.getBooks()

        // Assert
        assertEquals(1, response.books.size)
        assertEquals("John", response.books.first().name)
    }

    @Test
    fun `getBooks targets the books endpoint`() = runTest {
        // Arrange
        val engine = respondJson("""{"books":[]}""")
        val service = BibleApiService(jsonClient(engine))

        // Act
        service.getBooks()

        // Assert
        assertTrue(engine.requestHistory.first().url.encodedPath.endsWith("/books"))
    }

    @Test
    fun `getChapters embeds the bookId in the path`() = runTest {
        // Arrange
        val engine = respondJson("""{"chapters":[]}""")
        val service = BibleApiService(jsonClient(engine))

        // Act
        service.getChapters("psalms")

        // Assert
        assertTrue(engine.requestHistory.first().url.encodedPath.endsWith("/chapters/psalms"))
    }

    @Test
    fun `getPassage deserializes verses from the response`() = runTest {
        // Arrange
        val engine = respondJson(
            """{"reference":"John 3:16","verses":[{"book_id":"JHN","book_name":"John","chapter":3,"verse":16,"text":"For God so loved the world"}],"text":"...","translation_id":"web","translation_name":"WEB"}""",
        )
        val service = BibleApiService(jsonClient(engine))

        // Act
        val response = service.getPassage("John%203:16")

        // Assert
        assertEquals(1, response.verses.size)
        assertEquals(16, response.verses.first().verse)
        assertEquals("John 3:16", response.reference)
    }

    @Test
    fun `getPassage on a 404 fails instead of returning a value`() = runTest {
        // Arrange — the production client does not set expectSuccess, so a 404 body
        // (plain text) cannot be deserialized into VersesResponseDto.
        val engine = MockEngine { respondError(HttpStatusCode.NotFound) }
        val service = BibleApiService(jsonClient(engine))

        // Act + Assert
        assertFailsWith<NoTransformationFoundException> { service.getPassage("Bogus%201:1") }
    }
}
