package com.sunilbb.bibleappkmp.domain.usecase

import com.sunilbb.bibleappkmp.domain.model.Book
import com.sunilbb.bibleappkmp.domain.repository.BibleRepository

class GetBooksUseCase(private val repository: BibleRepository) {
    suspend operator fun invoke(): List<Book> = repository.getBooks()
}
