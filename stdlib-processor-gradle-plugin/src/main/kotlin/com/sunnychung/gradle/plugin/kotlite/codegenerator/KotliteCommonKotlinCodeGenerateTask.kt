package com.sunnychung.gradle.plugin.kotlite.codegenerator

import com.sunnychung.gradle.plugin.kotlite.codegenerator.domain.StdLibDelegationCodeGenerator
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class KotliteCommonKotlinCodeGenerateTask : DefaultTask() {
    @get:InputDirectory
    abstract val inputDir: DirectoryProperty

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun generate() {
        val outputDirObj = outputDir.asFile.get()
        outputDirObj.mkdirs()
        inputDir.asFileTree.forEach {
            val name = it.nameWithoutExtension
            val output = StdLibDelegationCodeGenerator(name, it.readText()).generate()
            File(outputDirObj, "$name.kt").writeText(output)
        }
    }
}
