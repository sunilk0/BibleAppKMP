package com.sunilbb.bibleappkmp.presentation

import com.sunilbb.bibleappkmp.data.api.BibleApiService
import com.sunilbb.bibleappkmp.data.api.createBibleHttpClient
import com.sunilbb.bibleappkmp.data.database.DatabaseDriverFactory
import com.sunilbb.bibleappkmp.data.database.createLocalCache
import com.sunilbb.bibleappkmp.data.repository.BibleRepositoryImpl
import com.sunilbb.bibleappkmp.domain.usecase.AddBookmarkUseCase
import com.sunilbb.bibleappkmp.domain.usecase.GetBookmarksUseCase
import com.sunilbb.bibleappkmp.domain.usecase.GetBooksUseCase
import com.sunilbb.bibleappkmp.domain.usecase.GetChaptersUseCase
import com.sunilbb.bibleappkmp.domain.usecase.GetVersesUseCase
import com.sunilbb.bibleappkmp.domain.usecase.IsBookmarkedUseCase
import com.sunilbb.bibleappkmp.domain.usecase.RefreshVersesUseCase
import com.sunilbb.bibleappkmp.domain.usecase.RemoveBookmarkUseCase

fun createBibleViewModel(driverFactory: DatabaseDriverFactory): BibleViewModel {
    val client = createBibleHttpClient()
    val api = BibleApiService(client)
    val cache = createLocalCache(driverFactory)
    val repository = BibleRepositoryImpl(api, cache)
    return BibleViewModel(
        getBooks = GetBooksUseCase(repository),
        getChapters = GetChaptersUseCase(repository),
        getVerses = GetVersesUseCase(repository),
        getBookmarks = GetBookmarksUseCase(repository),
        addBookmark = AddBookmarkUseCase(repository),
        removeBookmark = RemoveBookmarkUseCase(repository),
        refreshVerses = RefreshVersesUseCase(repository),
        isBookmarked = IsBookmarkedUseCase(repository),
    )
}
