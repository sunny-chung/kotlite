package com.sunnychung.gradle.plugin.kotlite.codegenerator

import com.sunnychung.gradle.plugin.kotlite.codegenerator.domain.StdLibDelegationCodeGenerator
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class KotliteCommonKotlinCodeGenerateTask : DefaultTask() {
    @get:InputDirectory
    abstract val inputDir: DirectoryProperty

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @get:Input
    abstract val outputPackage: Property<String>

    @get:Input
    abstract val configs: MapProperty<String, KotliteModuleConfig>

    @TaskAction
    fun generate() {
        val outputDirObj = outputDir.asFile.get()
        outputDirObj.mkdirs()
        inputDir.asFileTree.forEach {
            if (!it.name.endsWith(".kt")) return@forEach
            val name = it.nameWithoutExtension
            val config = configs.get()[name] ?: KotliteModuleConfig()
            val output = StdLibDelegationCodeGenerator(
                name = name,
                code = it.readText(),
                outputPackage = outputPackage.get(),
                config = config
            ).generate()
            File(outputDirObj, "${name}LibModule.kt").writeText(output)
        }
    }
}
