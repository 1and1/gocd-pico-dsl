package com.github.masooh.gocdpicodsl

import org.jgrapht.Graph
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.traverse.BreadthFirstIterator
import java.io.StringWriter

fun Graph<PipelineSingle, DefaultEdge>.toYaml(): String {
    val writer = StringWriter()
    BreadthFirstIterator(this).forEach { pipeline ->
        writer.append("""
            ${pipeline.name}:
        """.trimIndent())
        if (pipeline.parameters != null) {
            writer.append("""
                parameters:
                    ${pipeline.parameters}
            """.trimIndent())
        }
    }
    return writer.toString()
}