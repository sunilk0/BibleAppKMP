package com.sunilbb.bibleappkmp.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VersesResponseDto(
    val reference: String = "",
    val verses: List<VerseDto> = emptyList(),
    val text: String = "",
    @SerialName("translation_id")
    val translationId: String = "",
    @SerialName("translation_name")
    val translationName: String = "",
)

@Serializable
data class VerseDto(
    @SerialName("book_id")
    val bookId: String = "",
    @SerialName("book_name")
    val bookName: String = "",
    val chapter: Int = 0,
    val verse: Int = 0,
    val text: String = "",
)
