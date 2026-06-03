# Build a Real KMP App from Scratch — Part 2: Clean Architecture & the Networking Layer

*Series: Building a Bible Reader app that runs on Android, iOS, and the web — using one shared Kotlin codebase.*

---

In [Part 1](./part-1-what-is-kmp.md) we set the stage: what makes Kotlin Multiplatform different, the three-module structure, how to declare targets, and the `expect`/`actual` pattern. That was the skeleton. Now we put organs in it.

This is Part 2, and we're going deep into the `:shared` module to build the part that actually makes the app work: the **Clean Architecture foundation** — domain models, the repository contract, use cases, and the **Ktor networking layer** that talks to a live Bible API.

By the end, our shared module will fetch real Bible passages from the internet — in a way that compiles to Android, iOS, *and* the browser without a single line of duplicated logic. Let's get into it.

---

## Why Clean Architecture in a Shared Module

In a normal Android app, Clean Architecture is a nice-to-have. In a KMP app, it's load-bearing.

Here's why. The whole point of `:shared` is that it compiles to every platform — but not every Kotlin API exists everywhere. `android.os.Build` isn't on iOS, `UIDevice` isn't on Android, and neither exists in a browser. The moment a framework dependency leaks into the wrong place, your shared code stops being shared. Clean Architecture maps perfectly onto this constraint. We split the shared module into three layers:

```
shared/src/commonMain/kotlin/com/sunilbb/bibleappkmp/
├── domain/          ← pure Kotlin. Models, contracts, use cases. Zero framework deps.
│   ├── model/
│   ├── repository/
│   └── usecase/
├── data/            ← the "how". Ktor, DTOs, mapping, caching.
│   ├── api/
│   ├── database/
│   └── repository/
└── presentation/    ← ViewModel + UI state, shared across platforms.
```

The rule that keeps this honest: **`domain` depends on nothing.** No Ktor, no SQLDelight, no Android imports. It's plain Kotlin. The `data` layer depends on `domain` (it implements its contracts), and `presentation` depends on `domain` too. Dependencies point *inward*, toward the pure core.

This isn't architecture for architecture's sake. It's the thing that makes "write your business logic once" actually true.

---

## The Domain Models

Let's start at the center. Domain models are the vocabulary of the app — what a "book", a "chapter", a "verse" *is*, independent of where the data came from or where it's displayed.

```kotlin
// domain/model/ — plain Kotlin, one file per type
data class Book(val id: String, val name: String, val abbreviation: String, val testament: String)

data class Chapter(val id: String, val bookId: String, val number: Int)

data class Verse(val id: String, val chapterId: String, val bookId: String, val number: Int, val text: String)

data class Bookmark(
    val id: String,
    val bookId: String,
    val bookName: String,
    val chapter: Int,
    val verseNumber: Int,
    val verseText: String,
    val createdAt: Long,
)
```

Notice what's *not* here. No `@Serializable`. No SQLDelight annotations. No nullable junk to accommodate a flaky API. These are clean, framework-free Kotlin data classes — the stable heart of the app that the rest of the code orbits. When the API changes its JSON shape (and it will), these don't move. That's the payoff.

---

## The Repository Interface — The Contract

The domain layer declares *what* it needs, not *how* it's done. That declaration is the repository interface:

```kotlin
// domain/repository/BibleRepository.kt
interface BibleRepository {
    suspend fun getBooks(): List<Book>
    suspend fun getChapters(bookId: String): List<Chapter>
    fun getVersesFlow(bookId: String, chapter: Int): Flow<List<Verse>>
    suspend fun searchPassage(reference: String): List<Verse>

    fun getBookmarksFlow(): Flow<List<Bookmark>>
    suspend fun addBookmark(bookmark: Bookmark)
    suspend fun removeBookmark(id: String)
    suspend fun isBookmarked(id: String): Boolean
}
```

There's a deliberate choice baked into these return types. Some are `suspend fun` returning a plain `List` — **one-shot reads**. Ask once, get an answer, done; the list of books doesn't change while you're staring at it.

Others return `Flow<List<…>>` — **reactive streams**. Verses and bookmarks return a `Flow` because they can *change underneath you*: a verse arrives from the network and gets cached, a bookmark gets toggled on another screen. With a `Flow`, the UI re-renders automatically when the data shifts. We don't poll, we subscribe.

This interface is the contract the *domain* owns. The data layer's job is to obey it.

---

## The Ktor Networking Layer

Now we cross into the `data` layer and the part everyone wants to see: actually talking to the internet.

We're using [bible-api.com](https://bible-api.com), a free, open API that returns passage text by reference. Ask it for `Genesis 1` and it hands you back the verses.

First, the `HttpClient`. In KMP, `HttpClient` is configured in `commonMain` once, and each platform plugs in its own engine (OkHttp on Android, Darwin/URLSession on iOS, Fetch in the browser — we wired those up in Part 1). The *configuration* is shared:

