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
package net.oneandone.gocd.picodsl.renderer

import net.oneandone.gocd.picodsl.dsl.GocdConfig
import net.oneandone.gocd.picodsl.dsl.PipelineSingle
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.io.DOTExporter
import java.io.StringWriter

fun GocdConfig.toDot(plantUmlWrapper: Boolean = false): String {
    val graph = this.pipelines.graph

    val writer = StringWriter()
    if (plantUmlWrapper) {
        writer.appendLine("@startuml")
    }
    val dotExporter = DOTExporter<PipelineSingle, DefaultEdge>(
            { it.name.replace("-", "_") },
            { "${it.name}\\n${it.template?.name}" },
            { "" }
    )
    dotExporter.putGraphAttribute("rankdir", "LR")
    dotExporter.exportGraph(graph, writer)
    if (plantUmlWrapper) {
        writer.appendLine("@enduml")
    }
    return writer.toString()
}
