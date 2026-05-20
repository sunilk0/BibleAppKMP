package com.sunilbb.bibleappkmp.domain.repository

import com.sunilbb.bibleappkmp.domain.model.Bookmark
import com.sunilbb.bibleappkmp.domain.model.Book
import com.sunilbb.bibleappkmp.domain.model.Chapter
import com.sunilbb.bibleappkmp.domain.model.Verse
import kotlinx.coroutines.flow.Flow

interface BibleRepository {
    suspend fun getBooks(): List<Book>
    suspend fun getChapters(bookId: String): List<Chapter>
    fun getVersesFlow(bookId: String, chapter: Int): Flow<List<Verse>>
    suspend fun searchPassage(reference: String): List<Verse>

    fun getBookmarksFlow(): Flow<List<Bookmark>>
    suspend fun addBookmark(bookmark: Bookmark)
    suspend fun removeBookmark(id: String)
    suspend fun isBookmarked(id: String): Boolean
}
