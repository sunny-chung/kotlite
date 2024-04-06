package com.sunnychung.gradle.plugin.kotlite.codegenerator

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import java.io.File
import java.io.Serializable

class KotliteLibHeaderProcessorPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("kotliteStdLibHeaderProcessor", KotliteLibHeaderProcessorPluginExtension::class.java)

        val taskName = "kotliteStdlibHeaderProcess"
        project.tasks.register(taskName, KotliteCommonKotlinCodeGenerateTask::class.java) {
            it.inputDir.set(File(extension.inputDir.get()))
            it.outputDir.set(File(extension.outputDir.get()))
            it.outputPackage.set(extension.outputPackage.get())
            it.configs.set(extension.configs.get())
        }
//        project.afterEvaluate {
//            project.tasks.filter {
//                it.name.startsWith("compile")
//            }.forEach {
//                it.dependsOn(taskName)
//                println("${it.path} dependsOn $taskName")
//            }
//        }
    }
}

interface KotliteLibHeaderProcessorPluginExtension {
    val inputDir: Property<String>
    val outputDir: Property<String>
    val outputPackage: Property<String>
    val configs: MapProperty<String, KotliteModuleConfig>
}

data class KotliteModuleConfig(
    val imports: List<String> = emptyList(),
    val typeAliases: Map<String, String> = emptyMap(),
) : Serializable
