package com.sunilbb.bibleappkmp.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BibleDataTest {

    @Test
    fun `bibleBooks contains the full 66-book canon`() {
        assertEquals(66, bibleBooks.size)
    }

    @Test
    fun `bibleBooks splits 39 Old and 27 New Testament books`() {
        val old = bibleBooks.count { it.testament == "Old Testament" }
        val new = bibleBooks.count { it.testament == "New Testament" }
        assertEquals(39, old)
        assertEquals(27, new)
    }

    @Test
    fun `bibleBooks ids are unique`() {
        val ids = bibleBooks.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun `chaptersForBook returns the correct count for a known book`() {
        // Genesis has 50 chapters.
        val chapters = chaptersForBook("genesis")
        assertEquals(50, chapters.size)
    }

    @Test
    fun `chaptersForBook numbers chapters sequentially from one`() {
        val chapters = chaptersForBook("psalms")
        assertEquals(150, chapters.size)
        assertEquals((1..150).toList(), chapters.map { it.number })
    }

    @Test
    fun `chaptersForBook builds composite ids of bookId dot number`() {
        val chapters = chaptersForBook("john")
        assertEquals("john.1", chapters.first().id)
        assertEquals("john.21", chapters.last().id)
    }

    @Test
    fun `chaptersForBook tags every chapter with its bookId`() {
        val chapters = chaptersForBook("mark")
        assertTrue(chapters.all { it.bookId == "mark" })
    }

    @Test
    fun `chaptersForBook returns an empty list for an unknown book`() {
        val chapters = chaptersForBook("nonexistent")
        assertTrue(chapters.isEmpty())
    }
}
