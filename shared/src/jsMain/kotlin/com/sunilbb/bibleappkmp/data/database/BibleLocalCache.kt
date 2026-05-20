package com.sunilbb.bibleappkmp.data.database

import com.sunilbb.bibleappkmp.domain.model.Bookmark
import com.sunilbb.bibleappkmp.domain.model.Book
import com.sunilbb.bibleappkmp.domain.model.Chapter
import com.sunilbb.bibleappkmp.domain.model.Verse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

actual class BibleLocalCache {
    private val versesCache = mutableMapOf<String, MutableStateFlow<List<Verse>>>()
    private val bookmarksFlow = MutableStateFlow<Map<String, Bookmark>>(emptyMap())

    actual fun getBooks(): List<Book> = emptyList()
    actual fun insertBooks(books: List<Book>) {}
    actual fun getChapters(bookId: String): List<Chapter> = emptyList()
    actual fun insertChapters(chapters: List<Chapter>) {}

    actual fun getVersesFlow(chapterId: String): Flow<List<Verse>> =
        versesCache.getOrPut(chapterId) { MutableStateFlow(emptyList()) }

    actual fun insertVerses(verses: List<Verse>, chapterId: String) {
        versesCache.getOrPut(chapterId) { MutableStateFlow(emptyList()) }.value = verses
    }

    actual fun deleteVersesByChapter(chapterId: String) {
        versesCache[chapterId]?.value = emptyList()
    }

    actual fun getBookmarksFlow(): Flow<List<Bookmark>> =
        bookmarksFlow.map { it.values.sortedByDescending { b -> b.createdAt } }

    actual suspend fun insertBookmark(bookmark: Bookmark) {
        bookmarksFlow.value = bookmarksFlow.value + (bookmark.id to bookmark)
    }

    actual suspend fun deleteBookmark(id: String) {
        bookmarksFlow.value = bookmarksFlow.value - id
    }

    actual suspend fun isBookmarked(id: String): Boolean =
        bookmarksFlow.value.containsKey(id)
}
