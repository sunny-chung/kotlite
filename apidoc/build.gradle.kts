plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":kotlite-interpreter"))
    implementation(project(":kotlite-stdlib"))
}

task<JavaExec>("generateStdlibApiDoc") {
    mainClass.set("com.sunnychung.gradle.plugin.kotlite.apidoc.GenerateApiDocTaskKt")
    classpath(sourceSets["main"].runtimeClasspath)
    workingDir(projectDir)
    args("build/apidoc/API.adoc")
}
