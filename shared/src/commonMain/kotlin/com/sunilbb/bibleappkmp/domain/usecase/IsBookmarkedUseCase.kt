package com.sunilbb.bibleappkmp.domain.usecase

import com.sunilbb.bibleappkmp.domain.repository.BibleRepository

class IsBookmarkedUseCase(private val repository: BibleRepository) {
    suspend operator fun invoke(id: String): Boolean = repository.isBookmarked(id)
}
