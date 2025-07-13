# Minestom-Holyngots

Minestom-Holyngots is a small showcase built on top of the [Minestom](https://github.com/Minestom/Minestom) framework. It demonstrates how to create a custom Minecraft-like server without relying on a vanilla server jar.

The project focuses on simplicity and aims to help you understand how Minestom works. It includes examples of:

- Multiple worlds ("instances") loaded from the `worlds/` folder.
- A basic combat system and event management.
- Custom items with their own stats and lore.
- Saving player data to JSON so progress persists between sessions.
- Periodic world saving when the server shuts down or at regular intervals.

Feel free to explore the source code in `src/main/java` to see how these features are implemented.

## Building

This repository uses Gradle. To compile everything, run:

```bash
./gradlew build
```

The resulting jar will be placed in `build/libs/`.

## Running the server

After building, start the server with:

```bash
java -cp build/libs/* org.example.Main
```

By default it binds to `0.0.0.0:25565`. Connect with your Minecraft client to try the demo.

