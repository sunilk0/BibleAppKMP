package com.sunilbb.bibleappkmp.domain.model

data class Bookmark(
    val id: String,
    val bookId: String,
    val bookName: String,
    val chapter: Int,
    val verseNumber: Int,
    val verseText: String,
    val createdAt: Long,
)
