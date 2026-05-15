package com.sunilbb.bibleappkmp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform