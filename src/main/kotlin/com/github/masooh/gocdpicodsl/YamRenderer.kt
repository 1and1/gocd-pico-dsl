package com.github.masooh.gocdpicodsl

import org.jgrapht.Graph
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.traverse.BreadthFirstIterator
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.introspector.Property
import org.yaml.snakeyaml.nodes.NodeTuple
import org.yaml.snakeyaml.nodes.Tag
import org.yaml.snakeyaml.representer.Representer

data class YamlPipeline(private val pipelineSingle: PipelineSingle) {
    val template
        get() = pipelineSingle.template

    val lock_behavior
        get() = pipelineSingle.lockBehavior

    val label_template : String
        get() {
            return "${'$'}{${materials.entries.first().key}}"
        }
    val group
        get() = pipelineSingle.group
    val parameters: Map<String, String>
        get() {
            return pipelineSingle.parameters.map {
                it.key to it.value.getValue()
            }.toMap()
        }

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
                else -> upstreamPipelines().map {
                    it.name to mapOf(
                            "pipeline" to it.name,
                            "stage" to it.lastStage
                    )
                }.toMap()
            }
        }

    private fun upstreamPipelines(): List<PipelineSingle> {
        val incomingEdges = graph.incomingEdgesOf(pipelineSingle)
        return incomingEdges.map { graph.getEdgeSource(it) }
    }
}

object NonNullRepresenter: Representer() {
    init {
        addClassTag(YamlPipeline::class.java, Tag.MAP)
    }
    override fun representJavaBeanProperty(javaBean: Any, property: Property, propertyValue: Any?, customTag: Tag?): NodeTuple? {
        // if value of property is null, ignore it.
        return when  {
            propertyValue == null -> null
            propertyValue is Map<*, *> && propertyValue.isEmpty() -> null
            else -> {
                super.representJavaBeanProperty(javaBean, property, propertyValue, customTag)
            }
        }
    }
}


fun Graph<PipelineSingle, DefaultEdge>.toYaml(): String {
    val options = DumperOptions()
    options.isPrettyFlow = true
    options.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
    options.isAllowReadOnlyProperties = true
    options.isCanonical = false

    val yaml = Yaml(NonNullRepresenter, options)

    val map = BreadthFirstIterator(this).asSequence().toList().map { it.name to YamlPipeline(it) }.toMap()
    return yaml.dump(map)
}

