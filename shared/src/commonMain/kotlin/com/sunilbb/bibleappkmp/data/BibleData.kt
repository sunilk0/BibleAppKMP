package com.sunilbb.bibleappkmp.data

import com.sunilbb.bibleappkmp.domain.model.Book
import com.sunilbb.bibleappkmp.domain.model.Chapter

private const val OLD_TESTAMENT = "Old Testament"
private const val NEW_TESTAMENT = "New Testament"

val bibleBooks: List<Book> = listOf(
    Book("genesis", "Genesis", "Gen", OLD_TESTAMENT),
    Book("exodus", "Exodus", "Exo", OLD_TESTAMENT),
    Book("leviticus", "Leviticus", "Lev", OLD_TESTAMENT),
    Book("numbers", "Numbers", "Num", OLD_TESTAMENT),
    Book("deuteronomy", "Deuteronomy", "Deu", OLD_TESTAMENT),
    Book("joshua", "Joshua", "Jos", OLD_TESTAMENT),
    Book("judges", "Judges", "Jdg", OLD_TESTAMENT),
    Book("ruth", "Ruth", "Rut", OLD_TESTAMENT),
    Book("1samuel", "1 Samuel", "1Sa", OLD_TESTAMENT),
    Book("2samuel", "2 Samuel", "2Sa", OLD_TESTAMENT),
    Book("1kings", "1 Kings", "1Ki", OLD_TESTAMENT),
    Book("2kings", "2 Kings", "2Ki", OLD_TESTAMENT),
    Book("1chronicles", "1 Chronicles", "1Ch", OLD_TESTAMENT),
    Book("2chronicles", "2 Chronicles", "2Ch", OLD_TESTAMENT),
    Book("ezra", "Ezra", "Ezr", OLD_TESTAMENT),
    Book("nehemiah", "Nehemiah", "Neh", OLD_TESTAMENT),
    Book("esther", "Esther", "Est", OLD_TESTAMENT),
    Book("job", "Job", "Job", OLD_TESTAMENT),
    Book("psalms", "Psalms", "Psa", OLD_TESTAMENT),
    Book("proverbs", "Proverbs", "Pro", OLD_TESTAMENT),
    Book("ecclesiastes", "Ecclesiastes", "Ecc", OLD_TESTAMENT),
    Book("songofsolomon", "Song of Solomon", "Sol", OLD_TESTAMENT),
    Book("isaiah", "Isaiah", "Isa", OLD_TESTAMENT),
    Book("jeremiah", "Jeremiah", "Jer", OLD_TESTAMENT),
    Book("lamentations", "Lamentations", "Lam", OLD_TESTAMENT),
    Book("ezekiel", "Ezekiel", "Eze", OLD_TESTAMENT),
    Book("daniel", "Daniel", "Dan", OLD_TESTAMENT),
    Book("hosea", "Hosea", "Hos", OLD_TESTAMENT),
    Book("joel", "Joel", "Joe", OLD_TESTAMENT),
    Book("amos", "Amos", "Amo", OLD_TESTAMENT),
    Book("obadiah", "Obadiah", "Oba", OLD_TESTAMENT),
    Book("jonah", "Jonah", "Jon", OLD_TESTAMENT),
    Book("micah", "Micah", "Mic", OLD_TESTAMENT),
    Book("nahum", "Nahum", "Nah", OLD_TESTAMENT),
    Book("habakkuk", "Habakkuk", "Hab", OLD_TESTAMENT),
    Book("zephaniah", "Zephaniah", "Zep", OLD_TESTAMENT),
    Book("haggai", "Haggai", "Hag", OLD_TESTAMENT),
    Book("zechariah", "Zechariah", "Zec", OLD_TESTAMENT),
    Book("malachi", "Malachi", "Mal", OLD_TESTAMENT),
    Book("matthew", "Matthew", "Mat", NEW_TESTAMENT),
    Book("mark", "Mark", "Mar", NEW_TESTAMENT),
    Book("luke", "Luke", "Luk", NEW_TESTAMENT),
    Book("john", "John", "Joh", NEW_TESTAMENT),
    Book("acts", "Acts", "Act", NEW_TESTAMENT),
    Book("romans", "Romans", "Rom", NEW_TESTAMENT),
    Book("1corinthians", "1 Corinthians", "1Co", NEW_TESTAMENT),
    Book("2corinthians", "2 Corinthians", "2Co", NEW_TESTAMENT),
    Book("galatians", "Galatians", "Gal", NEW_TESTAMENT),
    Book("ephesians", "Ephesians", "Eph", NEW_TESTAMENT),
    Book("philippians", "Philippians", "Php", NEW_TESTAMENT),
    Book("colossians", "Colossians", "Col", NEW_TESTAMENT),
    Book("1thessalonians", "1 Thessalonians", "1Th", NEW_TESTAMENT),
    Book("2thessalonians", "2 Thessalonians", "2Th", NEW_TESTAMENT),
    Book("1timothy", "1 Timothy", "1Ti", NEW_TESTAMENT),
    Book("2timothy", "2 Timothy", "2Ti", NEW_TESTAMENT),
    Book("titus", "Titus", "Tit", NEW_TESTAMENT),
    Book("philemon", "Philemon", "Phm", NEW_TESTAMENT),
    Book("hebrews", "Hebrews", "Heb", NEW_TESTAMENT),
    Book("james", "James", "Jam", NEW_TESTAMENT),
    Book("1peter", "1 Peter", "1Pe", NEW_TESTAMENT),
    Book("2peter", "2 Peter", "2Pe", NEW_TESTAMENT),
    Book("1john", "1 John", "1Jo", NEW_TESTAMENT),
    Book("2john", "2 John", "2Jo", NEW_TESTAMENT),
    Book("3john", "3 John", "3Jo", NEW_TESTAMENT),
    Book("jude", "Jude", "Jud", NEW_TESTAMENT),
    Book("revelation", "Revelation", "Rev", NEW_TESTAMENT),
)

