package com.sunilbb.bibleappkmp.domain.usecase

import com.sunilbb.bibleappkmp.testutil.FakeBibleRepository
import com.sunilbb.bibleappkmp.testutil.bookmark
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class AddBookmarkUseCaseTest {

    @Test
    fun `invoke delegates the bookmark to the repository`() = runTest {
        // Arrange
        val repository = FakeBibleRepository()
        val useCase = AddBookmarkUseCase(repository)
        val target = bookmark(id = "john.3.16")

        // Act
        useCase(target)

        // Assert
        assertEquals(listOf(target), repository.addedBookmarks)
    }

    @Test
    fun `invoke adding two distinct bookmarks records both`() = runTest {
        // Arrange
        val repository = FakeBibleRepository()
        val useCase = AddBookmarkUseCase(repository)

        // Act
        useCase(bookmark(id = "john.3.16"))
        useCase(bookmark(id = "psalms.23.1"))

        // Assert
        assertEquals(2, repository.addedBookmarks.size)
        assertEquals(listOf("john.3.16", "psalms.23.1"), repository.addedBookmarks.map { it.id })
    }

    @Test
    fun `invoke makes the bookmark observable on the bookmarks flow`() = runTest {
        // Arrange
        val repository = FakeBibleRepository()
        val useCase = AddBookmarkUseCase(repository)
        val target = bookmark(id = "john.3.16")

        // Act
        useCase(target)

        // Assert
        assertTrue(repository.isBookmarked("john.3.16"))
    }

    @Test
    fun `invoke propagates repository failures`() = runTest {
        // Arrange
        val repository = FakeBibleRepository().apply { errorToThrow = IllegalStateException("db full") }
        val useCase = AddBookmarkUseCase(repository)

        // Act + Assert
        assertFailsWith<IllegalStateException> { useCase(bookmark()) }
    }
}
