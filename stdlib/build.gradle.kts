import com.sunnychung.gradle.plugin.kotlite.codegenerator.KotliteModuleConfig

//buildscript {
//    dependencies {
//        classpath("$group:kotlite-stdlib-processor-plugin")
//    }
//}

plugins {
    kotlin("multiplatform")
    id("sunnychung.publication")
    id("io.github.sunny-chung.kotlite-stdlib-processor-plugin") version "1.0.0"
}

//apply("kotlite-stdlib-processor-plugin")

version = "1.0.0"

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
                    timeout = "21s" // github mac runner is slow
                }
            }
        }
        nodejs {
            testTask {
                useMocha {
                    timeout = "21s" // github mac runner is slow
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
                implementation("io.github.sunny-chung:kdatetime-multiplatform:1.0.0")
            }
//            kotlin.srcDir("build/generated/common/")
            kotlin.srcDir(tasks.named("kotliteStdlibHeaderProcess").map { it.outputs })
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
            imports = listOf(
                "com.sunnychung.lib.multiplatform.kotlite.stdlib.regex.RegexClass",
                "com.sunnychung.lib.multiplatform.kotlite.stdlib.regex.RegexValue",
            )
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
                "com.sunnychung.lib.multiplatform.kotlite.stdlib.kdatetime.KDateTimeFormattableInterface",
                "com.sunnychung.lib.multiplatform.kotlite.stdlib.kdatetime.KPointOfTimeValue",
                "com.sunnychung.lib.multiplatform.kotlite.stdlib.kdatetime.KFixedTimeUnitValue",
                "com.sunnychung.lib.multiplatform.kotlite.stdlib.kdatetime.KInstantClass",
                "com.sunnychung.lib.multiplatform.kotlite.stdlib.kdatetime.KZonedInstantClass",
                "com.sunnychung.lib.multiplatform.kotlite.stdlib.kdatetime.KZoneOffsetClass",
                "com.sunnychung.lib.multiplatform.kotlite.stdlib.kdatetime.KDurationClass",
                "com.sunnychung.lib.multiplatform.kotlite.stdlib.kdatetime.KZonedDateTimeClass",
                "com.sunnychung.lib.multiplatform.kotlite.stdlib.kdatetime.KDateClass",
                "com.sunnychung.lib.multiplatform.kotlite.stdlib.kdatetime.KDateTimeFormatClass",
                "com.sunnychung.lib.multiplatform.kotlite.stdlib.kdatetime.KPointOfTimeClass",
                "com.sunnychung.lib.multiplatform.kotlite.stdlib.kdatetime.KFixedTimeUnitClass",
                "com.sunnychung.lib.multiplatform.kdatetime.KFixedTimeUnit",
                "com.sunnychung.lib.multiplatform.kdatetime.KInstant",
                "com.sunnychung.lib.multiplatform.kdatetime.KZonedInstant",
                "com.sunnychung.lib.multiplatform.kdatetime.KZoneOffset",
                "com.sunnychung.lib.multiplatform.kdatetime.KDuration",
                "com.sunnychung.lib.multiplatform.kdatetime.KZonedDateTime",
                "com.sunnychung.lib.multiplatform.kdatetime.KDate",
                "com.sunnychung.lib.multiplatform.kdatetime.KDateTimeFormat",
                "com.sunnychung.lib.multiplatform.kdatetime.KDateTimeFormattable",
                "com.sunnychung.lib.multiplatform.kdatetime.KPointOfTime",
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
                "com.sunnychung.lib.multiplatform.kotlite.stdlib.collections.MutableListValue",
                "com.sunnychung.lib.multiplatform.kotlite.stdlib.collections.MapValue",
                "com.sunnychung.lib.multiplatform.kotlite.stdlib.collections.MutableMapValue",
                "com.sunnychung.lib.multiplatform.kotlite.stdlib.collections.MapEntryIterator",
                "com.sunnychung.lib.multiplatform.kotlite.stdlib.collections.MapEntryValue",
                "com.sunnychung.lib.multiplatform.kotlite.stdlib.collections.SetValue",
                "com.sunnychung.lib.multiplatform.kotlite.stdlib.collections.MutableSetValue",
                "com.sunnychung.lib.multiplatform.kotlite.stdlib.collections.wrap",
                "com.sunnychung.lib.multiplatform.kotlite.stdlib.collections.MutableListClass",
                "com.sunnychung.lib.multiplatform.kotlite.stdlib.collections.MapClass",
                "com.sunnychung.lib.multiplatform.kotlite.stdlib.collections.MutableMapClass",
                "com.sunnychung.lib.multiplatform.kotlite.stdlib.collections.MapEntryClass",
                "com.sunnychung.lib.multiplatform.kotlite.stdlib.collections.SetClass",
                "com.sunnychung.lib.multiplatform.kotlite.stdlib.collections.MutableSetClass",
            ),
            typeAliases = mapOf(
                "MapEntry<K, V>" to "Map.Entry<K, V>"
            ),
        ),
        "Core" to KotliteModuleConfig(),
        "Math" to KotliteModuleConfig(
            imports = listOf("kotlin.math.*")
        ),
        "Byte" to KotliteModuleConfig(
            imports = listOf(
                "com.sunnychung.lib.multiplatform.kotlite.stdlib.byte.ByteArrayValue",
                "com.sunnychung.lib.multiplatform.kotlite.stdlib.byte.wrap",
                "com.sunnychung.lib.multiplatform.kotlite.stdlib.collections.SetValue",
            )
        ),
        "Range" to KotliteModuleConfig(
            imports = listOf(
                "com.sunnychung.lib.multiplatform.kotlite.stdlib.range.ClosedRangeValue",
                "com.sunnychung.lib.multiplatform.kotlite.stdlib.range.OpenEndRangeValue",
                "com.sunnychung.lib.multiplatform.kotlite.stdlib.range.IntProgressionValue",
                "com.sunnychung.lib.multiplatform.kotlite.stdlib.range.IntRangeValue",
                "com.sunnychung.lib.multiplatform.kotlite.stdlib.range.LongProgressionValue",
                "com.sunnychung.lib.multiplatform.kotlite.stdlib.range.LongRangeValue",
            )
        ),
    )
}
