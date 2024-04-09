plugins {
    kotlin("multiplatform")
    id("com.android.application")
}

repositories {
    mavenCentral()
    google()
}

kotlin {
    js {
        browser()
        binaries.executable()
    }

    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
        iosX64(),
    ).forEach {
        it.binaries.framework {
            baseName = "KotliteInterpreterDemoShared"
            isStatic = true
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.github.sunny-chung:kotlite-interpreter:1.0.0")
                implementation("io.github.sunny-chung:kotlite-stdlib:1.0.0")
            }
        }
        val jsMain by getting
        val jsTest by getting
    }
}

android {
    namespace = "com.sunnychung.lib.android.kotlite.interpreter.demo"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.sunnychung.lib.android.kotlite.interpreter.demo"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
    }
}
