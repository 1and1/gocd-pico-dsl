package net.oneandone.gocd.picodsl.examples.registry

import net.oneandone.gocd.picodsl.dsl.*

fun PipelineGroup.deploy(
        name: String,
        block: PipelineSingle.() -> Unit = {}
) = deployPipeline(this, name, block)

fun SequenceContext.deploy(
        name: String,
        block: PipelineSingle.() -> Unit = {}
) = deployPipeline(this.pipelineGroup, name, block)

fun ParallelContext.deploy(
        name: String,
        block: PipelineSingle.() -> Unit = {}
) = deployPipeline(this.pipelineGroup, name, block)

private fun deployPipeline(pipelineGroup: PipelineGroup, name: String, block: PipelineSingle.() -> Unit) {
    pipelineGroup.pipeline(name, block).apply {
        template = deploy
        parameter("param1", "value1")
    }
}