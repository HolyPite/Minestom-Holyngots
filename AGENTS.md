# Repository Guidelines

## Project Structure & Module Organization
- `src/main/java/org/example` hosts the server entry (`Main.java`), instance wiring (`InstancesInit.java`), and shared utilities; keep new services within this tree.
- `org/example/mmo` groups gameplay modules: `combat`, `quest`, `npc`, `item`, `inventory`, `player`, and `dev`; respect those boundaries when expanding features.
- Runtime assets live in `worlds/` for Anvil maps and `playerdata/` for JSON saves; treat both as hot-reloadable resources during development.

## Build, Test, and Development Commands
- `./gradlew build` compiles the code, runs unit tests, and emits `build/libs/Minestom-Holyngots-1.0-SNAPSHOT.jar`.
- `./gradlew shadowJar` forces a fresh shaded artifact whenever dependencies or resources change.
- `java -jar build/libs/Minestom-Holyngots-1.0-SNAPSHOT.jar` launches the standalone Minestom server; watch the console for quest and NPC logs.
- `./gradlew clean` clears Gradle outputs to resolve classpath drift before re-running builds.

## Coding Style & Naming Conventions
- Target Java 21 with 4-space indentation and prefer `final` for immutable collaborators; avoid Lombok or unchecked reflection helpers.
- Classes use UpperCamelCase (`QuestManager`), fields and locals use lowerCamelCase, and constants remain in `UPPER_SNAKE_CASE`.
- Place new gameplay code under `org.example.mmo.<domain>` and suffix quest classes with `Quest` or `Step` to mirror existing registries.
- Prefer adventure `Component` messaging and SLF4J logging over `System.out`.

## Testing Guidelines
- Add JUnit tests beneath `src/test/java`; Gradle discovers them automatically via `./gradlew test`.
- For quest or NPC changes, cover missing registry entries and invalid IDs to prevent regressions like null NPC names.
- Smoke-test every change by running the shaded jar locally and walking through at least one affected quest or combat scenario.

## Commit & Pull Request Guidelines
- Write short, imperative commit subjects (e.g. `fix: guard quest completion`) with optional scope prefixes and wrap bodies at ~72 characters.
- Before opening a PR, confirm `./gradlew build` succeeds and summarize the manual playtests you executed.
- Reference related issues, attach relevant console output or screenshots, and call out data migrations touching `worlds/` or `playerdata/`.
