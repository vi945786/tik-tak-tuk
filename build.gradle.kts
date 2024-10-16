plugins {
    kotlin("jvm") version "2.0.21"
    application
    java
}

buildscript {
    extra["kotlin_version"] = "2.0.21"

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.21")
    }
}

group = "vi"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("com.google.guava:guava:33.3.1-jre")
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "tiktaktuk.MainKt"
    }

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    from(sourceSets.main.get().output)

    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
}

tasks.test {
    useJUnitPlatform()
}