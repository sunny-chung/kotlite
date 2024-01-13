package com.sunnychung.gradle.plugin.kotlite.codegenerator

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Property
import java.io.File

class KotliteLibHeaderProcessorPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("kotliteStdLibHeaderProcessor", KotliteLibHeaderProcessorPluginExtension::class.java)

        val taskName = "kotliteStdlibHeaderProcess"
        project.tasks.register(taskName, KotliteCommonKotlinCodeGenerateTask::class.java) {
            it.inputDir.set(File(extension.inputDir.get()))
            it.outputDir.set(File(extension.outputDir.get()))
        }
        project.afterEvaluate {
            project.tasks.filter {
                it.name.startsWith("compileKotlin")
            }.forEach {
                it.dependsOn(taskName)
            }
        }
    }
}

interface KotliteLibHeaderProcessorPluginExtension {
    val inputDir: Property<String>
    val outputDir: Property<String>
}
