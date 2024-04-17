plugins {
    kotlin("multiplatform")
    id("sunnychung.publication")
}

version = "1.1.0-snapshot.1"

repositories {
    mavenCentral()
}

kotlin {
    jvm {
        jvmToolchain(8)
        withJava()
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
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val nonJsMain by creating {
            dependsOn(commonMain)
        }
        val nonJsTest by creating {
            dependsOn(commonTest)
        }
        val jvmMain by getting {
            dependsOn(nonJsMain)
        }
        val jvmTest by getting
        val jsMain by getting
        val jsTest by getting
//        val nativeMain by getting
//        val nativeTest by getting

        val darwinMain by creating {
            // bug: Compilation failed: IrPropertyPublicSymbolImpl ...
//            dependsOn(commonMain)
            dependsOn(nonJsMain)
        }
        val darwinTest by creating {
//            dependsOn(commonTest)
            dependsOn(nonJsTest)
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
