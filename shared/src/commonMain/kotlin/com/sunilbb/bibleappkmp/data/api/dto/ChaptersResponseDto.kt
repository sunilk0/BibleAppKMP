package com.sunilbb.bibleappkmp.data.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class ChaptersResponseDto(
    val chapters: List<ChapterDto> = emptyList(),
)

@Serializable
data class ChapterDto(
    val id: String = "",
    @kotlinx.serialization.SerialName("book_id")
    val bookId: String = "",
    val number: Int = 0,
)
