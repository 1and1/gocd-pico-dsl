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

import net.oneandone.gocd.picodsl.dsl.GocdConfig
import net.oneandone.gocd.picodsl.renderer.toYaml
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

class ConfigSuite(vararg configs: GocdConfig, private val outputFolder: Path = Paths.get(".")) {
    private val configs: List<GocdConfig> = configs.toList()

    fun writeFiles() {
        if (!outputFolder.toFile().mkdirs()) {
            throw IllegalStateException("output folder could not be created: ${outputFolder}")
        }

        configs.forEachIndexed { index, gocdConfig ->
            val yamlString = gocdConfig.toYaml()
            File(outputFolder.toFile(), "pipelines-${gocdConfig.name ?: index}.gocd.yaml").writeText(yamlString)
        }
    }
}