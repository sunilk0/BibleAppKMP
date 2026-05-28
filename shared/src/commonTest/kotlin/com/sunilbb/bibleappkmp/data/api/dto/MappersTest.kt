package com.sunilbb.bibleappkmp.data.api.dto

import kotlin.test.Test
import kotlin.test.assertEquals

class MappersTest {

    @Test
    fun `BookDto toDomain copies every field`() {
        // Arrange
        val dto = BookDto(id = "john", name = "John", abbreviation = "Joh", testament = "New Testament")

        // Act
        val domain = dto.toDomain()

        // Assert
        assertEquals("john", domain.id)
        assertEquals("John", domain.name)
        assertEquals("Joh", domain.abbreviation)
        assertEquals("New Testament", domain.testament)
    }

    @Test
    fun `ChapterDto toDomain keeps bookId when it is present`() {
        // Arrange
        val dto = ChapterDto(id = "john.1", bookId = "john", number = 1)

        // Act
        val domain = dto.toDomain(fallbackBookId = "fallback")

        // Assert
        assertEquals("john", domain.bookId)
    }

    @Test
    fun `ChapterDto toDomain uses the fallback bookId when bookId is blank`() {
        // Arrange
        val dto = ChapterDto(id = "john.1", bookId = "", number = 1)

        // Act
        val domain = dto.toDomain(fallbackBookId = "john")

        // Assert
        assertEquals("john", domain.bookId)
    }

    @Test
    fun `ChapterDto toDomain copies id and number`() {
        // Arrange
        val dto = ChapterDto(id = "john.5", bookId = "john", number = 5)

        // Act
        val domain = dto.toDomain(fallbackBookId = "john")

        // Assert
        assertEquals("john.5", domain.id)
        assertEquals(5, domain.number)
    }
}
