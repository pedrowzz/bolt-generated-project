plugins {
    kotlin("jvm") version "1.8.9"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.8.9"
}

group = "fun.autoclick"
version = "1.0.0"

allprojects {
    repositories {
        mavenCentral()
        maven { url = uri("https://repo.spigotmc.org/snapshots/") }
        maven { url = uri("https://hub.spigotmc.org/nexus/repository/public/") }
        maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")

    dependencies {
        implementation(kotlin("stdlib"))
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
    }
}
