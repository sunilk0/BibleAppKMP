# Build a Real KMP App from Scratch — Part 1: What is Kotlin Multiplatform and Why It's Different

*Series: Building a Bible Reader app that runs on Android, iOS, and the web — using one shared Kotlin codebase.*

---

I've been building Android apps for years. I've tried Flutter. I've looked at React Native. But when I finally sat down and built something real with Kotlin Multiplatform (KMP), I understood why it's generating so much excitement — and why it's fundamentally different from every other cross-platform approach.

This is Part 1 of a 5-part series where we build a full Bible reader app together: books list, chapter navigation, verse reader, offline caching, and it all runs on Android, iOS, and the browser from a single shared codebase.

By the end of this series you'll have a real, production-grade KMP app — not a toy counter app.

Let's start at the beginning.

---

## The Cross-Platform Problem

Every cross-platform framework makes the same promise: *write once, run anywhere*. The difference is in how they keep it.

**Flutter** ships its own rendering engine (Skia/Impeller). Your app looks and behaves identically on every platform — because it's not using platform UI at all. That's powerful, but it also means your app never quite *feels* native, and you're carrying Google's rendering engine everywhere you go.

**React Native** bridges JavaScript to native components. You get real native UI, but the JS bridge has historically been a performance bottleneck, and debugging across the boundary is painful.

**KMP takes a different approach entirely.** It doesn't try to replace your platform UI. It only shares the code that *should* be shared — your business logic, data layer, networking, caching — while letting each platform own its UI.

On Android you write Jetpack Compose. On iOS you write SwiftUI or UIKit. Or — and this is where it gets interesting — you use **Compose Multiplatform** and share the UI too, but opt in to it rather than being forced into it.

This matters because:

- Your Android team doesn't have to learn a new UI framework
- Your iOS app still uses Apple's native stack
- You eliminate the duplication that actually hurts: networking, models, business rules, caching, error handling

---

## What We're Building

A Bible reader app with:

- A list of all 66 books
- Chapter navigation per book
- A verse reader that fetches from a live API and caches offline
- Shared business logic across all platforms
- Running targets: **Android**, **iOS**, and **Web (browser)**