```kotlin
// data/api/BibleApiClient.kt
fun createBibleHttpClient(): HttpClient = HttpClient {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true; isLenient = true })
    }
    install(Logging) { level = LogLevel.INFO }
    install(HttpRequestRetry) {
        retryOnServerErrors(maxRetries = 3)
        retryOnException(maxRetries = 3, retryOnTimeout = true)
        exponentialDelay()
    }
    defaultRequest { url("https://bible-api.com") }
}
```

Four plugins, each pulling its weight:

- **`ContentNegotiation` + `Json`** — automatic (de)serialization via `kotlinx.serialization`. `ignoreUnknownKeys` lets the API add fields without breaking our parser; `isLenient` tolerates slightly-off JSON. Defensive parsing is the difference between a resilient app and a crashy one.
- **`Logging`** — request/response logging at `INFO`. Invaluable when an endpoint misbehaves.
- **`HttpRequestRetry`** — the offline-first mindset showing up early: three retries on server errors *and* exceptions/timeouts, with `exponentialDelay()` so we back off politely instead of hammering a struggling server.
- **`defaultRequest`** — sets the base URL once so every call can be relative.

Then the service itself — a thin, typed wrapper over the three endpoints we use:

```kotlin
// data/api/BibleApiService.kt
class BibleApiService(private val client: HttpClient) {

    suspend fun getBooks(): BooksResponseDto =
        client.get("/books").body()

    suspend fun getChapters(bookId: String): ChaptersResponseDto =
        client.get("/chapters/$bookId").body()

    suspend fun getPassage(reference: String): VersesResponseDto =
        client.get("/$reference").body()
}
```

That `.body()` call is where the magic lands: Ktor reads the response stream, hands it to `kotlinx.serialization`, and gives us back a typed DTO. No manual JSON parsing anywhere.

---

## DTOs and the Mapping Boundary

The API speaks its own dialect — snake_case keys, optional fields, a JSON shape we don't control. We don't let that leak into the domain. Instead we parse into **DTOs** that mirror the API exactly, then map them to clean domain models.

```kotlin
// data/api/dto/VersesResponseDto.kt
@Serializable
data class VersesResponseDto(
    val reference: String = "",
    val verses: List<VerseDto> = emptyList(),
    @SerialName("translation_name") val translationName: String = "",
)

@Serializable
data class VerseDto(
    @SerialName("book_id") val bookId: String = "",
    @SerialName("book_name") val bookName: String = "",
    val chapter: Int = 0,
    val verse: Int = 0,
    val text: String = "",
)
```

Two patterns to call out: **`@SerialName`** maps the API's snake_case (`book_id`) onto idiomatic camelCase, so the JSON ugliness stops at the DTO. And **default values everywhere** (`""`, `0`, `emptyList()`) mean a missing field degrades to a sensible default instead of throwing — combined with `ignoreUnknownKeys`, the API can disappoint us without crashing us.

Then a handful of `toDomain()` mappers turn DTOs into domain models:

```kotlin
// data/api/dto/Mappers.kt
fun BookDto.toDomain() = Book(id, name, abbreviation, testament)

fun ChapterDto.toDomain(fallbackBookId: String) =
    Chapter(id, bookId.ifBlank { fallbackBookId }, number)
```

This little seam is doing real work. The API's `VerseDto` and our domain `Verse` are *different types on purpose*. If the API renames a field, we change the DTO and one mapper — the domain model, repository, use cases, and UI all stay untouched.

---

## The Repository Implementation — Cache-First (with a peek ahead)

Here's where the contract meets reality. `BibleRepositoryImpl` implements `BibleRepository`, and it's built around a **cache-first** strategy:

```kotlin
// data/repository/BibleRepositoryImpl.kt
class BibleRepositoryImpl(
    private val api: BibleApiService,
    private val cache: BibleLocalCache,
) : BibleRepository {

    // Reads check the cache first, falling back to a built-in list.
    override suspend fun getBooks(): List<Book> =
        cache.getBooks().ifEmpty { bibleBooks.also(cache::insertBooks) }

    // The UI reads verses straight from the cache as a reactive stream.
    override fun getVersesFlow(bookId: String, chapter: Int): Flow<List<Verse>> =
        cache.getVersesFlow("$bookId.$chapter")

    // ...getChapters, searchPassage, and bookmark methods follow the same pattern.

    // The network call runs in the background and refills the cache.
    suspend fun fetchAndCacheVerses(bookId: String, chapter: Int) {
        val chapterId = "$bookId.$chapter"
        try {
            val book = bibleBooks.find { it.id == bookId } ?: return
            val response = api.getPassage("${book.name}%20$chapter")
            val verses = response.verses.map { dto ->
                Verse("$chapterId.${dto.verse}", chapterId, bookId, dto.verse, dto.text.trim())
            }
            if (verses.isNotEmpty()) {
                cache.deleteVersesByChapter(chapterId)
                cache.insertVerses(verses, chapterId)
            }
        } catch (_: Exception) {
            // getVersesFlow emits whatever is already in cache
        }
    }
}
```

