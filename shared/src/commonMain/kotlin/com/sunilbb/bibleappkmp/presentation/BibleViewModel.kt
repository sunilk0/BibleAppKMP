package com.sunilbb.bibleappkmp.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sunilbb.bibleappkmp.data.bibleBooks
import com.sunilbb.bibleappkmp.data.repository.BibleRepositoryImpl
import com.sunilbb.bibleappkmp.domain.usecase.GetBooksUseCase
import com.sunilbb.bibleappkmp.domain.usecase.GetChaptersUseCase
import com.sunilbb.bibleappkmp.domain.usecase.GetVersesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BibleViewModel(
    private val getBooks: GetBooksUseCase,
    private val getChapters: GetChaptersUseCase,
    private val getVerses: GetVersesUseCase,
    private val repository: BibleRepositoryImpl,
) : ViewModel() {

    private val _booksState = MutableStateFlow(BooksUiState())
    val booksState: StateFlow<BooksUiState> = _booksState.asStateFlow()

    private val _chaptersState = MutableStateFlow(ChaptersUiState())
    val chaptersState: StateFlow<ChaptersUiState> = _chaptersState.asStateFlow()

    private val _readerState = MutableStateFlow(ReaderUiState())
    val readerState: StateFlow<ReaderUiState> = _readerState.asStateFlow()

    // Tracks the selected book name for the top bar title — avoids reading nav bundle on iOS
    private val _selectedBookName = MutableStateFlow("Bible")
    val selectedBookName: StateFlow<String> = _selectedBookName.asStateFlow()

    init {
        loadBooks()
    }

    fun loadBooks() {
        viewModelScope.launch {
            _booksState.update { it.copy(isLoading = true, error = null) }
            try {
                val books = getBooks()
                _booksState.update { it.copy(isLoading = false, books = books) }
            } catch (e: Exception) {
                _booksState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun loadChapters(bookId: String) {
        _selectedBookName.value = bibleBooks.find { it.id == bookId }?.name ?: bookId
        viewModelScope.launch {
            _chaptersState.update { it.copy(isLoading = true, error = null) }
            try {
                val chapters = getChapters(bookId)
                _chaptersState.update { it.copy(isLoading = false, chapters = chapters) }
            } catch (e: Exception) {
                _chaptersState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun loadVerses(bookId: String, chapter: Int) {
        _readerState.update { it.copy(isLoading = true, bookId = bookId, chapter = chapter, error = null) }
        viewModelScope.launch {
            repository.fetchAndCacheVerses(bookId, chapter)
        }
        getVerses(bookId, chapter)
            .onEach { verses ->
                _readerState.update { it.copy(isLoading = false, verses = verses) }
            }
            .launchIn(viewModelScope)
    }
}
