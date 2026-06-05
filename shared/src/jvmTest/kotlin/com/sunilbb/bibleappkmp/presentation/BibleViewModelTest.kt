package com.sunilbb.bibleappkmp.presentation

import com.sunilbb.bibleappkmp.data.repository.BibleRepositoryImpl
import com.sunilbb.bibleappkmp.domain.usecase.AddBookmarkUseCase
import com.sunilbb.bibleappkmp.domain.usecase.GetBookmarksUseCase
import com.sunilbb.bibleappkmp.domain.usecase.GetBooksUseCase
import com.sunilbb.bibleappkmp.domain.usecase.GetChaptersUseCase
import com.sunilbb.bibleappkmp.domain.usecase.GetVersesUseCase
import com.sunilbb.bibleappkmp.domain.usecase.IsBookmarkedUseCase
import com.sunilbb.bibleappkmp.domain.usecase.RefreshVersesUseCase
import com.sunilbb.bibleappkmp.domain.usecase.RemoveBookmarkUseCase
import com.sunilbb.bibleappkmp.testutil.inMemoryCache
import com.sunilbb.bibleappkmp.testutil.mockApiService
import com.sunilbb.bibleappkmp.testutil.verse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeout
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * State-transition tests for [BibleViewModel].
 *
 * The ViewModel is wired through use cases backed by a real [BibleRepositoryImpl] with an
 * in-memory SQLDelight cache and a Ktor MockEngine API. `viewModelScope` is pinned to an
 * [UnconfinedTestDispatcher] via Dispatchers.setMain so the init block and launched
 * coroutines run eagerly. Note that SQLDelight-backed flows (`getBookmarksFlow`,
 * `getVersesFlow`) map on `Dispatchers.Default`, which is outside the test scheduler;
 * assertions on flow-fed state therefore use [awaitState], a bounded real-time poll.
 */
class BibleViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val emptyVersesJson = """{"reference":"","verses":[],"text":"","translation_id":"","translation_name":""}"""

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(repository: BibleRepositoryImpl): BibleViewModel =
        BibleViewModel(
            getBooks = GetBooksUseCase(repository),
            getChapters = GetChaptersUseCase(repository),
            getVerses = GetVersesUseCase(repository),
            getBookmarks = GetBookmarksUseCase(repository),
            addBookmark = AddBookmarkUseCase(repository),
            removeBookmark = RemoveBookmarkUseCase(repository),
            refreshVerses = RefreshVersesUseCase(repository),
            isBookmarked = IsBookmarkedUseCase(repository),
        )

    private fun repo(): BibleRepositoryImpl =
        BibleRepositoryImpl(mockApiService(json = emptyVersesJson), inMemoryCache())

    /**
     * Polls [block] in real time until it returns true (default 2s budget). Used for state
     * that is fed by SQLDelight flows running on `Dispatchers.Default` rather than the
     * test scheduler — keeps the assertion deterministic without depending on timing.
     */
    private fun awaitTrue(timeoutMs: Long = 2_000, block: () -> Boolean) = runBlocking {
        withTimeout(timeoutMs) {
            while (!block()) delay(10)
        }
    }

    @Test
    fun `loadBooks moves books state from loading to loaded with the canon`() = runTest(dispatcher) {
        // Arrange
        val vm = viewModel(repo())

        // Act
        advanceUntilIdle() // let the init { loadBooks() } block finish

        // Assert
        val state = vm.booksState.value
        assertFalse(state.isLoading)
        assertEquals(66, state.books.size)
        assertNull(state.error)
    }

    @Test
    fun `loadChapters populates chapters state for the selected book`() = runTest(dispatcher) {
        // Arrange
        val vm = viewModel(repo())
        advanceUntilIdle()

        // Act
        vm.loadChapters("genesis")
        advanceUntilIdle()

        // Assert
        val state = vm.chaptersState.value
        assertFalse(state.isLoading)
        assertEquals(50, state.chapters.size)
    }

    @Test
    fun `loadChapters updates the selected book name from the catalogue`() = runTest(dispatcher) {
        // Arrange
        val vm = viewModel(repo())
        advanceUntilIdle()

        // Act
        vm.loadChapters("john")
        advanceUntilIdle()

        // Assert
        assertEquals("John", vm.selectedBookName.value)
    }

    @Test
    fun `loadChapters falls back to the bookId when the book is unknown`() = runTest(dispatcher) {
        // Arrange
        val vm = viewModel(repo())
        advanceUntilIdle()

        // Act
        vm.loadChapters("mystery")
        advanceUntilIdle()

        // Assert
        assertEquals("mystery", vm.selectedBookName.value)
    }

    @Test
    fun `loadVerses records the requested bookId and chapter on reader state`() = runTest(dispatcher) {
        // Arrange
        val vm = viewModel(repo())
        advanceUntilIdle()

        // Act
        vm.loadVerses("john", 3)
        advanceUntilIdle()

        // Assert — bookId/chapter are set synchronously by loadVerses
        val state = vm.readerState.value
        assertEquals("john", state.bookId)
        assertEquals(3, state.chapter)
    }

    @Test
    fun `loadVerses clears the loading flag once the verses flow emits`() = runTest(dispatcher) {
        // Arrange — seed one verse so the verses flow has something to emit
        val repository = repo()
        val vm = viewModel(repository)
        advanceUntilIdle()

        // Act
        vm.loadVerses("john", 3)
        advanceUntilIdle()

        // Assert — verses flow emits on Dispatchers.Default, so wait for the transition
        awaitTrue { !vm.readerState.value.isLoading }
        assertFalse(vm.readerState.value.isLoading)
    }

    @Test
    fun `toggleBookmark on an unbookmarked verse persists it through the repository`() = runTest(dispatcher) {
        // Arrange
        val repository = repo()
        val vm = viewModel(repository)
        advanceUntilIdle()

        // Act
        vm.toggleBookmark(verse(bookId = "john", chapter = 3, number = 16), bookName = "John")
        advanceUntilIdle()

        // Assert — repository is the deterministic source of truth
        assertTrue(runBlocking { repository.isBookmarked("john.3.16") })
    }

    @Test
    fun `toggleBookmark on an already bookmarked verse removes it`() = runTest(dispatcher) {
        // Arrange
        val repository = repo()
        val vm = viewModel(repository)
        advanceUntilIdle()
        val target = verse(bookId = "john", chapter = 3, number = 16)
        vm.toggleBookmark(target, bookName = "John") // first toggle adds
        advanceUntilIdle()
        assertTrue(runBlocking { repository.isBookmarked("john.3.16") })

        // Act
        vm.toggleBookmark(target, bookName = "John") // second toggle removes
        advanceUntilIdle()

        // Assert
        assertFalse(runBlocking { repository.isBookmarked("john.3.16") })
    }

    @Test
    fun `toggleBookmark surfaces the new bookmark on bookmarks state`() = runTest(dispatcher) {
        // Arrange
        val vm = viewModel(repo())
        advanceUntilIdle()

        // Act
        vm.toggleBookmark(verse(bookId = "john", chapter = 3, number = 16), bookName = "John")
        advanceUntilIdle()

        // Assert — bookmarksState is fed by a Dispatchers.Default flow; poll for it
        awaitTrue { vm.bookmarksState.value.bookmarks.isNotEmpty() }
        val saved = vm.bookmarksState.value.bookmarks.single()
        assertEquals("john.3.16", saved.id)
        assertEquals("John", saved.bookName)
        assertEquals(16, saved.verseNumber)
    }

    @Test
    fun `toggleBookmark derives the chapter from the verse chapterId`() = runTest(dispatcher) {
        // Arrange
        val vm = viewModel(repo())
        advanceUntilIdle()

        // Act — verse(chapter = 5) builds chapterId "psalms.5"
        vm.toggleBookmark(verse(bookId = "psalms", chapter = 5, number = 1), bookName = "Psalms")
        advanceUntilIdle()

        // Assert
        awaitTrue { vm.bookmarksState.value.bookmarks.isNotEmpty() }
        assertEquals(5, vm.bookmarksState.value.bookmarks.single().chapter)
    }

    @Test
    fun `removeBookmark by id clears it from bookmarks state`() = runTest(dispatcher) {
        // Arrange
        val repository = repo()
        val vm = viewModel(repository)
        advanceUntilIdle()
        vm.toggleBookmark(verse(bookId = "john", chapter = 3, number = 16), bookName = "John")
        advanceUntilIdle()
        awaitTrue { vm.bookmarksState.value.bookmarks.isNotEmpty() }

        // Act
        vm.deleteBookmark("john.3.16")
        advanceUntilIdle()

        // Assert
        assertFalse(runBlocking { repository.isBookmarked("john.3.16") })
        awaitTrue { vm.bookmarksState.value.bookmarks.isEmpty() }
        assertTrue(vm.bookmarksState.value.bookmarks.isEmpty())
    }

    @Test
    fun `bookmarkEvent emits Bookmarked when a verse is newly bookmarked`() = runTest(dispatcher) {
        // Arrange — start collecting before the toggle so the emission is observed
        val vm = viewModel(repo())
        advanceUntilIdle()
        val events = mutableListOf<String>()
        val collector = backgroundScope.launch { vm.bookmarkEvent.collect { events += it } }
        advanceUntilIdle()

        // Act
        vm.toggleBookmark(verse(bookId = "john", chapter = 3, number = 16), bookName = "John")
        advanceUntilIdle()

        // Assert
        awaitTrue { events.isNotEmpty() }
        assertEquals(listOf("Bookmarked"), events)
        collector.cancel()
    }

    @Test
    fun `bookmarkEvent emits a removed message when an existing bookmark is toggled off`() = runTest(dispatcher) {
        // Arrange
        val vm = viewModel(repo())
        advanceUntilIdle()
        val target = verse(bookId = "john", chapter = 3, number = 16)
        vm.toggleBookmark(target, bookName = "John") // add first
        advanceUntilIdle()
        val events = mutableListOf<String>()
        val collector = backgroundScope.launch { vm.bookmarkEvent.collect { events += it } }
        advanceUntilIdle()

        // Act
        vm.toggleBookmark(target, bookName = "John") // remove
        advanceUntilIdle()

        // Assert
        awaitTrue { events.isNotEmpty() }
        assertEquals(listOf("Bookmark removed"), events)
        collector.cancel()
    }
}
