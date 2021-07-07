plugins {
    kotlin("jvm") version "1.4.30"
    id("org.openjfx.javafxplugin") version "0.0.10"
}

group = "org.wordandahalf"
version = "1.0"

repositories {
    mavenCentral()
}

javafx {
    version = "11.0.2"
    modules("javafx.controls", "javafx.fxml")
}

dependencies {
    implementation("no.tornado:tornadofx:1.7.20")
    implementation("br.com.gamemods:nbt-manipulator:2.0.0")
    implementation("com.github.luben:zstd-jni:1.5.0-2")
}

tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> { kotlinOptions { jvmTarget = "11" } }
}