Look at how verses work, because it's the cleverest piece. The UI subscribes to `getVersesFlow(...)`, which reads straight from the cache. Separately, `fetchAndCacheVerses(...)` hits the network, and when it succeeds it *writes into the cache* — which makes the Flow re-emit and the screen update. If the call fails, the `catch` swallows it and the Flow just keeps serving what was already cached.

That's offline-first in a nutshell: **the cache is the source of truth, and the network is a background refresh.** Open a chapter you've read before with no signal, and it's just *there*.

You'll have noticed `BibleLocalCache`. That's our offline cache — treat it as a black box for now: "give me the books / verses / bookmarks, here are some to store." **We build it for real with SQLDelight, including the `DatabaseDriverFactory` `expect`/`actual` per platform, in Part 3.**

---

## Use Cases — The Thin Seam That Earns Its Keep

The presentation layer doesn't talk to the repository directly. It goes through **use cases** — one small class per action:

```kotlin
// domain/usecase/ — one tiny class per action
class GetBooksUseCase(private val repository: BibleRepository) {
    suspend operator fun invoke(): List<Book> = repository.getBooks()
}

class GetVersesUseCase(private val repository: BibleRepository) {
    operator fun invoke(bookId: String, chapter: Int): Flow<List<Verse>> =
        repository.getVersesFlow(bookId, chapter)
}

class AddBookmarkUseCase(private val repository: BibleRepository) {
    suspend operator fun invoke(bookmark: Bookmark) = repository.addBookmark(bookmark)
}
```

"But these just forward to the repository — why bother?" Fair question, and the honest answer is that the payoff is mostly about *intent and seams*.

The `operator fun invoke` trick lets you call a use case like a function: `getBooks()` instead of `getBooks.invoke()`. The ViewModel then reads like a list of *actions the user can take* rather than a grab-bag of repository methods. And each is a tiny, single-purpose class — trivially testable with a fake repository.

Right now they're thin. But this is exactly where business rules land when they show up — "you can't bookmark a verse twice", "log this action". Better to have the seam empty than to retrofit it through twenty call sites later.

---

## Wiring It All Together — No DI Framework

This project deliberately uses **no dependency-injection framework** — no Koin, no Hilt. For a module this size, a single factory function is clearer than a DI graph and has zero runtime cost. Manual DI, by choice:

```kotlin
// presentation/BibleViewModelFactory.kt
fun createBibleViewModel(driverFactory: DatabaseDriverFactory): BibleViewModel {
    val client = createBibleHttpClient()
    val api = BibleApiService(client)
    val cache = createLocalCache(driverFactory)
    val repository = BibleRepositoryImpl(api, cache)
    return BibleViewModel(
        getBooks = GetBooksUseCase(repository),
        getChapters = GetChaptersUseCase(repository),
        getVerses = GetVersesUseCase(repository),
        getBookmarks = GetBookmarksUseCase(repository),
        addBookmark = AddBookmarkUseCase(repository),
        removeBookmark = RemoveBookmarkUseCase(repository),
        repository = repository,
    )
}
```

Read it top to bottom and the whole dependency graph is right there: HTTP client → API service → repository (with its cache) → use cases → ViewModel. Every arrow points inward toward the domain, exactly as Clean Architecture prescribes.

That `driverFactory` parameter is the one platform-specific thing passed in from outside — the `expect`/`actual` database driver from Part 1's entry points. Everything else is constructed right here in shared code.

---

## What's Next

In Part 2 we built the shared module's backbone:

- ✅ Why Clean Architecture is load-bearing in a KMP shared module
- ✅ Framework-free **domain models** as the stable core
- ✅ The **repository interface** — `suspend` for one-shot reads, `Flow` for reactive streams
- ✅ The **Ktor networking layer** — content negotiation, lenient JSON, retries with exponential backoff
- ✅ **DTOs + mappers** that keep the API's shape out of the domain
- ✅ A **cache-first repository** where the network is a background refresh
- ✅ **Use cases** and a no-framework **manual DI** factory that ties the graph together

We kept saying "more on the cache in Part 3" — so that's exactly where we go next.

**In Part 3**, we build the offline-first storage engine for real: the SQLDelight schema, typed queries, reactive `Flow` results straight from the database, and the `DatabaseDriverFactory` `expect`/`actual` that gives us SQLite on Android and iOS — plus why the browser falls back to an in-memory cache instead.

---

*The full source code for this series is on GitHub: [github.com/sunilk0/BibleAppKMP](https://github.com/sunilk0/BibleAppKMP)*

*Follow me on Medium for Parts 3–5 as they drop.*

---

**Tags:** `Kotlin` `KMP` `Kotlin Multiplatform` `Clean Architecture` `Ktor` `Android` `iOS`
