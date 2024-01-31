import com.sunnychung.gradle.plugin.kotlite.codegenerator.KotliteModuleConfig

//buildscript {
//    dependencies {
//        classpath("$group:kotlite-stdlib-processor-plugin")
//    }
//}

plugins {
    kotlin("multiplatform")
    id("com.sunnychung.kotlite-stdlib-processor-plugin") version "0.1"
}

//apply("kotlite-stdlib-processor-plugin")

version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    jvm {
        jvmToolchain(8)
//        withJava()
        testRuns.named("test") {
            executionTask.configure {
                useJUnitPlatform()
            }
        }
    }
    js {
        browser {
            commonWebpackConfig {
                cssSupport {
                    enabled.set(true)
                }
            }
            testTask {
                useMocha {
                    timeout = "11s"
                }
            }
        }
        nodejs {
            testTask {
                useMocha {
                    timeout = "11s"
                }
            }
        }
    }
//    val hostOs = System.getProperty("os.name")
//    val isArm64 = System.getProperty("os.arch") == "aarch64"
//    val isMingwX64 = hostOs.startsWith("Windows")
//    val nativeTarget = when {
//        hostOs == "Mac OS X" && isArm64 -> macosArm64("native")
//        hostOs == "Mac OS X" && !isArm64 -> macosX64("native")
//        hostOs == "Linux" && isArm64 -> linuxArm64("native")
//        hostOs == "Linux" && !isArm64 -> linuxX64("native")
//        isMingwX64 -> mingwX64("native")
//        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
//    }

    val darwinTargets = listOf(
        iosArm64(),
        iosSimulatorArm64(),
        iosX64(),
        watchosArm64(),
        watchosSimulatorArm64(),
        watchosX64(),
        tvosArm64(),
        tvosSimulatorArm64(),
        tvosX64(),
        macosArm64(),
        macosX64()
    )

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("co.touchlab:kermit:1.0.0")
                implementation(project(":kotlite-interpreter"))
                implementation("io.github.sunny-chung:kdatetime-multiplatform:0.5.0")
            }
            kotlin.srcDir("build/generated/common/")
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting
        val jvmTest by getting
        val jsMain by getting
        val jsTest by getting
//        val nativeMain by getting
//        val nativeTest by getting

        val darwinMain by creating {
            dependsOn(commonMain)
        }
        val darwinTest by creating {
            dependsOn(commonTest)
        }
        val iosMain by creating {
            dependsOn(darwinMain)
        }
        val watchosMain by creating {
            dependsOn(darwinMain)
        }
        val tvosMain by creating {
            dependsOn(darwinMain)
        }
        val macosMain by creating {
            dependsOn(darwinMain)
        }

        configure(darwinTargets) {
            val (mainSourceSet, testSourceSet) = when {
                name.startsWith("ios") -> Pair(iosMain, darwinTest)
                name.startsWith("watchos") -> Pair(watchosMain, darwinTest)
                name.startsWith("tvos") -> Pair(tvosMain, darwinTest)
                name.startsWith("macos") -> Pair(macosMain, darwinTest)
                else -> throw UnsupportedOperationException("Target $name is not supported")
            }
            compilations["main"].defaultSourceSet.dependsOn(mainSourceSet)
            compilations["test"].defaultSourceSet.dependsOn(testSourceSet)
        }
    }
}

kotliteStdLibHeaderProcessor {
    inputDir = "src/kotlinheader/"
    outputDir = "build/generated/common/"
    outputPackage = "com.sunnychung.lib.multiplatform.kotlite.stdlib"
    configs = mapOf(
        "Regex" to KotliteModuleConfig(
            imports = listOf("com.sunnychung.lib.multiplatform.kotlite.stdlib.regex.RegexValue")
        ),
        "KDateTime" to KotliteModuleConfig(
            imports = listOf(
                "com.sunnychung.lib.multiplatform.kotlite.stdlib.kdatetime.KInstantValue",
                "com.sunnychung.lib.multiplatform.kotlite.stdlib.kdatetime.KZonedInstantValue",
                "com.sunnychung.lib.multiplatform.kotlite.stdlib.kdatetime.KZoneOffsetValue",
                "com.sunnychung.lib.multiplatform.kotlite.stdlib.kdatetime.KDurationValue",
                "com.sunnychung.lib.multiplatform.kotlite.stdlib.kdatetime.KZonedDateTimeValue",
                "com.sunnychung.lib.multiplatform.kotlite.stdlib.kdatetime.KDateValue",
                "com.sunnychung.lib.multiplatform.kotlite.stdlib.kdatetime.KDateTimeFormatValue",
                "com.sunnychung.lib.multiplatform.kdatetime.KInstant",
                "com.sunnychung.lib.multiplatform.kdatetime.KZonedInstant",
                "com.sunnychung.lib.multiplatform.kdatetime.KZoneOffset",
                "com.sunnychung.lib.multiplatform.kdatetime.KDuration",
                "com.sunnychung.lib.multiplatform.kdatetime.KZonedDateTime",
                "com.sunnychung.lib.multiplatform.kdatetime.KDate",
                "com.sunnychung.lib.multiplatform.kdatetime.KDateTimeFormat",
                "com.sunnychung.lib.multiplatform.kdatetime.toKZonedDateTime",
                "com.sunnychung.lib.multiplatform.kdatetime.extension.milliseconds",
                "com.sunnychung.lib.multiplatform.kdatetime.extension.seconds",
                "com.sunnychung.lib.multiplatform.kdatetime.extension.minutes",
                "com.sunnychung.lib.multiplatform.kdatetime.extension.hours",
                "com.sunnychung.lib.multiplatform.kdatetime.extension.days",
                "com.sunnychung.lib.multiplatform.kdatetime.extension.weeks",
            )
        ),
        "Collections" to KotliteModuleConfig(
            imports = listOf(
                "com.sunnychung.lib.multiplatform.kotlite.model.ListValue",
                "com.sunnychung.lib.multiplatform.kotlite.stdlib.collections.MutableListValue",
            )
        ),
    )
}