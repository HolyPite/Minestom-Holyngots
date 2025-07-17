# Minestom-Holyngots

This project is a small demo using the [Minestom](https://github.com/Minestom/Minestom) server framework. It demonstrates custom items, combat mechanics and other features implemented in `src/main/java`.

Player data is persisted in the `playerdata` folder using JSON files. Each player gets a file named `<uuid>.json` containing their level, experience and inventory.

The project now includes a minimal quest system. Quests are composed of several
steps and their progress is stored with the rest of the player data. Quest
definitions and progress classes can be found under
`src/main/java/org/example/mmo/quests`.
When you interact with an NPC tagged with a quest ID, the server now lists the
quests you can start or continue with that character.

## Building

The repository uses the Gradle build system. To compile the project, run:

```bash
./gradlew build
```

This will compile the code and produce `build/libs/Minestom-Holyngots-1.0-SNAPSHOT.jar`.

## Running the server

Once built, you can start the server using the `java` command. Make sure the compiled jar and the `libs` directory are on the classpath so that dependencies are found:

```bash
java -cp build/libs/* org.example.Main
```

The server will bind on `0.0.0.0:25565` by default.
