# Agent Guide for Tennis Ball Mod

This file is for agentic coding assistants working in this repository.
It summarizes how to build, run, and follow project conventions.

## Repository Context
- Fabric mod written primarily in Clojure with minimal Java bootstrap.
- Java runtime/compile target: 17 (see `build.gradle`, `gradle.properties`).
- Mod id: `tennisball` (see `fabric.mod.json`).

## Build / Run / Test / Lint Commands
Use the Gradle wrapper to avoid local version drift.

### Build
- `./gradlew build` — assemble JAR and run standard checks.
- `./gradlew clean` — remove build artifacts.

### Run (dev)
- `./gradlew runClient` — launches Minecraft client with the mod.

### Tests
There are currently no tests or test libraries configured.
If tests are added later, prefer the Gradle `test` task.

Common Gradle patterns for single test selection:
- Java/JUnit: `./gradlew test --tests "com.tennisball.SomeTest"`
- Java/JUnit single method: `./gradlew test --tests "com.tennisball.SomeTest.someMethod"`

If Clojure tests are added (e.g., `clojure.test`), wire a task in
`build.gradle` and document the exact command here.

### Lint / Format
No linter or formatter is configured.
If you add one, document the exact task and config files here.

### Useful Gradle Tasks
- `./gradlew tasks` — list available tasks.
- `./gradlew dependencies` — dependency insight.

## Code Style Guidelines
Follow existing patterns in the codebase and keep changes minimal.

### General
- Favor clarity over cleverness; keep changes scoped.
- Preserve the current structure: Clojure for logic, Java for entry points.
- Avoid adding new dependencies unless necessary.

### Clojure
- Namespace form:
  - Use `(ns ...)` at top of file.
  - Group `:import` classes inside a vector, one class per line.
- Naming:
  - Namespaces use `tennisball.*` and kebab-case for file names.
  - Functions use kebab-case (e.g., `register-item`, `init-client`).
  - Constants use `def` with kebab-case; avoid `*earmuffs*` unless dynamic.
- Formatting:
  - Use standard Clojure indentation (2 spaces).
  - Keep line lengths reasonable and avoid deep nesting.
- Interop:
  - Use `Class/STATIC` for static fields (e.g., `RegistryKeys/ITEM`).
  - Use `Class.` for constructor calls.
  - Use Java method calls with dot syntax (e.g., `(.registryKey ...)`).
- Logging:
  - Current pattern uses `println` for lifecycle logs.
  - Prefer concise, prefixed messages (e.g., "Tennis Ball Mod - ...").
- Error handling:
  - Use `try`/`catch` only around code likely to fail at runtime.
  - Fail fast on initialization errors so mod load issues are visible.

### Java
- Keep Java minimal and delegate to Clojure.
- Class/package naming:
  - Packages follow `com.tennisball`.
  - Classes use PascalCase (e.g., `TennisBallMod`).
- Imports:
  - Group standard library, third-party, and project imports.
  - Avoid unused imports.
- Formatting:
  - 4-space indentation, braces on the same line.
- Interop:
  - Use `Clojure.var(...)` for function lookup and invoke with `IFn`.

### Resources
- Resource files live under `src/main/resources`.
- Keep asset paths consistent with mod id: `assets/tennisball/...`.
- Language file: `assets/tennisball/lang/en_us.json`.
- Model/texture paths should match item ids.

## Project Layout
- `src/main/clojure/tennisball/` — core mod logic.
- `src/main/java/com/tennisball/` — Fabric entry points.
- `src/main/resources/` — Fabric metadata, assets, lang, models.

## Adding New Features
- Prefer Clojure implementations; only add Java when required by Fabric APIs.
- Wire new item/entity registration in `tennisball.core` or new namespaces.
- If adding entities, update both server and client init as needed.

## Cursor/Copilot Rules
- No `.cursor/rules/`, `.cursorrules`, or `.github/copilot-instructions.md`
  were found at the repository root when this file was generated.

## Safety Notes for Agents
- Do not modify `gradle.properties` unless required by the change.
- Do not remove existing assets or metadata without explicit intent.
- If you add tests, update this document with exact commands.
