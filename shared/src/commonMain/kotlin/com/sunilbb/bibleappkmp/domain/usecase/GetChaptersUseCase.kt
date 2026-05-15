package com.sunilbb.bibleappkmp.domain.usecase

import com.sunilbb.bibleappkmp.domain.model.Chapter
import com.sunilbb.bibleappkmp.domain.repository.BibleRepository

class GetChaptersUseCase(private val repository: BibleRepository) {
    suspend operator fun invoke(bookId: String): List<Chapter> = repository.getChapters(bookId)
}
