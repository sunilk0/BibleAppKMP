package com.sunilbb.bibleappkmp.domain.usecase

import com.sunilbb.bibleappkmp.domain.repository.BibleRepository

class RemoveBookmarkUseCase(private val repository: BibleRepository) {
    suspend operator fun invoke(id: String) = repository.removeBookmark(id)
}
