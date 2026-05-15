package com.sunilbb.bibleappkmp

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.sunilbb.bibleappkmp.data.database.DatabaseDriverFactory

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport {
        App(DatabaseDriverFactory())
    }
}