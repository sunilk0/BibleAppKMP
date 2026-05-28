package com.sunilbb.bibleappkmp.domain.usecase

import com.sunilbb.bibleappkmp.testutil.FakeBibleRepository
import com.sunilbb.bibleappkmp.testutil.bookmark
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetBookmarksUseCaseTest {

    @Test
    fun `invoke emits an empty list when there are no bookmarks`() = runTest {
        // Arrange
        val repository = FakeBibleRepository()
        val useCase = GetBookmarksUseCase(repository)

        // Act
        val emitted = useCase().first()

        // Assert
        assertTrue(emitted.isEmpty())
    }

    @Test
    fun `invoke emits the bookmarks currently held by the repository`() = runTest {
        // Arrange
        val repository = FakeBibleRepository()
        val expected = listOf(bookmark(id = "john.3.16"), bookmark(id = "psalms.23.1"))
        repository.emitBookmarks(expected)
        val useCase = GetBookmarksUseCase(repository)

        // Act
        val emitted = useCase().first()

        // Assert
        assertEquals(expected, emitted)
    }

    @Test
    fun `invoke reflects bookmarks added after subscription`() = runTest {
        // Arrange
        val repository = FakeBibleRepository()
        val useCase = GetBookmarksUseCase(repository)

        // Act
        repository.addBookmark(bookmark(id = "john.3.16"))
        val emitted = useCase().first()

        // Assert
        assertEquals(listOf("john.3.16"), emitted.map { it.id })
    }
}
