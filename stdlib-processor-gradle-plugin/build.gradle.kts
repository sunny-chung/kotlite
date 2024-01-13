plugins {
    `java-gradle-plugin`
    kotlin("jvm")
    `maven-publish`
}

version = "0.1"

gradlePlugin {
    plugins {
        create("stdlibProcessorPlugin") {
            println("> group: $group")
            id = "$group.kotlite-stdlib-processor-plugin"
            implementationClass = "com.sunnychung.gradle.plugin.kotlite.codegenerator.KotliteLibHeaderProcessorPlugin"
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":kotlite-interpreter"))
}
