package com.sunilbb.bibleappkmp.data.database

import com.sunilbb.bibleappkmp.db.BibleDatabase

actual fun createLocalCache(factory: DatabaseDriverFactory): BibleLocalCache =
    BibleLocalCache(BibleDatabase(factory.createDriver()))
