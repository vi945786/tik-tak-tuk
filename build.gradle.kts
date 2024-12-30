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

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("com.google.guava:guava:33.3.1-jre")
    implementation(kotlin("stdlib-jdk8"))
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

tasks.register("forceClean") {
    doLast {
        Runtime.getRuntime().exec("D:\\Program Files\\LockHunter\\LockHunter.exe -sm -d ${layout.buildDirectory}")
    }
}
kotlin {
    jvmToolchain(8)
}