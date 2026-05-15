package com.sunilbb.bibleappkmp.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BooksResponseDto(
    val books: List<BookDto> = emptyList(),
)

@Serializable
data class BookDto(
    val id: String = "",
    val name: String = "",
    val abbreviation: String = "",
    val testament: String = "",
)
