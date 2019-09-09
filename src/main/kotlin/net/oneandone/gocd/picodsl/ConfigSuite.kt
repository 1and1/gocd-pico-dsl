package net.oneandone.gocd.picodsl

import net.oneandone.gocd.picodsl.dsl.GocdConfig
import java.nio.file.Path
import java.nio.file.Paths

class ConfigSuite {
    private val outputFolder: Path
    private lateinit var configs: List<GocdConfig>

    constructor(outputFolder: Path = Paths.get("."), vararg configs: GocdConfig) {
        this.outputFolder = outputFolder
        this.configs = configs.toList()
    }
}