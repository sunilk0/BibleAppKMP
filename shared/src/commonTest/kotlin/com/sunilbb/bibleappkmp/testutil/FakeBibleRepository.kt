package com.sunilbb.bibleappkmp.testutil

import com.sunilbb.bibleappkmp.domain.model.Book
import com.sunilbb.bibleappkmp.domain.model.Bookmark
import com.sunilbb.bibleappkmp.domain.model.Chapter
import com.sunilbb.bibleappkmp.domain.model.Verse
import com.sunilbb.bibleappkmp.domain.repository.BibleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Hand-written in-memory fake for [BibleRepository]. No mocking framework, no real I/O.
 *
 * Behaviour can be steered per test: set [booksResult]/[chaptersResult] to control return
 * values, or set [errorToThrow] to make suspend calls fail.
 */
class FakeBibleRepository : BibleRepository {

    var booksResult: List<Book> = emptyList()
    var chaptersResult: List<Chapter> = emptyList()
    var versesResult: List<Verse> = emptyList()
    var searchResult: List<Verse> = emptyList()

    /** When non-null, suspend calls throw this instead of returning. */
    var errorToThrow: Throwable? = null

    var getBooksCallCount = 0
        private set
    var getChaptersCallCount = 0
        private set
    val addedBookmarks = mutableListOf<Bookmark>()
    val removedBookmarkIds = mutableListOf<String>()
    var lastRequestedChaptersBookId: String? = null
        private set

    private val bookmarksFlow = MutableStateFlow<List<Bookmark>>(emptyList())

    fun emitBookmarks(bookmarks: List<Bookmark>) {
        bookmarksFlow.value = bookmarks
    }

    override suspend fun getBooks(): List<Book> {
        getBooksCallCount++
        errorToThrow?.let { throw it }
        return booksResult
    }

    override suspend fun getChapters(bookId: String): List<Chapter> {
        getChaptersCallCount++
        lastRequestedChaptersBookId = bookId
        errorToThrow?.let { throw it }
        return chaptersResult
    }

    override fun getVersesFlow(bookId: String, chapter: Int): Flow<List<Verse>> =
        MutableStateFlow(versesResult).asStateFlow()

    override suspend fun searchPassage(reference: String): List<Verse> {
        errorToThrow?.let { throw it }
        return searchResult
    }

    override fun getBookmarksFlow(): Flow<List<Bookmark>> = bookmarksFlow.asStateFlow()

    override suspend fun addBookmark(bookmark: Bookmark) {
        errorToThrow?.let { throw it }
        addedBookmarks += bookmark
        bookmarksFlow.value = bookmarksFlow.value + bookmark
    }

    override suspend fun removeBookmark(id: String) {
        errorToThrow?.let { throw it }
        removedBookmarkIds += id
        bookmarksFlow.value = bookmarksFlow.value.filterNot { it.id == id }
    }

    override suspend fun isBookmarked(id: String): Boolean {
        errorToThrow?.let { throw it }
        return bookmarksFlow.value.any { it.id == id }
    }
}
