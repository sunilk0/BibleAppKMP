package com.sunilbb.bibleappkmp.domain.usecase

import com.sunilbb.bibleappkmp.testutil.FakeBibleRepository
import com.sunilbb.bibleappkmp.testutil.bookmark
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

class RemoveBookmarkUseCaseTest {

    @Test
    fun `invoke delegates the id to the repository`() = runTest {
        // Arrange
        val repository = FakeBibleRepository()
        val useCase = RemoveBookmarkUseCase(repository)

        // Act
        useCase("john.3.16")

        // Assert
        assertEquals(listOf("john.3.16"), repository.removedBookmarkIds)
    }

    @Test
    fun `invoke removes a previously added bookmark`() = runTest {
        // Arrange
        val repository = FakeBibleRepository()
        repository.addBookmark(bookmark(id = "john.3.16"))
        val useCase = RemoveBookmarkUseCase(repository)

        // Act
        useCase("john.3.16")

        // Assert
        assertFalse(repository.isBookmarked("john.3.16"))
    }

    @Test
    fun `invoke removing a missing id is a no-op that still succeeds`() = runTest {
        // Arrange
        val repository = FakeBibleRepository()
        val useCase = RemoveBookmarkUseCase(repository)

        // Act
        useCase("does.not.exist")

        // Assert
        assertEquals(listOf("does.not.exist"), repository.removedBookmarkIds)
    }

    @Test
    fun `invoke propagates repository failures`() = runTest {
        // Arrange
        val repository = FakeBibleRepository().apply { errorToThrow = RuntimeException("io error") }
        val useCase = RemoveBookmarkUseCase(repository)

        // Act + Assert
        assertFailsWith<RuntimeException> { useCase("john.3.16") }
    }
}
