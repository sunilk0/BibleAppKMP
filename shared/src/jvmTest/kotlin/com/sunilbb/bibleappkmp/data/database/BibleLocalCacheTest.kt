package com.sunilbb.bibleappkmp.data.database

import com.sunilbb.bibleappkmp.testutil.bookmark
import com.sunilbb.bibleappkmp.testutil.inMemoryCache
import com.sunilbb.bibleappkmp.domain.model.Book
import com.sunilbb.bibleappkmp.domain.model.Chapter
import com.sunilbb.bibleappkmp.domain.model.Verse
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for the JVM/SQLDelight-backed [BibleLocalCache] actual implementation.
 * Every test starts with a fresh in-memory database so there is no shared state.
 */
class BibleLocalCacheTest {

    // ─── Books ────────────────────────────────────────────────────────────────

    @Test
    fun `getBooks returns empty list when the table is empty`() {
        // Arrange
        val cache = inMemoryCache()

        // Act
        val books = cache.getBooks()

        // Assert
        assertTrue(books.isEmpty())
    }

    @Test
    fun `insertBooks then getBooks returns the same books`() {
        // Arrange
        val cache = inMemoryCache()
        val books = listOf(
            Book("genesis", "Genesis", "Gen", "Old Testament"),
            Book("john", "John", "Joh", "New Testament"),
        )

        // Act
        cache.insertBooks(books)

        // Assert
        val stored = cache.getBooks()
        assertEquals(2, stored.size)
        assertEquals("genesis", stored[0].id)
        assertEquals("john", stored[1].id)
    }

    @Test
    fun `insertBooks is idempotent when called twice with the same data`() {
        // Arrange
        val cache = inMemoryCache()
        val books = listOf(Book("genesis", "Genesis", "Gen", "Old Testament"))

        // Act — second call should silently replace (INSERT OR REPLACE)
        cache.insertBooks(books)
        cache.insertBooks(books)

        // Assert
        assertEquals(1, cache.getBooks().size)
    }

    // ─── Chapters ─────────────────────────────────────────────────────────────

    @Test
    fun `getChapters returns empty list for a book that has no cached chapters`() {
        // Arrange
        val cache = inMemoryCache()

        // Act
        val chapters = cache.getChapters("genesis")

        // Assert
        assertTrue(chapters.isEmpty())
    }

    @Test
    fun `insertChapters then getChapters returns the inserted chapters`() {
        // Arrange
        val cache = inMemoryCache()
        val chapters = listOf(
            Chapter("genesis.1", "genesis", 1),
            Chapter("genesis.2", "genesis", 2),
        )

        // Act
        cache.insertChapters(chapters)

        // Assert
        val stored = cache.getChapters("genesis")
        assertEquals(2, stored.size)
        assertEquals(1, stored[0].number)
        assertEquals(2, stored[1].number)
    }

    @Test
    fun `getChapters only returns chapters for the requested bookId`() {
        // Arrange
        val cache = inMemoryCache()
        cache.insertChapters(listOf(Chapter("genesis.1", "genesis", 1)))
        cache.insertChapters(listOf(Chapter("john.1", "john", 1)))

        // Act
        val genesisChapters = cache.getChapters("genesis")
        val johnChapters = cache.getChapters("john")

        // Assert
        assertEquals(1, genesisChapters.size)
        assertEquals("genesis", genesisChapters.first().bookId)
        assertEquals(1, johnChapters.size)
        assertEquals("john", johnChapters.first().bookId)
    }

    // ─── Verses ───────────────────────────────────────────────────────────────

    @Test
    fun `getVersesFlow emits an empty list when no verses are cached`() = runTest {
        // Arrange
        val cache = inMemoryCache()

        // Act
        val verses = cache.getVersesFlow("john.1").first()

        // Assert
        assertTrue(verses.isEmpty())
    }

    @Test
    fun `insertVerses then getVersesFlow emits the stored verses`() = runTest {
        // Arrange
        val cache = inMemoryCache()
        val verses = listOf(
            Verse("john.1.1", "john.1", "john", 1, "In the beginning"),
            Verse("john.1.2", "john.1", "john", 2, "He was in the beginning"),
        )

        // Act
        cache.insertVerses(verses, "john.1")

        // Assert
        val stored = cache.getVersesFlow("john.1").first()
        assertEquals(2, stored.size)
        assertEquals("In the beginning", stored[0].text)
    }

