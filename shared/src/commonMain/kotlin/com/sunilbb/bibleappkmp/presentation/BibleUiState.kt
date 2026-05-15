package com.sunilbb.bibleappkmp.presentation

import com.sunilbb.bibleappkmp.domain.model.Book
import com.sunilbb.bibleappkmp.domain.model.Chapter
import com.sunilbb.bibleappkmp.domain.model.Verse

data class BooksUiState(
    val isLoading: Boolean = false,
    val books: List<Book> = emptyList(),
    val error: String? = null,
)

data class ChaptersUiState(
    val isLoading: Boolean = false,
    val chapters: List<Chapter> = emptyList(),
    val error: String? = null,
)

data class ReaderUiState(
    val isLoading: Boolean = false,
    val verses: List<Verse> = emptyList(),
    val bookId: String = "",
    val chapter: Int = 0,
    val error: String? = null,
)
