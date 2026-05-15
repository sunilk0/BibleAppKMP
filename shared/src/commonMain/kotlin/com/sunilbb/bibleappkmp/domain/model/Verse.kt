package com.sunilbb.bibleappkmp.domain.model

data class Verse(
    val id: String,
    val chapterId: String,
    val bookId: String,
    val number: Int,
    val text: String,
)
