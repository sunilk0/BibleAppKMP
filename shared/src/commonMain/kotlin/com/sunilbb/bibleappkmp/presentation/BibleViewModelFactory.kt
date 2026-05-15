package com.sunilbb.bibleappkmp.presentation

import com.sunilbb.bibleappkmp.data.api.BibleApiService
import com.sunilbb.bibleappkmp.data.api.createBibleHttpClient
import com.sunilbb.bibleappkmp.data.database.DatabaseDriverFactory
import com.sunilbb.bibleappkmp.data.database.createLocalCache
import com.sunilbb.bibleappkmp.data.repository.BibleRepositoryImpl
import com.sunilbb.bibleappkmp.domain.usecase.GetBooksUseCase
import com.sunilbb.bibleappkmp.domain.usecase.GetChaptersUseCase
import com.sunilbb.bibleappkmp.domain.usecase.GetVersesUseCase

fun createBibleViewModel(driverFactory: DatabaseDriverFactory): BibleViewModel {
    val client = createBibleHttpClient()
    val api = BibleApiService(client)
    val cache = createLocalCache(driverFactory)
    val repository = BibleRepositoryImpl(api, cache)
    return BibleViewModel(
        getBooks = GetBooksUseCase(repository),
        getChapters = GetChaptersUseCase(repository),
        getVerses = GetVersesUseCase(repository),
        repository = repository,
    )
}
