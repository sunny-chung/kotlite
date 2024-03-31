plugins {
    kotlin("multiplatform") version "1.9.23" apply false
    kotlin("jvm") version "1.9.23" apply false
}

group = "com.sunnychung"

val projectGroup = group
subprojects {
    group = projectGroup
}

repositories { // needed by commonizer
    mavenCentral()
}
