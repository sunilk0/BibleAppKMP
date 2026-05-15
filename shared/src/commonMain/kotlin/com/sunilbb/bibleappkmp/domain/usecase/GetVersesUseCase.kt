package com.sunilbb.bibleappkmp.domain.usecase

import com.sunilbb.bibleappkmp.domain.model.Verse
import com.sunilbb.bibleappkmp.domain.repository.BibleRepository
import kotlinx.coroutines.flow.Flow

class GetVersesUseCase(private val repository: BibleRepository) {
    operator fun invoke(bookId: String, chapter: Int): Flow<List<Verse>> =
        repository.getVersesFlow(bookId, chapter)
}
