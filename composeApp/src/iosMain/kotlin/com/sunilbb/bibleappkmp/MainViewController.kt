package com.sunilbb.bibleappkmp

import androidx.compose.ui.window.ComposeUIViewController
import com.sunilbb.bibleappkmp.data.database.DatabaseDriverFactory

fun MainViewController() = ComposeUIViewController { App(DatabaseDriverFactory()) }