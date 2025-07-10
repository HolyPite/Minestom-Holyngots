plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("net.minestom:minestom-snapshots:1_21_5-c974e54f44")
    implementation("org.slf4j:slf4j-simple:2.0.14")
    implementation("io.github.classgraph:classgraph:4.8.163")
    implementation("com.google.code.gson:gson:2.10.1")
}
