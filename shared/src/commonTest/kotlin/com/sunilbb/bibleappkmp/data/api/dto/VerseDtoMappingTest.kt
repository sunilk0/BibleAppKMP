package com.sunilbb.bibleappkmp.data.api.dto

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Additional mapping tests for [VerseDto] and [VersesResponseDto] defaults.
 */
class VerseDtoMappingTest {

    @Test
    fun `VerseDto defaults produce zero values and empty strings`() {
        // Arrange + Act
        val dto = VerseDto()

        // Assert
        assertEquals("", dto.bookId)
        assertEquals("", dto.bookName)
        assertEquals(0, dto.chapter)
        assertEquals(0, dto.verse)
        assertEquals("", dto.text)
    }

    @Test
    fun `VersesResponseDto defaults to empty verses list`() {
        // Arrange + Act
        val dto = VersesResponseDto()

        // Assert
        assertTrue(dto.verses.isEmpty())
        assertEquals("", dto.reference)
        assertEquals("", dto.translationId)
        assertEquals("", dto.translationName)
    }

    @Test
    fun `ChapterDto defaults to empty strings and zero number`() {
        // Arrange + Act
        val dto = ChapterDto()

        // Assert
        assertEquals("", dto.id)
        assertEquals("", dto.bookId)
        assertEquals(0, dto.number)
    }

    @Test
    fun `BookDto defaults to empty strings`() {
        // Arrange + Act
        val dto = BookDto()

        // Assert
        assertEquals("", dto.id)
        assertEquals("", dto.name)
        assertEquals("", dto.abbreviation)
        assertEquals("", dto.testament)
    }

    @Test
    fun `BooksResponseDto defaults to empty books list`() {
        val dto = BooksResponseDto()
        assertTrue(dto.books.isEmpty())
    }

    @Test
    fun `ChaptersResponseDto defaults to empty chapters list`() {
        val dto = ChaptersResponseDto()
        assertTrue(dto.chapters.isEmpty())
    }

    @Test
    fun `VerseDto toDomain via BookDto toDomain preserves individual fields`() {
        // Arrange
        val bookDto = BookDto(id = "psalms", name = "Psalms", abbreviation = "Psa", testament = "Old Testament")

        // Act
        val domain = bookDto.toDomain()

        // Assert
        assertEquals("psalms", domain.id)
        assertEquals("Old Testament", domain.testament)
    }
}
