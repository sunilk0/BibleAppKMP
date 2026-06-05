package com.sunilbb.bibleappkmp.domain.usecase

import com.sunilbb.bibleappkmp.domain.repository.BibleRepository

class RefreshVersesUseCase(private val repository: BibleRepository) {
    suspend operator fun invoke(bookId: String, chapter: Int) =
        repository.fetchAndCacheVerses(bookId, chapter)
}
