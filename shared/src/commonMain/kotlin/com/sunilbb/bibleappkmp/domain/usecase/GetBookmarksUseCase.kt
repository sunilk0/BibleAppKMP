package com.sunilbb.bibleappkmp.domain.usecase

import com.sunilbb.bibleappkmp.domain.model.Bookmark
import com.sunilbb.bibleappkmp.domain.repository.BibleRepository
import kotlinx.coroutines.flow.Flow

class GetBookmarksUseCase(private val repository: BibleRepository) {
    operator fun invoke(): Flow<List<Bookmark>> = repository.getBookmarksFlow()
}
