# Repository Guidelines

## Quick Start
- Use the Gradle wrapper with the built-in Java 25 toolchain; no separate JDK management is required.
- Run `./gradlew build` to compile, execute unit tests, and refresh the shaded jar in `build/libs/Minestom-Holyngots-1.0-SNAPSHOT.jar`.
- Run `./gradlew shadowJar` when you need to force a new shaded artifact after changing dependencies or resources.
- Launch the server locally via `java -jar build/libs/Minestom-Holyngots-1.0-SNAPSHOT.jar` and monitor the console for quest/NPC/mob logs.
- Use `./gradlew clean` if you encounter classpath drift or stale compiled assets.

## Project Structure & Ownership
- `src/main/java/org/example/Main.java` is the Minestom entry point; it bootstraps instances, initialises the game lifecycle, restores player data, and auto-starts quests on login.
- `org/example/bootstrap` contains bootstrap wiring: `InstanceBootstrap`/`InstanceRegistry`, `GameLifecycle`, `GameContext`, `InstancesSaving`, and the MOTD service. Place new lifecycle wiring and shared services here.
- `org/example/mmo` holds gameplay modules (`combat`, `commands`, `dev`, `inventory`, `item`, `mob`, `npc`, `player`, `quest`). Respect these boundaries when adding features; keep shared helpers close to their domain.
- `org/example/data` stores shared DTOs (player data classes, teleport payloads) consumed by multiple modules. `org/example/utils` contains generic utilities safe to reuse anywhere.
- Runtime data lives in `worlds/` (Anvil maps) and `playerdata/` (JSON saves). Treat them as hot-reloadable resources but avoid committing accidental changes.
- `docs/` provides gameplay documentation (mob guides, validation checklists) and `codex.txt` captures the current mob-system backlog; keep them updated when extending those areas.

## Core Systems
- `Main` constructs an `InstanceRegistry` via `InstanceBootstrap`, seeds `GameLifecycle`, registers the MOTD, and restores player state/spawn points through `PlayerDataUtils` before starting the server.
- `GameLifecycle` owns the Minestom event graph (`gameNode`, `playerNode`, `entityNode`, `inventoryNode`), starts `PlayerDataService`, and registers combat listeners/UI, inventory hooks, mob AI/spawn/zone services, item events, quest manager, NPC dialog service, dev utilities, and commands.
- `GameContext` is the global access point for instances, event nodes, player data service, and mob spawning services. Gameplay modules should resolve dependencies through it instead of instantiating new singletons.
- Bootstraps (`ItemBootstrap`, `QuestBootstrap`, `NpcBootstrap`, `MobBootstrap`, `MobZoneBootstrap`) rely on ClassGraph package scanning. Add new items, quests, NPCs, mobs, AI behaviours, loot tables, or spawn zones in their expected packages so static initialisers register them automatically.
- `MobSpawnService` manages archetype spawning and tracks active entities; `MobSpawningZoneService` keeps hunting grounds populated and handles respawn timers. Mobs are tagged with `mmo:mob_archetype` for quest/objective integration.
- Player persistence flows through `PlayerDataService` (JSON via `JsonPlayerDataRepository`). Keep DTO changes in sync with the serializer and update migration steps if player data formats evolve.

## Coding & Style
- Target Java 25 with 4-space indentation, prefer `final` for immutable collaborators, and avoid Lombok or unchecked reflection helpers.
- Follow package naming conventions (`org.example.mmo.<domain>`). Suffix quests with `Quest` or `Step`, name behaviours/loot/AI classes consistently, and group dev tooling under `org.example.mmo.dev`.
- Use Adventure `Component` messaging and SLF4J logging (`LoggerFactory`); do not rely on `System.out`.
- Keep runtime configuration (IDs, loot tables, quest metadata) adjacent to their registries/bootstraps and document special cases in `docs/`.

## Testing & Verification
- Add JUnit tests under `src/test/java`; Gradle discovers them automatically via `./gradlew test`.
- Cover registry edge cases (missing IDs, duplicate registrations), quest/NPC failure paths, loot roll boundaries, and mob behaviour selection when making gameplay changes.
- After gameplay-impacting changes, run the shaded jar locally and walk through at least one affected quest/combat/mob scenario to confirm boss bars, drops, and persistence behave correctly.
- Use the validation checklists in `docs/mobs-validation.md` and other docs when extending the mob system.

## Workflow Notes
- Register player-facing commands through `CommandRegister` and reuse the gating logic provided by `GameLifecycle` to scope them to game instances.
- For teleport/spawn logic reuse `TeleportUtils` and `InstanceRegistry` helpers so respawn points remain consistent across sessions.
- Keep `worlds/` and `playerdata/` out of commits unless intentional fixtures are required; treat them as runtime data.
- Update `docs/` or `codex.txt` when introducing new systems or altering onboarding steps so future contributors stay aligned.

