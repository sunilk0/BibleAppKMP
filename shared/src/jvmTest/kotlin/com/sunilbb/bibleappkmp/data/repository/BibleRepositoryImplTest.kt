package com.sunilbb.bibleappkmp.data.repository

import com.sunilbb.bibleappkmp.testutil.bookmark
import com.sunilbb.bibleappkmp.testutil.failingApiService
import com.sunilbb.bibleappkmp.testutil.inMemoryCache
import com.sunilbb.bibleappkmp.testutil.mockApiService
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Exercises [BibleRepositoryImpl] against a real in-memory SQLDelight cache and a
 * Ktor MockEngine — no production code is mocked.
 */
class BibleRepositoryImplTest {

    private val emptyVersesJson = """{"reference":"","verses":[],"text":"","translation_id":"","translation_name":""}"""

    @Test
    fun `getBooks returns the canon and seeds the empty cache`() = runTest {
        // Arrange
        val cache = inMemoryCache()
        val repo = BibleRepositoryImpl(mockApiService(json = emptyVersesJson), cache)

        // Act
        val books = repo.getBooks()

        // Assert
        assertEquals(66, books.size)
        assertEquals(66, cache.getBooks().size)
    }

    @Test
    fun `getBooks serves cached books on subsequent calls`() = runTest {
        // Arrange
        val cache = inMemoryCache()
        val repo = BibleRepositoryImpl(mockApiService(json = emptyVersesJson), cache)
        repo.getBooks() // first call seeds the cache

        // Act
        val cachedBooks = repo.getBooks()

        // Assert
        assertEquals(66, cachedBooks.size)
    }

    @Test
    fun `getChapters returns the chapter list for a known book and caches it`() = runTest {
        // Arrange
        val cache = inMemoryCache()
        val repo = BibleRepositoryImpl(mockApiService(json = emptyVersesJson), cache)

        // Act
        val chapters = repo.getChapters("genesis")

        // Assert
        assertEquals(50, chapters.size)
        assertEquals(50, cache.getChapters("genesis").size)
    }

    @Test
    fun `getChapters returns an empty list for an unknown book`() = runTest {
        // Arrange
        val cache = inMemoryCache()
        val repo = BibleRepositoryImpl(mockApiService(json = emptyVersesJson), cache)

        // Act
        val chapters = repo.getChapters("nonexistent")

        // Assert
        assertTrue(chapters.isEmpty())
    }

    @Test
    fun `addBookmark then getBookmarksFlow emits the saved bookmark`() = runTest {
        // Arrange
        val repo = BibleRepositoryImpl(mockApiService(json = emptyVersesJson), inMemoryCache())
        val target = bookmark(id = "john.3.16")

        // Act
        repo.addBookmark(target)
        val emitted = repo.getBookmarksFlow().first()

        // Assert
        assertEquals(listOf(target), emitted)
    }

    @Test
    fun `isBookmarked is true only after a bookmark is added`() = runTest {
        // Arrange
        val repo = BibleRepositoryImpl(mockApiService(json = emptyVersesJson), inMemoryCache())

        // Act + Assert
        assertFalse(repo.isBookmarked("john.3.16"))
        repo.addBookmark(bookmark(id = "john.3.16"))
        assertTrue(repo.isBookmarked("john.3.16"))
    }

    @Test
    fun `removeBookmark deletes a previously added bookmark`() = runTest {
        // Arrange
        val repo = BibleRepositoryImpl(mockApiService(json = emptyVersesJson), inMemoryCache())
        repo.addBookmark(bookmark(id = "john.3.16"))

        // Act
        repo.removeBookmark("john.3.16")

        // Assert
        assertFalse(repo.isBookmarked("john.3.16"))
        assertTrue(repo.getBookmarksFlow().first().isEmpty())
    }

    @Test
    fun `addBookmark with a duplicate id replaces the existing bookmark`() = runTest {
        // Arrange
        val repo = BibleRepositoryImpl(mockApiService(json = emptyVersesJson), inMemoryCache())
        repo.addBookmark(bookmark(id = "john.3.16", verseText = "old text"))

        // Act
        repo.addBookmark(bookmark(id = "john.3.16", verseText = "new text"))
        val emitted = repo.getBookmarksFlow().first()

        // Assert
        assertEquals(1, emitted.size)
        assertEquals("new text", emitted.first().verseText)
    }

    @Test
    fun `searchPassage maps verse DTOs into domain verses with composite ids`() = runTest {
        // Arrange
        val json = """{"reference":"John 3:16","verses":[{"book_id":"JHN","book_name":"John","chapter":3,"verse":16,"text":"  For God so loved the world  "}],"text":"...","translation_id":"web","translation_name":"WEB"}"""
        val repo = BibleRepositoryImpl(mockApiService(json = json), inMemoryCache())

        // Act
        val verses = repo.searchPassage("John 3:16")

        // Assert
        assertEquals(1, verses.size)
        val verse = verses.first()
        assertEquals("JHN.3.16", verse.id)
        assertEquals("JHN.3", verse.chapterId)
        assertEquals("For God so loved the world", verse.text) // trimmed
    }

    @Test
    fun `searchPassage propagates an API error`() = runTest {
        // Arrange — searchPassage does not catch errors, so a failed request surfaces.
        val repo = BibleRepositoryImpl(failingApiService(HttpStatusCode.NotFound), inMemoryCache())

        // Act + Assert
        assertFailsWith<NoTransformationFoundException> { repo.searchPassage("Bogus 1:1") }
    }

    @Test
    fun `fetchAndCacheVerses stores fetched verses so getVersesFlow emits them`() = runTest {
        // Arrange
        val json = """{"reference":"John 1","verses":[{"book_id":"JHN","book_name":"John","chapter":1,"verse":1,"text":"In the beginning"},{"book_id":"JHN","book_name":"John","chapter":1,"verse":2,"text":"The same was in the beginning"}],"text":"...","translation_id":"web","translation_name":"WEB"}"""
        val repo = BibleRepositoryImpl(mockApiService(json = json), inMemoryCache())

        // Act
        repo.fetchAndCacheVerses("john", 1)
        val verses = repo.getVersesFlow("john", 1).first()

        // Assert
        assertEquals(2, verses.size)
        assertEquals("john.1.1", verses.first().id)
        assertEquals("In the beginning", verses.first().text)
    }

    @Test
    fun `fetchAndCacheVerses ignores an unknown book without throwing`() = runTest {
        // Arrange
        val repo = BibleRepositoryImpl(mockApiService(json = emptyVersesJson), inMemoryCache())

        // Act
        repo.fetchAndCacheVerses("nonexistent", 1)
        val verses = repo.getVersesFlow("nonexistent", 1).first()

        // Assert
        assertTrue(verses.isEmpty())
    }

    @Test
    fun `fetchAndCacheVerses swallows API failures and leaves the cache untouched`() = runTest {
        // Arrange
        val repo = BibleRepositoryImpl(failingApiService(), inMemoryCache())

        // Act — must not throw even though the API errors
        repo.fetchAndCacheVerses("john", 1)
        val verses = repo.getVersesFlow("john", 1).first()

        // Assert
        assertTrue(verses.isEmpty())
    }
}
