plugins {
    kotlin("jvm") version "1.4.30"
    id("org.openjfx.javafxplugin") version "0.0.10"
    id("com.github.johnrengelman.shadow") version "7.1.0"
}

group = "org.wordandahalf"
version = "1.0.0"

repositories {
    mavenCentral()
}

javafx {
    version = "16"
    modules("javafx.controls", "javafx.fxml")
}

dependencies {
    implementation("no.tornado:tornadofx:1.7.20")
    implementation("br.com.gamemods:nbt-manipulator:3.1.0")
    implementation("com.github.luben:zstd-jni:1.5.2-2")
}

tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> { kotlinOptions { jvmTarget = "11" } }
    jar {
        manifest {
            attributes(
                "Main-Class" to "org.wordandahalf.slimedit.SlimeditMainKt",
                "Implementation-Title" to "Gradle",
                "Implementation-Version" to archiveVersion
            )
        }
    }
}