package com.sunilbb.bibleappkmp.data.database

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.sunilbb.bibleappkmp.db.BibleDatabase

actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver =
        AndroidSqliteDriver(BibleDatabase.Schema, context, "bible.db")
}
