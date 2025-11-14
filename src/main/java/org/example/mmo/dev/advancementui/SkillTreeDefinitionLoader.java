package org.example.mmo.dev.advancementui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.example.mmo.dev.advancementui.definition.SkillTreeDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Charge toutes les {@link SkillTreeDefinition} présentes sous {@code resources/skilltrees}.
 */
public final class SkillTreeDefinitionLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(SkillTreeDefinitionLoader.class);
    private static final Gson GSON = new GsonBuilder().create();
    private static final String ROOT = "skilltrees";

    private final ClassLoader classLoader = SkillTreeDefinitionLoader.class.getClassLoader();

    public List<SkillTreeDefinition> loadAll() {
        List<String> resourcePaths = discoverResourcePaths();
        List<SkillTreeDefinition> definitions = new ArrayList<>();
        for (String resourcePath : resourcePaths) {
            try (InputStream stream = classLoader.getResourceAsStream(resourcePath)) {
                if (stream == null) {
                    LOGGER.warn("Resource {} disappeared before it could be read", resourcePath);
                    continue;
                }
                try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                    SkillTreeDefinition definition = GSON.fromJson(reader, SkillTreeDefinition.class);
                    if (definition != null) {
                        definitions.add(definition);
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Failed to load skill tree definition {}", resourcePath, e);
            }
        }
        LOGGER.info("Loaded {} skill tree definitions from {}", definitions.size(), resourcePaths.size());
        if (definitions.isEmpty()) {
            LOGGER.warn("No skill tree definitions discovered. Check that resources exist under src/main/resources/skilltrees/");
        }
        return definitions;
    }

    private List<String> discoverResourcePaths() {
        Set<String> paths = new HashSet<>();
        try {
            Enumeration<URL> resources = classLoader.getResources(ROOT);
            if (!resources.hasMoreElements()) {
                LOGGER.warn("No '{}' directory found on the classpath", ROOT);
            }
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                switch (url.getProtocol()) {
                    case "file" -> scanDirectory(url, paths);
                    case "jar" -> scanJar(url, paths);
                    default -> LOGGER.warn("Unsupported protocol {} for {}", url.getProtocol(), url);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Failed to enumerate skill tree resources", e);
        }
        return List.copyOf(paths);
    }

    private void scanDirectory(URL url, Set<String> collector) {
        try {
            Path directory = Paths.get(url.toURI());
            if (!Files.isDirectory(directory)) {
                return;
            }
            try (var stream = Files.walk(directory)) {
                stream.filter(Files::isRegularFile)
                        .filter(path -> path.getFileName().toString().endsWith(".json"))
                        .forEach(path -> collector.add(ROOT + "/" + directory.relativize(path).toString().replace('\\', '/')));
            }
        } catch (URISyntaxException | IOException e) {
            LOGGER.error("Failed to scan directory {} for skill trees", url, e);
        }
    }

    private void scanJar(URL url, Set<String> collector) {
        try {
            JarURLConnection connection = (JarURLConnection) url.openConnection();
            try (JarFile jarFile = connection.getJarFile()) {
                String prefix = connection.getEntryName();
                if (prefix == null || prefix.isBlank()) {
                    prefix = ROOT;
                }
                if (!prefix.endsWith("/")) {
                    prefix += "/";
                }
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    if (entry.isDirectory()) {
                        continue;
                    }
                    String name = entry.getName();
                    if (!name.startsWith(prefix) || !name.endsWith(".json")) {
                        continue;
                    }
                    collector.add(name);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Failed to scan jar {} for skill trees", url, e);
        }
    }
}
