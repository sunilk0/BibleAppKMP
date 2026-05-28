package com.sunilbb.bibleappkmp.domain.usecase

import com.sunilbb.bibleappkmp.testutil.FakeBibleRepository
import com.sunilbb.bibleappkmp.testutil.book
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class GetBooksUseCaseTest {

    @Test
    fun `invoke returns the books supplied by the repository`() = runTest {
        // Arrange
        val repository = FakeBibleRepository()
        val expected = listOf(book(id = "genesis"), book(id = "john"))
        repository.booksResult = expected
        val useCase = GetBooksUseCase(repository)

        // Act
        val result = useCase()

        // Assert
        assertEquals(expected, result)
    }

    @Test
    fun `invoke returns an empty list when the repository has no books`() = runTest {
        // Arrange
        val repository = FakeBibleRepository()
        val useCase = GetBooksUseCase(repository)

        // Act
        val result = useCase()

        // Assert
        assertTrue(result.isEmpty())
    }

    @Test
    fun `invoke calls the repository exactly once per invocation`() = runTest {
        // Arrange
        val repository = FakeBibleRepository()
        val useCase = GetBooksUseCase(repository)

        // Act
        useCase()

        // Assert
        assertEquals(1, repository.getBooksCallCount)
    }

    @Test
    fun `invoke propagates repository failures`() = runTest {
        // Arrange
        val repository = FakeBibleRepository().apply { errorToThrow = RuntimeException("network down") }
        val useCase = GetBooksUseCase(repository)

        // Act + Assert
        assertFailsWith<RuntimeException> { useCase() }
    }
}
