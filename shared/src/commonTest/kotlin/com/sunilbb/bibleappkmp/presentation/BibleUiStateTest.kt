package com.sunilbb.bibleappkmp.presentation

import com.sunilbb.bibleappkmp.testutil.book
import com.sunilbb.bibleappkmp.testutil.bookmark
import com.sunilbb.bibleappkmp.testutil.chapter
import com.sunilbb.bibleappkmp.testutil.verse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit tests for the UI state data classes.
 * Covers default field values, copy behaviour, and error transitions.
 */
class BibleUiStateTest {

    // ─── BooksUiState ─────────────────────────────────────────────────────────

    @Test
    fun `BooksUiState defaults to not loading with empty books and no error`() {
        val state = BooksUiState()
        assertFalse(state.isLoading)
        assertTrue(state.books.isEmpty())
        assertNull(state.error)
    }

    @Test
    fun `BooksUiState copy with isLoading true preserves books and error`() {
        val state = BooksUiState(books = listOf(book()), error = null)
        val loading = state.copy(isLoading = true)
        assertTrue(loading.isLoading)
        assertEquals(1, loading.books.size)
        assertNull(loading.error)
    }

    @Test
    fun `BooksUiState copy with error message clears isLoading`() {
        val state = BooksUiState(isLoading = true)
        val errored = state.copy(isLoading = false, error = "network timeout")
        assertFalse(errored.isLoading)
        assertEquals("network timeout", errored.error)
    }

    @Test
    fun `BooksUiState copy with books list replaces existing list`() {
        val initial = BooksUiState(books = listOf(book(id = "genesis")))
        val updated = initial.copy(books = listOf(book(id = "genesis"), book(id = "john")))
        assertEquals(2, updated.books.size)
    }

    // ─── ChaptersUiState ──────────────────────────────────────────────────────

    @Test
    fun `ChaptersUiState defaults to not loading with empty chapters and no error`() {
        val state = ChaptersUiState()
        assertFalse(state.isLoading)
        assertTrue(state.chapters.isEmpty())
        assertNull(state.error)
    }

    @Test
    fun `ChaptersUiState loaded transition clears loading flag and stores chapters`() {
        val state = ChaptersUiState(isLoading = true)
        val loaded = state.copy(isLoading = false, chapters = listOf(chapter()))
        assertFalse(loaded.isLoading)
        assertEquals(1, loaded.chapters.size)
    }

    @Test
    fun `ChaptersUiState error transition stores the error message`() {
        val errored = ChaptersUiState().copy(error = "book not found")
        assertEquals("book not found", errored.error)
    }

    // ─── ReaderUiState ────────────────────────────────────────────────────────

    @Test
    fun `ReaderUiState defaults to empty bookId zero chapter and no verses`() {
        val state = ReaderUiState()
        assertFalse(state.isLoading)
        assertEquals("", state.bookId)
        assertEquals(0, state.chapter)
        assertTrue(state.verses.isEmpty())
        assertNull(state.error)
    }

    @Test
    fun `ReaderUiState copy with bookId and chapter stores them`() {
        val state = ReaderUiState().copy(bookId = "john", chapter = 3)
        assertEquals("john", state.bookId)
        assertEquals(3, state.chapter)
    }

    @Test
    fun `ReaderUiState copy with verses list replaces empty default`() {
        val verses = listOf(verse())
        val state = ReaderUiState().copy(verses = verses)
        assertEquals(1, state.verses.size)
    }

    @Test
    fun `ReaderUiState error clears loading and sets message`() {
        val state = ReaderUiState(isLoading = true).copy(isLoading = false, error = "cache miss")
        assertFalse(state.isLoading)
        assertEquals("cache miss", state.error)
    }

    // ─── BookmarksUiState ─────────────────────────────────────────────────────

    @Test
    fun `BookmarksUiState defaults to empty bookmarks`() {
        val state = BookmarksUiState()
        assertTrue(state.bookmarks.isEmpty())
    }

    @Test
    fun `BookmarksUiState copy with bookmarks list replaces empty default`() {
        val state = BookmarksUiState().copy(bookmarks = listOf(bookmark()))
        assertEquals(1, state.bookmarks.size)
    }

    @Test
    fun `BookmarksUiState copy clearing bookmarks results in empty list`() {
        val state = BookmarksUiState(bookmarks = listOf(bookmark()))
        val cleared = state.copy(bookmarks = emptyList())
        assertTrue(cleared.bookmarks.isEmpty())
    }
}
