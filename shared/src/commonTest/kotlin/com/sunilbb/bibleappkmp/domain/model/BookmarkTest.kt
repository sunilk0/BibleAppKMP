package com.sunilbb.bibleappkmp.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/**
 * Unit tests for the [Bookmark] domain model.
 * Covers equality, copy semantics, and field accessors.
 */
class BookmarkTest {

    private fun sample() = Bookmark(
        id = "john.3.16",
        bookId = "john",
        bookName = "John",
        chapter = 3,
        verseNumber = 16,
        verseText = "For God so loved the world",
        createdAt = 1_000L,
    )

    @Test
    fun `two bookmarks with the same fields are equal`() {
        assertEquals(sample(), sample())
    }

    @Test
    fun `two bookmarks with different ids are not equal`() {
        assertNotEquals(sample(), sample().copy(id = "psalms.23.1"))
    }

    @Test
    fun `copy changes only the specified field`() {
        // Arrange
        val original = sample()

        // Act
        val updated = original.copy(verseText = "new text")

        // Assert
        assertEquals("new text", updated.verseText)
        assertEquals(original.id, updated.id)
        assertEquals(original.chapter, updated.chapter)
    }

    @Test
    fun `chapter field reflects the value set at construction`() {
        val bm = sample().copy(chapter = 23)
        assertEquals(23, bm.chapter)
    }

    @Test
    fun `createdAt field reflects the value set at construction`() {
        val bm = sample().copy(createdAt = 42_000L)
        assertEquals(42_000L, bm.createdAt)
    }

    @Test
    fun `bookName and bookId can differ legitimately`() {
        // Arrange — abbreviated id vs display name
        val bm = Bookmark(
            id = "1john.1.1",
            bookId = "1john",
            bookName = "1 John",
            chapter = 1,
            verseNumber = 1,
            verseText = "That which was from the beginning",
            createdAt = 0L,
        )

        // Assert
        assertEquals("1john", bm.bookId)
        assertEquals("1 John", bm.bookName)
    }
}
