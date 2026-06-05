package com.sunilbb.bibleappkmp

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sunilbb.bibleappkmp.data.database.DatabaseDriverFactory
import com.sunilbb.bibleappkmp.presentation.createBibleViewModel
import com.sunilbb.bibleappkmp.ui.screen.books.BooksScreen
import com.sunilbb.bibleappkmp.ui.screen.bookmarks.BookmarksScreen
import com.sunilbb.bibleappkmp.ui.screen.chapters.ChaptersScreen
import com.sunilbb.bibleappkmp.ui.screen.reader.ReaderScreen
import com.sunilbb.bibleappkmp.ui.screen.splash.SplashScreen
import com.sunilbb.bibleappkmp.ui.theme.BibleTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(driverFactory: DatabaseDriverFactory) {
    BibleTheme {
        var showSplash by remember { mutableStateOf(true) }
        if (showSplash) {
            SplashScreen(onTimeout = { showSplash = false })
            return@BibleTheme
        }

        val navController = rememberNavController()
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination?.route

        val viewModel = remember { createBibleViewModel(driverFactory) }

        val selectedBookName by viewModel.selectedBookName.collectAsState()
        val readerState by viewModel.readerState.collectAsState()
        val bookmarksState by viewModel.bookmarksState.collectAsState()

        val snackbarHostState = remember { SnackbarHostState() }

        LaunchedEffect(Unit) {
            viewModel.bookmarkEvent.collect { message ->
                snackbarHostState.showSnackbar(message)
            }
        }

        val title = when {
            currentRoute?.startsWith("reader/") == true ->
                "$selectedBookName ${readerState.chapter}"
            currentRoute?.startsWith("chapters/") == true -> selectedBookName
            currentRoute == "bookmarks" -> "Bookmarks"
            else -> "Bible"
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(title) },
                    navigationIcon = {
                        if (currentRoute != "books") {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                )
                            }
                        }
                    },
                    actions = {
                        if (currentRoute == "books") {
                            IconButton(onClick = { navController.navigate("bookmarks") }) {
                                Icon(
                                    imageVector = Icons.Default.Bookmarks,
                                    contentDescription = "Bookmarks",
                                )
                            }
                        }
                    },
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "books",
                modifier = Modifier.fillMaxSize().padding(innerPadding),
            ) {
                composable("books") {
                    val state by viewModel.booksState.collectAsState()
                    BooksScreen(
                        state = state,
                        onBookClick = { book ->
                            viewModel.loadChapters(book.id)
                            navController.navigate("chapters/${book.id}")
                        },
                    )
                }
                composable("chapters/{bookId}") { backStack ->
                    val bookId = backStack.savedStateHandle.get<String>("bookId") ?: return@composable
                    LaunchedEffect(bookId) {
                        viewModel.loadChapters(bookId)
                    }
                    val state by viewModel.chaptersState.collectAsState()
                    ChaptersScreen(
                        state = state,
                        onChapterClick = { chapter ->
                            navController.navigate("reader/${chapter.bookId}/${chapter.number}")
                        },
                    )
                }
                composable("reader/{bookId}/{chapter}") { backStack ->
                    val bookId = backStack.savedStateHandle.get<String>("bookId") ?: return@composable
                    val chapter = backStack.savedStateHandle.get<String>("chapter")?.toIntOrNull() ?: return@composable
                    LaunchedEffect(bookId, chapter) {
                        viewModel.loadVerses(bookId, chapter)
                    }
                    val isBookmarked: (String) -> Boolean = remember(bookmarksState.bookmarks) {
                        val ids = bookmarksState.bookmarks.mapTo(HashSet()) { it.id }
                        val predicate: (String) -> Boolean = { verseId -> verseId in ids }
                        predicate
                    }
                    ReaderScreen(
                        state = readerState,
                        onToggleBookmark = { verse ->
                            viewModel.toggleBookmark(verse, selectedBookName)
                        },
                        isBookmarked = isBookmarked,
                    )
                }
                composable("bookmarks") {
                    BookmarksScreen(
                        state = bookmarksState,
                        onBookmarkClick = { bookId, chapter ->
                            viewModel.loadChapters(bookId)
                            navController.navigate("reader/$bookId/$chapter")
                        },
                        onDeleteClick = { id -> viewModel.deleteBookmark(id) },
                    )
                }
            }
        }
    }
}
