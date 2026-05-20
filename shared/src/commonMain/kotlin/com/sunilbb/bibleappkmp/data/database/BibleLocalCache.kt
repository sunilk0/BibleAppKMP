package com.sunilbb.bibleappkmp.data.database

import com.sunilbb.bibleappkmp.domain.model.Bookmark
import com.sunilbb.bibleappkmp.domain.model.Book
import com.sunilbb.bibleappkmp.domain.model.Chapter
import com.sunilbb.bibleappkmp.domain.model.Verse
import kotlinx.coroutines.flow.Flow

expect class BibleLocalCache {
    fun getBooks(): List<Book>
    fun insertBooks(books: List<Book>)
    fun getChapters(bookId: String): List<Chapter>
    fun insertChapters(chapters: List<Chapter>)
    fun getVersesFlow(chapterId: String): Flow<List<Verse>>
    fun insertVerses(verses: List<Verse>, chapterId: String)
    fun deleteVersesByChapter(chapterId: String)

    fun getBookmarksFlow(): Flow<List<Bookmark>>
    suspend fun insertBookmark(bookmark: Bookmark)
    suspend fun deleteBookmark(id: String)
    suspend fun isBookmarked(id: String): Boolean
}
