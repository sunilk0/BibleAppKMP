package com.sunilbb.bibleappkmp.data.database

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.sunilbb.bibleappkmp.db.BibleDatabase
import com.sunilbb.bibleappkmp.domain.model.Bookmark
import com.sunilbb.bibleappkmp.domain.model.Book
import com.sunilbb.bibleappkmp.domain.model.Chapter
import com.sunilbb.bibleappkmp.domain.model.Verse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

actual class BibleLocalCache(private val db: BibleDatabase) {

    actual fun getBooks(): List<Book> =
        db.bibleDatabaseQueries.selectAllBooks().executeAsList().map {
            Book(id = it.id, name = it.name, abbreviation = it.abbreviation, testament = it.testament)
        }

    actual fun insertBooks(books: List<Book>) = books.forEach {
        db.bibleDatabaseQueries.insertBook(it.id, it.name, it.abbreviation, it.testament)
    }

    actual fun getChapters(bookId: String): List<Chapter> =
        db.bibleDatabaseQueries.selectChaptersByBook(bookId).executeAsList().map {
            Chapter(id = it.id, bookId = it.bookId, number = it.number.toInt())
        }

    actual fun insertChapters(chapters: List<Chapter>) = chapters.forEach {
        db.bibleDatabaseQueries.insertChapter(it.id, it.bookId, it.number.toLong())
    }

    actual fun getVersesFlow(chapterId: String): Flow<List<Verse>> =
        db.bibleDatabaseQueries.selectVersesByChapter(chapterId)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list ->
                list.map { Verse(id = it.id, chapterId = it.chapterId, bookId = it.bookId, number = it.number.toInt(), text = it.text) }
            }

    actual fun insertVerses(verses: List<Verse>, chapterId: String) = verses.forEach {
        db.bibleDatabaseQueries.insertVerse(it.id, it.chapterId, it.bookId, it.number.toLong(), it.text)
    }

    actual fun deleteVersesByChapter(chapterId: String) =
        db.bibleDatabaseQueries.deleteVersesByChapter(chapterId)

    actual fun getBookmarksFlow(): Flow<List<Bookmark>> =
        db.bibleDatabaseQueries.selectAllBookmarks()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list ->
                list.map {
                    Bookmark(
                        id = it.id,
                        bookId = it.bookId,
                        bookName = it.bookName,
                        chapter = it.chapter.toInt(),
                        verseNumber = it.verseNumber.toInt(),
                        verseText = it.verseText,
                        createdAt = it.createdAt,
                    )
                }
            }

    actual suspend fun insertBookmark(bookmark: Bookmark) =
        db.bibleDatabaseQueries.insertBookmark(
            id = bookmark.id,
            bookId = bookmark.bookId,
            bookName = bookmark.bookName,
            chapter = bookmark.chapter.toLong(),
            verseNumber = bookmark.verseNumber.toLong(),
            verseText = bookmark.verseText,
            createdAt = bookmark.createdAt,
        )

    actual suspend fun deleteBookmark(id: String) =
        db.bibleDatabaseQueries.deleteBookmark(id)

    actual suspend fun isBookmarked(id: String): Boolean =
        db.bibleDatabaseQueries.isBookmarked(id).executeAsOne() > 0
}
