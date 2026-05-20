# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

# Bible KMP Project Rules

You are working on a production-grade Bible application using Kotlin Multiplatform.

## Core Stack

- Kotlin Multiplatform (KMP)
- Compose Multiplatform
- Ktor Client
- SQLDelight
- Coroutines + Flow
- Clean Architecture
- MVVM/MVI

## General Rules

- Use Kotlin only.
- Prefer shared business logic in shared/.
- Keep platform-specific code minimal.
- Prefer immutable UI state.
- Use Flow/StateFlow for reactive state.
- Use suspend functions for async operations.
- Avoid blocking operations.
- Prefer scalable modular architecture.
- Avoid unnecessary abstraction.
- Generate production-ready code.

## UI Rules

- Prefer Compose Multiplatform.
- Keep composables reusable.
- Use stateless composables when possible.
- Hoist UI state.
- Optimize recompositions.
- Support dark mode.
- Support tablets and phones.

## Architecture Rules

- Follow Clean Architecture.
- Use Repository pattern.
- Separate:
    - data
    - domain
    - presentation
- Use dependency injection.
- Avoid business logic in UI layer.
- Prefer feature-based modularization.

## API Rules

- Use Ktor Client.
- Handle retries and failures safely.
- Use serialization properly.
- Implement offline-first support.
- Cache Bible data aggressively.
- Avoid duplicate network calls.

## Performance Rules

- Optimize chapter rendering.
- Use pagination/lazy loading.
- Avoid unnecessary recompositions.
- Optimize startup time.
- Minimize memory allocations.

## Bible App Rules

- Support multiple translations.
- Support offline Bible reading.
- Support bookmarks/highlights.
- Support chapter navigation.
- Support verse sharing.
- Keep verse rendering fast.
- Optimize search indexing.

## Code Quality

- Prefer readable code.
- Add comments only when necessary.
- Keep functions focused.
- Avoid massive ViewModels.
- Prefer testable architecture.

## AI Features

- Support future AI integrations.
- Design scalable AI-ready architecture.
- Keep AI modules isolated.
- Support streaming AI responses.
## Build Commands

```shell
# Android
./gradlew :composeApp:assembleDebug

# Server (Ktor on port 8080)
./gradlew :server:run

# Web — WasmJS (modern browsers, faster)
./gradlew :composeApp:wasmJsBrowserDevelopmentRun

# Web — JS (broader browser support)
./gradlew :composeApp:jsBrowserDevelopmentRun

# iOS — open iosApp/ in Xcode and run from there

# Tests
./gradlew test
./gradlew :shared:test
./gradlew :server:test

```

## Module Architecture

Three Gradle modules, all under package `com.sunilbb.bibleappkmp`:

**`:shared`** — Pure Kotlin Multiplatform library consumed by all other modules. Targets: Android, iOS (arm64, simulatorArm64), JVM, JS, WasmJS. Holds platform-agnostic models, business logic, and shared constants (e.g., `SERVER_PORT = 8080`). Platform-specific behavior uses the `expect/actual` pattern — `expect fun getPlatform(): Platform` in `commonMain`, with `actual` implementations in each platform source set (`androidMain`, `iosMain`, `jvmMain`, `jsMain`, `wasmJsMain`).

**`:composeApp`** — Compose Multiplatform UI module. Targets: Android, iOS, JS, WasmJS. The shared `App()` composable lives in `commonMain`; each platform wires it up via its own entry point (`MainActivity` for Android, `MainViewController` for iOS, `main.kt` for web). Depends on `:shared`.

**`:server`** — JVM-only Ktor server using Netty. Entry point: `Application.kt`. Depends on `:shared` for business logic and shared constants. Test with `ktor-server-test-host`.

## Key Stack Versions

| Tool | Version |
|------|---------|
| Kotlin | 2.3.21 |
| Compose Multiplatform | 1.10.3 |
| Ktor | 3.4.3 |
| AGP | 8.11.2 |
| Android minSdk / compileSdk | 24 / 36 |

All dependency versions are centralized in `gradle/libs.versions.toml`.

## expect/actual Pattern

When adding platform-specific behavior to `:shared`, declare the API in `shared/src/commonMain/kotlin/` using `expect`, then provide implementations in each platform source set (`androidMain`, `iosMain`, `jvmMain`, `jsMain`, `wasmJsMain`). Not all targets need an `actual` if they share one (e.g., `jsMain` and `wasmJsMain` can be separate).
