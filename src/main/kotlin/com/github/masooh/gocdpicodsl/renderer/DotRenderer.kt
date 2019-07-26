package com.github.masooh.gocdpicodsl.renderer

import com.github.masooh.gocdpicodsl.dsl.PipelineSingle
import org.jgrapht.Graph
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.io.DOTExporter
import java.io.StringWriter

fun Graph<PipelineSingle, DefaultEdge>.toDot(plantUmlWrapper: Boolean = false): String {
    val writer = StringWriter()
    if (plantUmlWrapper) {
        writer.appendln("@startuml")
    }
    val dotExporter = DOTExporter<PipelineSingle, DefaultEdge>(
            { it.name.replace("-", "_") },
            { "${it.name}\\n${it.template?.name}" },
            { "" }
    )
    dotExporter.putGraphAttribute("rankdir", "LR")
    dotExporter.exportGraph(this, writer)
    if (plantUmlWrapper) {
        writer.appendln("@enduml")
    }
    return writer.toString()
}
