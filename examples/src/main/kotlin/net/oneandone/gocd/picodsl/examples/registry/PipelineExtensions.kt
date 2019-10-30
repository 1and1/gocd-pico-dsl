package net.oneandone.gocd.picodsl.examples.registry

import net.oneandone.gocd.picodsl.dsl.PipelineGroup
import net.oneandone.gocd.picodsl.dsl.PipelineSingle

fun PipelineGroup.deploy(name: String, block: PipelineSingle.() -> Unit = {}) {
    this.pipeline(name, block).apply {
        template = deploy
    }
}