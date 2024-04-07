pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        mavenLocal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

rootProject.name = "kotlite"
include(":kotlite-interpreter")
include(":kotlite-stdlib")
include(":kotlite-stdlib-processor-plugin")
include(":kotlite-apidoc")
//includeBuild("./stdlib-processor-gradle-plugin")

project(":kotlite-interpreter").projectDir = File("$rootDir/interpreter")
project(":kotlite-stdlib").projectDir = File("$rootDir/stdlib")
project(":kotlite-stdlib-processor-plugin").projectDir = File("$rootDir/stdlib-processor-gradle-plugin")
project(":kotlite-apidoc").projectDir = File("$rootDir/apidoc")

