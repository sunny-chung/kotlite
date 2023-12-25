pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

rootProject.name = "kotlite"
include(":kotlite-interpreter")

project(":kotlite-interpreter").projectDir = File("$rootDir/interpreter")

