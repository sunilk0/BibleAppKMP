package com.sunilbb.bibleappkmp.presentation

internal actual fun getCurrentTimeMillis(): Long = js("Date.now()").unsafeCast<Double>().toLong()
