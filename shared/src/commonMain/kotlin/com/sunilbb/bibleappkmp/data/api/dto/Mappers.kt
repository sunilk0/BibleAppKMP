package com.sunilbb.bibleappkmp.data.api.dto

import com.sunilbb.bibleappkmp.domain.model.Book
import com.sunilbb.bibleappkmp.domain.model.Chapter

fun BookDto.toDomain() = Book(
    id = id,
    name = name,
    abbreviation = abbreviation,
    testament = testament,
)

fun ChapterDto.toDomain(fallbackBookId: String) = Chapter(
    id = id,
    bookId = bookId.ifBlank { fallbackBookId },
    number = number,
)
