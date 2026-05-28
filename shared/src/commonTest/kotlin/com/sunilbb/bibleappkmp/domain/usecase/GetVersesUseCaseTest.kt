package com.sunilbb.bibleappkmp.domain.usecase

import com.sunilbb.bibleappkmp.testutil.FakeBibleRepository
import com.sunilbb.bibleappkmp.testutil.verse
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetVersesUseCaseTest {

    @Test
    fun `invoke emits the verses held by the repository`() = runTest {
        // Arrange
        val repository = FakeBibleRepository()
        val expected = listOf(verse(number = 1), verse(number = 2))
        repository.versesResult = expected
        val useCase = GetVersesUseCase(repository)

        // Act
        val emitted = useCase("john", 3).first()

        // Assert
        assertEquals(expected, emitted)
    }

    @Test
    fun `invoke emits an empty list when the chapter has no cached verses`() = runTest {
        // Arrange
        val repository = FakeBibleRepository()
        val useCase = GetVersesUseCase(repository)

        // Act
        val emitted = useCase("john", 3).first()

        // Assert
        assertTrue(emitted.isEmpty())
    }
}
