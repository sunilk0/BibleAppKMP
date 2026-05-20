package com.sunilbb.bibleappkmp.data.repository

import com.sunilbb.bibleappkmp.data.bibleBooks
import com.sunilbb.bibleappkmp.data.chaptersForBook
import com.sunilbb.bibleappkmp.data.api.BibleApiService
import com.sunilbb.bibleappkmp.data.database.BibleLocalCache
import com.sunilbb.bibleappkmp.domain.model.Bookmark
import com.sunilbb.bibleappkmp.domain.model.Book
import com.sunilbb.bibleappkmp.domain.model.Chapter
import com.sunilbb.bibleappkmp.domain.model.Verse
import com.sunilbb.bibleappkmp.domain.repository.BibleRepository
import kotlinx.coroutines.flow.Flow

class BibleRepositoryImpl(
    private val api: BibleApiService,
    private val cache: BibleLocalCache,
) : BibleRepository {

    override suspend fun getBooks(): List<Book> {
        val cached = cache.getBooks()
        if (cached.isNotEmpty()) return cached
        cache.insertBooks(bibleBooks)
        return bibleBooks
    }

    override suspend fun getChapters(bookId: String): List<Chapter> {
        val cached = cache.getChapters(bookId)
        if (cached.isNotEmpty()) return cached
        val chapters = chaptersForBook(bookId)
        cache.insertChapters(chapters)
        return chapters
    }

    override fun getVersesFlow(bookId: String, chapter: Int): Flow<List<Verse>> =
        cache.getVersesFlow("$bookId.$chapter")

    override suspend fun searchPassage(reference: String): List<Verse> {
        val encoded = reference.replace(" ", "%20")
        return api.getPassage(encoded).verses.map { dto ->
            Verse(
                id = "${dto.bookId}.${dto.chapter}.${dto.verse}",
                chapterId = "${dto.bookId}.${dto.chapter}",
                bookId = dto.bookId,
                number = dto.verse,
                text = dto.text.trim(),
            )
        }
    }

    override fun getBookmarksFlow(): Flow<List<Bookmark>> = cache.getBookmarksFlow()
    override suspend fun addBookmark(bookmark: Bookmark) = cache.insertBookmark(bookmark)
    override suspend fun removeBookmark(id: String) = cache.deleteBookmark(id)
    override suspend fun isBookmarked(id: String): Boolean = cache.isBookmarked(id)

    suspend fun fetchAndCacheVerses(bookId: String, chapter: Int) {
        val chapterId = "$bookId.$chapter"
        try {
            val book = bibleBooks.find { it.id == bookId } ?: return
            val reference = "${book.name}%20$chapter"
            val response = api.getPassage(reference)
            val verses = response.verses.mapIndexed { index, dto ->
                Verse(
                    id = "$chapterId.${dto.verse}",
                    chapterId = chapterId,
                    bookId = bookId,
                    number = dto.verse,
                    text = dto.text.trim(),
                )
            }
            if (verses.isNotEmpty()) {
                cache.deleteVersesByChapter(chapterId)
                cache.insertVerses(verses, chapterId)
            }
        } catch (_: Exception) {
            // getVersesFlow emits whatever is already in cache
        }
    }
}