    @Test
    fun `deleteVersesByChapter removes all verses for that chapter`() = runTest {
        // Arrange
        val cache = inMemoryCache()
        val verses = listOf(Verse("john.1.1", "john.1", "john", 1, "In the beginning"))
        cache.insertVerses(verses, "john.1")

        // Act
        cache.deleteVersesByChapter("john.1")

        // Assert
        assertTrue(cache.getVersesFlow("john.1").first().isEmpty())
    }

    @Test
    fun `deleteVersesByChapter does not affect verses from other chapters`() = runTest {
        // Arrange
        val cache = inMemoryCache()
        cache.insertVerses(listOf(Verse("john.1.1", "john.1", "john", 1, "v1")), "john.1")
        cache.insertVerses(listOf(Verse("john.2.1", "john.2", "john", 1, "v2")), "john.2")

        // Act
        cache.deleteVersesByChapter("john.1")

        // Assert
        assertTrue(cache.getVersesFlow("john.1").first().isEmpty())
        assertEquals(1, cache.getVersesFlow("john.2").first().size)
    }

    // ─── Bookmarks ────────────────────────────────────────────────────────────

    @Test
    fun `getBookmarksFlow emits an empty list initially`() = runTest {
        // Arrange
        val cache = inMemoryCache()

        // Act
        val bookmarks = cache.getBookmarksFlow().first()

        // Assert
        assertTrue(bookmarks.isEmpty())
    }

    @Test
    fun `insertBookmark then getBookmarksFlow emits the saved bookmark`() = runTest {
        // Arrange
        val cache = inMemoryCache()
        val bm = bookmark(id = "john.3.16")

        // Act
        cache.insertBookmark(bm)

        // Assert
        val stored = cache.getBookmarksFlow().first()
        assertEquals(1, stored.size)
        assertEquals("john.3.16", stored.first().id)
    }

    @Test
    fun `isBookmarked returns false before inserting and true after`() = runTest {
        // Arrange
        val cache = inMemoryCache()

        // Act + Assert
        assertFalse(cache.isBookmarked("john.3.16"))
        cache.insertBookmark(bookmark(id = "john.3.16"))
        assertTrue(cache.isBookmarked("john.3.16"))
    }

    @Test
    fun `deleteBookmark removes the entry and isBookmarked becomes false`() = runTest {
        // Arrange
        val cache = inMemoryCache()
        cache.insertBookmark(bookmark(id = "john.3.16"))

        // Act
        cache.deleteBookmark("john.3.16")

        // Assert
        assertFalse(cache.isBookmarked("john.3.16"))
        assertTrue(cache.getBookmarksFlow().first().isEmpty())
    }

    @Test
    fun `insertBookmark with duplicate id replaces the existing entry`() = runTest {
        // Arrange
        val cache = inMemoryCache()
        cache.insertBookmark(bookmark(id = "john.3.16", verseText = "original"))

        // Act
        cache.insertBookmark(bookmark(id = "john.3.16", verseText = "updated"))

        // Assert
        val stored = cache.getBookmarksFlow().first()
        assertEquals(1, stored.size)
        assertEquals("updated", stored.first().verseText)
    }

    @Test
    fun `deleteBookmark on a non-existent id is a silent no-op`() = runTest {
        // Arrange
        val cache = inMemoryCache()

        // Act — must not throw
        cache.deleteBookmark("does.not.exist")

        // Assert
        assertTrue(cache.getBookmarksFlow().first().isEmpty())
    }

    @Test
    fun `insertBookmark preserves all fields round-trip`() = runTest {
        // Arrange
        val cache = inMemoryCache()
        val bm = bookmark(
            id = "psalms.23.1",
            bookId = "psalms",
            bookName = "Psalms",
            chapter = 23,
            verseNumber = 1,
            verseText = "The Lord is my shepherd",
            createdAt = 9_999L,
        )

        // Act
        cache.insertBookmark(bm)
        val stored = cache.getBookmarksFlow().first().first()

        // Assert
        assertEquals(bm.id, stored.id)
        assertEquals(bm.bookId, stored.bookId)
        assertEquals(bm.bookName, stored.bookName)
        assertEquals(bm.chapter, stored.chapter)
        assertEquals(bm.verseNumber, stored.verseNumber)
        assertEquals(bm.verseText, stored.verseText)
        assertEquals(bm.createdAt, stored.createdAt)
    }
}
