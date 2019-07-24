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
    val parameters
        get() = pipelineSingle.parameters
    val materials : Map<String, Map<String, String>>
        get() {
            val incomingEdges = graph.incomingEdgesOf(pipelineSingle)
            return incomingEdges.map { graph.getEdgeSource(it) }.map {
                it.name to (mapOf("pipeline" to it.name, "stage" to (it.template?.stage ?: "???")))
            }.toMap()
        }
}

data class Test(val name: String)


fun main() {
    val options = DumperOptions()
    options.isPrettyFlow = true
    options.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
    options.isAllowReadOnlyProperties = true

    val yaml = Yaml(options)
    println(yaml.dump(Test("bla")))

    println(yaml.dump(mapOf("a" to mapOf("c" to 3))))
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

    val yaml = Yaml(NonNullRepresenter, options)

    val map = BreadthFirstIterator(this).asSequence().toList().map { it.name to YamlPipeline(it) }.toMap()
    return yaml.dump(map)
}