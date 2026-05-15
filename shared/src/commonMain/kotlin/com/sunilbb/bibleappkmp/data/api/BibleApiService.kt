package com.sunilbb.bibleappkmp.data.api

import com.sunilbb.bibleappkmp.data.api.dto.BooksResponseDto
import com.sunilbb.bibleappkmp.data.api.dto.ChaptersResponseDto
import com.sunilbb.bibleappkmp.data.api.dto.VersesResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class BibleApiService(private val client: HttpClient) {

    suspend fun getBooks(): BooksResponseDto =
        client.get("/books").body()

    suspend fun getChapters(bookId: String): ChaptersResponseDto =
        client.get("/chapters/$bookId").body()

    suspend fun getPassage(reference: String): VersesResponseDto =
        client.get("/$reference").body()
}