private val chapterCounts = mapOf(
    "genesis" to 50, "exodus" to 40, "leviticus" to 27, "numbers" to 36,
    "deuteronomy" to 34, "joshua" to 24, "judges" to 21, "ruth" to 4,
    "1samuel" to 31, "2samuel" to 24, "1kings" to 22, "2kings" to 25,
    "1chronicles" to 29, "2chronicles" to 36, "ezra" to 10, "nehemiah" to 13,
    "esther" to 10, "job" to 42, "psalms" to 150, "proverbs" to 31,
    "ecclesiastes" to 12, "songofsolomon" to 8, "isaiah" to 66, "jeremiah" to 52,
    "lamentations" to 5, "ezekiel" to 48, "daniel" to 12, "hosea" to 14,
    "joel" to 3, "amos" to 9, "obadiah" to 1, "jonah" to 4, "micah" to 7,
    "nahum" to 3, "habakkuk" to 3, "zephaniah" to 3, "haggai" to 2,
    "zechariah" to 14, "malachi" to 4, "matthew" to 28, "mark" to 16,
    "luke" to 24, "john" to 21, "acts" to 28, "romans" to 16,
    "1corinthians" to 16, "2corinthians" to 13, "galatians" to 6, "ephesians" to 6,
    "philippians" to 4, "colossians" to 4, "1thessalonians" to 5, "2thessalonians" to 3,
    "1timothy" to 6, "2timothy" to 4, "titus" to 3, "philemon" to 1,
    "hebrews" to 13, "james" to 5, "1peter" to 5, "2peter" to 3,
    "1john" to 5, "2john" to 1, "3john" to 1, "jude" to 1, "revelation" to 22,
)

fun chaptersForBook(bookId: String): List<Chapter> {
    val count = chapterCounts[bookId] ?: return emptyList()
    return (1..count).map { num ->
        Chapter(id = "$bookId.$num", bookId = bookId, number = num)
    }
}
