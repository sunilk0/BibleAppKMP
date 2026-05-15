package com.sunilbb.bibleappkmp.ui.screen.books

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sunilbb.bibleappkmp.domain.model.Book
import com.sunilbb.bibleappkmp.presentation.BooksUiState

@Composable
fun BooksScreen(
    state: BooksUiState,
    onBookClick: (Book) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        when {
            state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
            state.error != null -> Text(
                text = state.error!!,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.align(Alignment.Center).padding(16.dp),
            )
            else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(state.books, key = { it.id }) { book ->
                    ListItem(
                        headlineContent = { Text(book.name) },
                        supportingContent = { Text(book.testament) },
                        trailingContent = { Text(book.abbreviation, style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onBookClick(book) },
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}
