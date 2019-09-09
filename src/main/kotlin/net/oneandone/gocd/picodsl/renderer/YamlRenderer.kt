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

import net.oneandone.gocd.picodsl.dsl.PipelineSingle
import net.oneandone.gocd.picodsl.dsl.Script
import net.oneandone.gocd.picodsl.renderer.yaml.YamlConfig
import net.oneandone.gocd.picodsl.renderer.yaml.YamlJob
import net.oneandone.gocd.picodsl.renderer.yaml.YamlPipeline
import net.oneandone.gocd.picodsl.renderer.yaml.YamlStage
import org.jgrapht.Graph
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.traverse.BreadthFirstIterator
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.introspector.Property
import org.yaml.snakeyaml.nodes.NodeTuple
import org.yaml.snakeyaml.nodes.Tag
import org.yaml.snakeyaml.representer.Representer

object NonNullRepresenter: Representer() {
    /* remove class markers like  !!net.oneandone.gocd.picodsl.renderer.YamlPipeline */
    init {
        addClassTag(YamlConfig::class.java, Tag.MAP)
        addClassTag(YamlPipeline::class.java, Tag.MAP)
        addClassTag(YamlStage::class.java, Tag.MAP)
        addClassTag(YamlJob::class.java, Tag.MAP)
        addClassTag(Script::class.java, Tag.MAP)
    }

    /** Ignore null values and empty maps */
    override fun representJavaBeanProperty(javaBean: Any, property: Property, propertyValue: Any?, customTag: Tag?): NodeTuple? {
        return when  {
            propertyValue == null -> null
            propertyValue is Map<*, *> && propertyValue.isEmpty() -> null
            propertyValue is List<*> && propertyValue.isEmpty() -> null
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

    val listOfPipelines = BreadthFirstIterator(this).asSequence().toList()
    val config = YamlConfig(listOfPipelines, this)
    return yaml.dump(config)
}
