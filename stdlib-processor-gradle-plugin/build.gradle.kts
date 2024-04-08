plugins {
    `java-gradle-plugin`
    kotlin("jvm")
    id("sunnychung.publication")
}

version = "1.0.0"

fun getExtraString(name: String) = ext[name]?.toString()

gradlePlugin {
    plugins {
        create("stdlibProcessorPlugin") {
            println("> group: $group")
            id = "$group.kotlite-stdlib-processor-plugin"
            implementationClass = "com.sunnychung.gradle.plugin.kotlite.codegenerator.KotliteLibHeaderProcessorPlugin"
            displayName = getExtraString("artifact-name")
            description = getExtraString("artifact-description")

        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":kotlite-interpreter"))
}

java {
    withSourcesJar()
}
