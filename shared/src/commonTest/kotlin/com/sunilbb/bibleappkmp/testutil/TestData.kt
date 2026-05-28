package com.sunilbb.bibleappkmp.testutil

import com.sunilbb.bibleappkmp.domain.model.Book
import com.sunilbb.bibleappkmp.domain.model.Bookmark
import com.sunilbb.bibleappkmp.domain.model.Chapter
import com.sunilbb.bibleappkmp.domain.model.Verse

/** Deterministic builders for domain models used across tests. */

fun book(
    id: String = "john",
    name: String = "John",
    abbreviation: String = "Joh",
    testament: String = "New Testament",
) = Book(id = id, name = name, abbreviation = abbreviation, testament = testament)

fun chapter(
    bookId: String = "john",
    number: Int = 1,
) = Chapter(id = "$bookId.$number", bookId = bookId, number = number)

fun verse(
    bookId: String = "john",
    chapter: Int = 3,
    number: Int = 16,
    text: String = "For God so loved the world",
) = Verse(
    id = "$bookId.$chapter.$number",
    chapterId = "$bookId.$chapter",
    bookId = bookId,
    number = number,
    text = text,
)

fun bookmark(
    id: String = "john.3.16",
    bookId: String = "john",
    bookName: String = "John",
    chapter: Int = 3,
    verseNumber: Int = 16,
    verseText: String = "For God so loved the world",
    createdAt: Long = 1_000L,
) = Bookmark(
    id = id,
    bookId = bookId,
    bookName = bookName,
    chapter = chapter,
    verseNumber = verseNumber,
    verseText = verseText,
    createdAt = createdAt,
)
