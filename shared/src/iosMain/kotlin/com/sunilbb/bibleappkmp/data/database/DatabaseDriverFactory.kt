package com.sunilbb.bibleappkmp.data.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.sunilbb.bibleappkmp.db.BibleDatabase

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver =
        NativeSqliteDriver(BibleDatabase.Schema, "bible.db")
}
