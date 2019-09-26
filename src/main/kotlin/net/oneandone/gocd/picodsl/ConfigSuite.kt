package net.oneandone.gocd.picodsl

import net.oneandone.gocd.picodsl.dsl.GocdConfig
import net.oneandone.gocd.picodsl.renderer.toYaml
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

class ConfigSuite(vararg configs: GocdConfig, private val outputFolder: Path = Paths.get(".")) {
    private val configs: List<GocdConfig> = configs.toList()

    fun writeFiles() {
        outputFolder.toFile().mkdirs()

        configs.forEachIndexed { index, gocdConfig ->
            val yamlString = gocdConfig.toYaml()
            File(outputFolder.toFile(), "pipelines-${gocdConfig.name ?: index}.gocd.yaml").writeText(yamlString)
        }
    }
}