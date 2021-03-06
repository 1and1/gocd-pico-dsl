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
package net.oneandone.gocd.picodsl.renderer.yaml

import net.oneandone.gocd.picodsl.dsl.*
import org.jgrapht.Graph
import org.jgrapht.graph.DefaultEdge

/**
 * Class represents the YAML structure,
 * see [gocd-yaml-config-plugin - Format Reference](https://github.com/tomzo/gocd-yaml-config-plugin#Format-reference)
 * */

data class YamlConfig(private val config: GocdConfig) {
    val format_version
        get() = 3

    val environments: Map<String, YamlEnvironment>
        get() {
            return config.environmentPipelines.map {
                it.key.name to YamlEnvironment(it.key, it.value.toList())
            }.toMap()
        }

    val pipelines: Map<String, YamlPipeline>
        get() {
            val graph = config.pipelines.graph
            val pipelineList = config.pipelines.pipelines()
            return pipelineList.filter { !it.stub }.map { it.name to YamlPipeline(it, graph) }.toMap()
        }
}

data class YamlEnvironment(private val environment: GocdEnvironment, private val configPipelines: List<PipelineSingle>) {
    val environment_variables
        get() = environment.environmentVariables

    val pipelines: List<String>
        get() {
            return configPipelines.filter { !it.stub }.map { it.name }
        }
}

data class YamlPipeline(private val pipelineSingle: PipelineSingle, private val graph: Graph<PipelineSingle, DefaultEdge>) {
    val template
        get() = pipelineSingle.template?.name

    val lock_behavior
        get() = pipelineSingle.lockBehavior

    val label_template: String
        get() {
            val materials = materials.entries
            return "${'$'}{${materials.first().key}}"
        }

    val timer: YamlTimer?
        get() = pipelineSingle.timer?.let { YamlTimer(it) }

    val group
        get() = pipelineSingle.group
    val parameters: Map<String, String>
        get() = pipelineSingle.parameters

    val environment_variables
        get() = pipelineSingle.environmentVariables

    val materials: Map<String, Map<String, String>>
        get() {
            return when {
                pipelineSingle.materials?.isNotEmpty() == true -> pipelineSingle.materials!!.map {
                    it.name to mapOf(
                            "package" to it.name
                    )
                }.toMap()
                else -> graph.upstreamPipelines(pipelineSingle).map {
                    it.name to mapOf(
                            "pipeline" to it.name,
                            "stage" to it.lastStage
                    )
                }.toMap()
            }
        }

    val stages: List<Map<String, YamlStage>>
        get() {
            return pipelineSingle.stages.map { mapOf(it.name to YamlStage(it)) }
        }
}

class YamlStage(private val stage: Stage) {
    val approval
        get() = if (stage.manualApproval) "manual" else null

    val jobs
        get() = stage.jobs.map { it.name to YamlJob(it) }.toMap()
}

class YamlJob(private val job: Job) {
    val tasks
        get() = job.tasks
}

class YamlTimer(private val timer: QuartzTimer) {
    val spec
        get() = timer.spec

    val only_on_changes
        get() = timer.onlyOnChanges
}