package com.github.masooh.gocdpicodsl

@DslMarker
annotation class PipelinesDslMarker

//@PipelinesDslMarker

abstract class Pipelines {
    protected val pipelines = mutableListOf<Pipeline>()

    fun pipeline(name: String, init: Pipeline.() -> Unit) {
        val pipeline = Pipeline(name)
        pipeline.init()
        pipeline.materials.addAll(lastElements())
        pipelines.add(pipeline)
    }

    abstract fun lastElements(): Collection<Material>
}

class PipelinesParallel : Pipelines() {
    override fun lastElements(): Collection<Material> {

    }

}
class PipelineSequence : Pipelines( ){
    override fun lastElements() = listOf(pipelines.last())
}

open class Material(val name: String)

class Pipeline(name: String) : Material(name) {
    val materials = mutableListOf<Material>()

    fun template(name: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun parameters(vararg map: Pair<String, String>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

fun sequence(init: PipelineSequence.() -> Unit): PipelineSequence {
    return PipelineSequence()
}

fun parallel(init: PipelineSequence.() -> Unit): PipelineSequence {
    return PipelineSequence()
}

fun main() {
    sequence {
        (1..3).forEach {
            pipeline(it.toString()) { }
        }
        pipeline("migration") {
            template("")
            parameters(
                    "a" to "b",
                    "b" to "c")
        }
        parallel {
            pipeline("crms") {
                template("")
            }
            sequence {
                pipeline("keyservice") {
                    template("")
                }
                parallel {
                    pipeline("ni") { }
                    pipeline("trinity") { }
                }
            }
        }
        pipeline("promote") {
            template("")
        }
    }
}