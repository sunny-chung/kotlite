plugins {
    kotlin("multiplatform")
}

repositories {
    mavenCentral()
}

kotlin {
    js {
        browser()
        binaries.executable()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":kotlite-interpreter"))
                implementation(project(":kotlite-stdlib"))
            }
        }
        val jsMain by getting
        val jsTest by getting
    }
}
