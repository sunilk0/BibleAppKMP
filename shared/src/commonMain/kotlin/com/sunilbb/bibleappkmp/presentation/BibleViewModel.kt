package com.sunilbb.bibleappkmp.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sunilbb.bibleappkmp.data.bibleBooks
import com.sunilbb.bibleappkmp.domain.model.Bookmark
import com.sunilbb.bibleappkmp.domain.model.Verse
import com.sunilbb.bibleappkmp.domain.usecase.AddBookmarkUseCase
import com.sunilbb.bibleappkmp.domain.usecase.GetBookmarksUseCase
import com.sunilbb.bibleappkmp.domain.usecase.GetBooksUseCase
import com.sunilbb.bibleappkmp.domain.usecase.GetChaptersUseCase
import com.sunilbb.bibleappkmp.domain.usecase.GetVersesUseCase
import com.sunilbb.bibleappkmp.domain.usecase.IsBookmarkedUseCase
import com.sunilbb.bibleappkmp.domain.usecase.RefreshVersesUseCase
import com.sunilbb.bibleappkmp.domain.usecase.RemoveBookmarkUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BibleViewModel(
    private val getBooks: GetBooksUseCase,
    private val getChapters: GetChaptersUseCase,
    private val getVerses: GetVersesUseCase,
    private val getBookmarks: GetBookmarksUseCase,
    private val addBookmark: AddBookmarkUseCase,
    private val removeBookmark: RemoveBookmarkUseCase,
    private val refreshVerses: RefreshVersesUseCase,
    private val isBookmarked: IsBookmarkedUseCase,
) : ViewModel() {

    private var versesJob: Job? = null

    private val _booksState = MutableStateFlow(BooksUiState())
    val booksState: StateFlow<BooksUiState> = _booksState.asStateFlow()

    private val _chaptersState = MutableStateFlow(ChaptersUiState())
    val chaptersState: StateFlow<ChaptersUiState> = _chaptersState.asStateFlow()

    private val _readerState = MutableStateFlow(ReaderUiState())
    val readerState: StateFlow<ReaderUiState> = _readerState.asStateFlow()

    private val _bookmarksState = MutableStateFlow(BookmarksUiState())
    val bookmarksState: StateFlow<BookmarksUiState> = _bookmarksState.asStateFlow()

    private val _selectedBookName = MutableStateFlow("Bible")
    val selectedBookName: StateFlow<String> = _selectedBookName.asStateFlow()

    private val _bookmarkEvent = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val bookmarkEvent: SharedFlow<String> = _bookmarkEvent.asSharedFlow()

    init {
        loadBooks()
        getBookmarks()
            .onEach { bookmarks -> _bookmarksState.update { it.copy(bookmarks = bookmarks) } }
            .launchIn(viewModelScope)
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
            refreshVerses(bookId, chapter)
        }
        versesJob?.cancel()
        versesJob = getVerses(bookId, chapter)
            .onEach { verses ->
                _readerState.update { it.copy(isLoading = false, verses = verses) }
            }
            .launchIn(viewModelScope)
    }

    fun toggleBookmark(verse: Verse, bookName: String) {
        viewModelScope.launch {
            val bookmarked = isBookmarked(verse.id)
            if (bookmarked) {
                removeBookmark(verse.id)
                _bookmarkEvent.emit("Bookmark removed")
            } else {
                addBookmark(
                    Bookmark(
                        id = verse.id,
                        bookId = verse.bookId,
                        bookName = bookName,
                        chapter = verse.chapterId.substringAfterLast(".").toIntOrNull()
                            ?: _readerState.value.chapter,
                        verseNumber = verse.number,
                        verseText = verse.text,
                        createdAt = getCurrentTimeMillis(),
                    )
                )
                _bookmarkEvent.emit("Bookmarked")
            }
        }
    }

    fun deleteBookmark(id: String) {
        viewModelScope.launch { removeBookmark.invoke(id) }
    }
}

internal expect fun getCurrentTimeMillis(): Long
