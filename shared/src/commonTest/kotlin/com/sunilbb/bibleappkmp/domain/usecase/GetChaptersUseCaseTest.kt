package com.sunilbb.bibleappkmp.domain.usecase

import com.sunilbb.bibleappkmp.testutil.FakeBibleRepository
import com.sunilbb.bibleappkmp.testutil.chapter
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class GetChaptersUseCaseTest {

    @Test
    fun `invoke returns the chapters supplied by the repository`() = runTest {
        // Arrange
        val repository = FakeBibleRepository()
        val expected = listOf(chapter(number = 1), chapter(number = 2))
        repository.chaptersResult = expected
        val useCase = GetChaptersUseCase(repository)

        // Act
        val result = useCase("john")

        // Assert
        assertEquals(expected, result)
    }

    @Test
    fun `invoke forwards the requested bookId to the repository`() = runTest {
        // Arrange
        val repository = FakeBibleRepository()
        val useCase = GetChaptersUseCase(repository)

        // Act
        useCase("psalms")

        // Assert
        assertEquals("psalms", repository.lastRequestedChaptersBookId)
    }

    @Test
    fun `invoke returns an empty list for a book with no chapters`() = runTest {
        // Arrange
        val repository = FakeBibleRepository()
        val useCase = GetChaptersUseCase(repository)

        // Act
        val result = useCase("unknown")

        // Assert
        assertTrue(result.isEmpty())
    }

    @Test
    fun `invoke propagates repository failures`() = runTest {
        // Arrange
        val repository = FakeBibleRepository().apply { errorToThrow = RuntimeException("timeout") }
        val useCase = GetChaptersUseCase(repository)

        // Act + Assert
        assertFailsWith<RuntimeException> { useCase("john") }
    }
}
