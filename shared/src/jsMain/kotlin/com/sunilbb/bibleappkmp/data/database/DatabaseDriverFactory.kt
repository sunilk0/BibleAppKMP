package com.sunilbb.bibleappkmp.data.database

import app.cash.sqldelight.db.SqlDriver

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver = throw UnsupportedOperationException("No SQLDelight driver for JS")
}