The API is [bible-api.com](https://bible-api.com) — a free, open Bible API that returns verse text by passage reference.

Here's a preview of the final app running on iOS simulator:

> *[Screenshot: Bible app showing the books list — Genesis, Exodus, Leviticus...]*

Same code. Android. iOS. Browser.

---

## The Three-Module Architecture

When you create a KMP project, you get a structure that might look unfamiliar at first. Here's ours:

```
BibleAppKMP/
├── shared/          ← Pure Kotlin. All platforms consume this.
├── composeApp/      ← Compose Multiplatform UI (Android, iOS, Web)
└── server/          ← Ktor JVM server
```

Each module has a specific responsibility:

**`:shared`** is the heart of the project. It's a pure Kotlin Multiplatform library with no platform-specific UI. Every business rule, every network call, every database query lives here. It compiles to:
- Android bytecode (JVM)
- iOS native binary (Kotlin/Native)
- JavaScript (for the browser)
- JVM (for the server)

**`:composeApp`** is the UI layer. It uses Compose Multiplatform to share screens across Android, iOS, and the browser. Each platform still has a thin entry point to wire things up.

**`:server`** is a Ktor JVM server. It shares models and constants from `:shared` without any UI code.

This separation is deliberate. Shared code belongs in `:shared`. Platform-specific code stays out.

---

## Setting Up the Project

The easiest way to start is the [Kotlin Multiplatform Wizard](https://kmp.jetbrains.com/). Select your targets and it generates a working project skeleton.

Our `settings.gradle.kts` declares the three modules:

```kotlin
rootProject.name = "BibleAppKMP"

include(":composeApp")
include(":server")
include(":shared")
```

And our `gradle/libs.versions.toml` centralises every dependency version — this is the KMP way of avoiding version conflicts across modules:

```toml
[versions]
kotlin = "2.3.21"
composeMultiplatform = "1.11.0"
ktor = "3.4.3"
sqldelight = "2.0.2"
kotlinx-coroutines = "1.10.2"

[plugins]
kotlinMultiplatform = { id = "org.jetbrains.kotlin.multiplatform", version.ref = "kotlin" }
composeMultiplatform = { id = "org.jetbrains.compose", version.ref = "composeMultiplatform" }
sqldelight = { id = "app.cash.sqldelight", version.ref = "sqldelight" }
```

One file. All versions. Every module reads from it.

---

## Declaring Targets in `:shared`

Here's where KMP gets concrete. In `shared/build.gradle.kts`, we declare every platform we want to support:

```kotlin
kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    iosArm64()           // physical iPhone
    iosSimulatorArm64()  // Apple Silicon simulator

    jvm()                // server

    js {
        browser()        // web
    }
}
```

Each target gets its own compiled output. The Kotlin compiler handles the translation — Kotlin/Native for iOS, Kotlin/JS for the browser, standard JVM bytecode for Android and server.

Dependencies are declared per source set, because not every library works on every platform:

```kotlin
sourceSets {
    commonMain.dependencies {
        implementation(libs.ktor.client.core)         // works everywhere
        implementation(libs.kotlinx.coroutines.core)  // works everywhere
        implementation(libs.androidx.lifecycle.viewmodel)
    }
    androidMain.dependencies {
        implementation(libs.ktor.client.okhttp)   // OkHttp on Android
        implementation(libs.sqldelight.android)   // SQLite on Android
    }
    iosMain.dependencies {
        implementation(libs.ktor.client.darwin)   // URLSession on iOS
        implementation(libs.sqldelight.native)    // SQLite on iOS
    }
    jsMain.dependencies {
        implementation(libs.ktor.client.js)       // Fetch API on web
        // no SQLDelight — no SQLite in the browser
    }
}
```

This is one of KMP's superpowers: you use the *right* native library on each platform instead of a lowest-common-denominator abstraction.

---

## The `expect`/`actual` Pattern — The Core of KMP

Here's the concept that everything else in KMP is built on.

Sometimes you need to write code that behaves differently on each platform, but you want to *call* it the same way from shared code. KMP solves this with `expect` and `actual`.

You declare *what* you need in `commonMain`:

```kotlin
// shared/src/commonMain/kotlin/.../Platform.kt
interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
```

The `expect` keyword says: "I need this function to exist, but each platform will implement it differently."

Then each platform provides its `actual` implementation:

**Android** (`androidMain`):
```kotlin
import android.os.Build

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()
```

**iOS** (`iosMain`):
```kotlin
import platform.UIKit.UIDevice

class IOSPlatform : Platform {
    override val name: String =
        UIDevice.currentDevice.systemName() + " " +
        UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()
```

Notice what just happened. The Android implementation uses `android.os.Build` — a pure Android API. The iOS implementation uses `UIDevice` — a pure Apple API. But from shared code, you call `getPlatform()` and don't think about it.

We use this pattern heavily in our app for the database driver — SQLite on Android/iOS, in-memory on the browser. More on that in Part 3.

---

## The Entry Points — How Each Platform Wires Up

Each platform has a thin entry point that creates the app:

**Android** — `MainActivity.kt`:
```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            App(DatabaseDriverFactory(this))
        }
    }
}
```

**iOS** — `MainViewController.kt` (in `iosMain`):
```kotlin
fun MainViewController() = ComposeUIViewController {
    App(DatabaseDriverFactory())
}
```

**Web** — `main.kt` (in `webMain`):
```kotlin
fun main() {
    onWasmReady {
        CanvasBasedWindow("BibleAppKMP") {
            App(DatabaseDriverFactory())
        }
    }
}
```

Each entry point does two things: creates the platform-specific `DatabaseDriverFactory`, then hands it to the shared `App()` composable. That's it. All the logic lives in shared code.

---

## Why Not WasmJS?

You might notice I'm using `js { browser() }` instead of `wasmJs`. The newer WasmJS target is faster and the future of Compose for Web — but at the time of writing, **SQLDelight 2.0.2 has no WasmJS artifact**. Since our app needs offline caching via SQLDelight, WasmJS is off the table for now.

I'll revisit this in a future post when SQLDelight publishes WasmJS support. For the browser we use a JS target with an in-memory cache instead.

---

## What's Next

In Part 1 we covered:

- ✅ What makes KMP different from Flutter and React Native
- ✅ The three-module project structure
- ✅ How to declare targets and per-platform dependencies
- ✅ The `expect`/`actual` pattern
- ✅ How each platform entry point wires into shared code

**In Part 2**, we go deeper into the shared module and build the Clean Architecture foundation: domain models, the repository interface, use cases, and the Ktor networking layer that talks to the Bible API.

---

*The full source code for this series is on GitHub: [github.com/sunilk0/BibleAppKMP](https://github.com/sunilk0/BibleAppKMP)*

*Follow me on Medium for Parts 2–5 as they drop.*

---

**Tags:** `Kotlin` `KMP` `Kotlin Multiplatform` `Android` `iOS` `Mobile Development` `Cross Platform`
