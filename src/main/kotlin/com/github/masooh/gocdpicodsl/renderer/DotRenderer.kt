package com.github.masooh.gocdpicodsl.renderer

import com.github.masooh.gocdpicodsl.PipelineSingle
import org.jgrapht.Graph
import org.jgrapht.graph.DefaultEdge
import java.io.StringWriter

val PipelineSingle.dotName
    get() = name.replace("-", "_")

fun Graph<PipelineSingle, DefaultEdge>.toDot(plantUmlWrapper: Boolean = false): String {
    val writer = StringWriter()
    if (plantUmlWrapper) {
        writer.appendln("@startuml")
    }
    writer.appendln("digraph Pipelines {")

    this.vertexSet().forEach { pipeline ->
        writer.appendln("    ${pipeline.dotName} [label=\"${pipeline.dotName}\\n${pipeline.template?.name}\"];")
    }

    writer.appendln()

    this.edgeSet().forEach { edge ->
        writer.appendln("    ${this.getEdgeSource(edge).dotName} -> ${this.getEdgeTarget(edge).dotName};")
    }

    writer.appendln("}")
    if (plantUmlWrapper) {
        writer.appendln("@enduml")
    }
    return writer.toString()
}
