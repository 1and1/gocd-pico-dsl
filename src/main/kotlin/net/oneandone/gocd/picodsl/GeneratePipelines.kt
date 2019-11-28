/**
 * Copyright 1&1 Internet AG, https://github.com/1and1/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.oneandone.gocd.picodsl

import mu.KotlinLogging
import org.apache.commons.cli.*
import org.reflections.Reflections
import java.io.File

/**
 * Class can be used for generating all pipelines which are registered in [ConfigRegistry].
 *
 * @param args first: base package of GocdRegistries,
 *             second: outputFolder
 */

private val logger = KotlinLogging.logger {}

val options = Options().apply {
    addRequiredOption("s", "sourcePackage", true, "package where the DSLs are located")
    addOption("o", "outputFolder", true, "folder where the generated YAMLs are created")
    addOption("d", "dot", false, "write dot files")
    addOption("p", "plantuml", false, "write plantuml files including dot")
}

fun main(args: Array<String>) {
    try {
        val cmd = DefaultParser().parse(options, args)
        writeFiles(cmd)
    } catch (e: ParseException) {
        logger.error(e) { "error while parsing arguments" }
        HelpFormatter().printHelp("net.oneandone.gocd.picodsl.GeneratePipelinesKt", options)
    }
}

fun writeFiles(cmd: CommandLine) {
    val basePackage = cmd.getOptionValue("sourcePackage").trim()
    val reflections = Reflections(basePackage)

    val configs = reflections.getSubTypesOf(RegisteredGocdConfig::class.java)

    if (configs.isEmpty()) {
        logger.warn { "no sub types of RegisteredGocdConfig found in package '$basePackage'" }
        return
    }

    // instantiate classes in order to get them registered
    configs.forEach {
        it.getDeclaredField("INSTANCE").get(null)
    }

    logger.info { "found the following registered config classes $configs" }

    val outputFolderPath = if (cmd.hasOption("o")) cmd.getOptionValue("o").trim() else "target/gocd-config"

    val configSuite = ConfigSuite(*ConfigRegistry.toTypedArray(), outputFolder = File(outputFolderPath))
    val yamlFiles = configSuite.writeYamlFiles()

    if (cmd.hasOption("dot")) {
        configSuite.writeDotFiles()
    }

    if (cmd.hasOption("plantuml")) {
        configSuite.writePlantUmlDotFiles()
    }

    logger.info { "Generated yamlFiles: $yamlFiles" }
}

