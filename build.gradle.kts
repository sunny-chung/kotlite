plugins {
    kotlin("multiplatform") version "1.9.23" apply false
    kotlin("jvm") version "1.9.23" apply false
}

group = "io.github.sunny-chung"

val projectGroup = group
subprojects {
    group = projectGroup
}

repositories { // needed by commonizer
    mavenCentral()
}
