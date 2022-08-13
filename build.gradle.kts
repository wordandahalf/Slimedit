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
    version = "11.0.2"
    modules("javafx.controls", "javafx.fxml")
}

dependencies {
    implementation("no.tornado:tornadofx:1.7.20")
    implementation("br.com.gamemods:nbt-manipulator:2.0.0")
    implementation("com.github.luben:zstd-jni:1.5.0-2")

    implementation("org.openjfx:javafx-graphics:11.0.2:linux")
    implementation("org.openjfx:javafx-graphics:11.0.2:win")

    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.4.30")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.4.30")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.4.30")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.4.30")
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