# Minestom-Holyngots

This project is a small demo using the [Minestom](https://github.com/Minestom/Minestom) server framework. It demonstrates custom items, combat mechanics and other features implemented in `src/main/java`.

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
