package net.oneandone.gocd.picodsl.renderer.yaml

import net.oneandone.gocd.picodsl.dsl.*
import org.jgrapht.Graph
import org.jgrapht.graph.DefaultEdge

/**
 * Class represents the YAML structure
 * @see https://github.com/tomzo/gocd-yaml-config-plugin#Format-reference
 * */

data class YamlConfig(private val pipelineList: List<PipelineSingle>, private val graph: Graph<PipelineSingle, DefaultEdge>) {
    val pipelines
            get() = pipelineList.map { it.name to YamlPipeline(it, graph) }.toMap()

    // Todo
//    val environments
//    val format_version
}

data class YamlEnvironments(private val environments: GocdEnvironments) {

}


data class YamlPipeline(private val pipelineSingle: PipelineSingle, private val graph: Graph<PipelineSingle, DefaultEdge>) {
    val template
        get() = pipelineSingle.template?.name

    val lock_behavior
        get() = pipelineSingle.lockBehavior

    val label_template : String
        get() {
            val materials = materials.entries
            return "${'$'}{${materials.first().key}}"
        }
    val group
        get() = pipelineSingle.group
    val parameters: Map<String, String>
        get() = pipelineSingle.parameters

    val environment_variables
        get() = pipelineSingle.environmentVariables

    val materials : Map<String, Map<String, String>>
        get() {
            return when {
                pipelineSingle.materials?.materials?.isNotEmpty() == true -> pipelineSingle.materials!!.materials.map {
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
        get() = if(stage.manualApproval) "manual" else null

    val jobs
        get() = stage.jobs.map { it.name to YamlJob(it) }.toMap()
}

class YamlJob(private val job: Job) {
    val tasks
        get() = job.tasks
}