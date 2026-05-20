package com.sunilbb.bibleappkmp.domain.usecase

import com.sunilbb.bibleappkmp.domain.model.Bookmark
import com.sunilbb.bibleappkmp.domain.repository.BibleRepository

class AddBookmarkUseCase(private val repository: BibleRepository) {
    suspend operator fun invoke(bookmark: Bookmark) = repository.addBookmark(bookmark)
}
