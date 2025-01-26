plugins {
  kotlin("jvm") version "1.8.9"
}

allprojects {
  group = "fun.autoclick"
  version = "1.0.0"

  repositories {
    mavenCentral()
  }
}

subprojects {
  apply(plugin = "org.jetbrains.kotlin.jvm")

  dependencies {
    implementation(kotlin("stdlib"))
  }
}
