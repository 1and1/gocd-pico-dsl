package com.github.masooh.gocdpicodsl

import org.jgrapht.Graph
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.SimpleDirectedGraph

/* todo prüfen, wie Klassen struktur ist, wo sind statisch Einstiege. Was mache ich mit Graph
      visitor pattern für Graph?
 */
val graph: Graph<PipelineSingle, DefaultEdge> = SimpleDirectedGraph(DefaultEdge::class.java)

sealed class PipelineGroup {
    val pipelinesInGroup = mutableListOf<PipelineGroup>()

    abstract fun getEndingPipelines(): List<PipelineSingle>
    abstract fun getStartingPipelines(): List<PipelineSingle>
    abstract fun addPipelineGroupToGraph()

    fun pipeline(name: String, init: PipelineSingle.() -> Unit): PipelineSingle {
        val pipelineSingle = PipelineSingle(name)
        pipelineSingle.init()
        pipelineSingle.addPipelineGroupToGraph()

        pipelinesInGroup.add(pipelineSingle)

        return pipelineSingle
    }
}

fun sequence(parent: PipelineParallel? = null, init: PipelineSequence.() -> Unit): PipelineSequence {
    val pipelineSequence = PipelineSequence()
    pipelineSequence.init()
    pipelineSequence.addPipelineGroupToGraph()

    parent?.pipelinesInGroup?.add(pipelineSequence)
    return pipelineSequence
}

class PipelineSequence : PipelineGroup() {
    override fun addPipelineGroupToGraph() {
        var fromGroup = pipelinesInGroup.first()

        pipelinesInGroup.drop(1).forEach { toGroup ->
                fromGroup.getEndingPipelines().forEach { from ->
                    toGroup.getStartingPipelines().forEach { to ->
                        graph.addEdge(from, to)
                    }
                }
                fromGroup = toGroup
        }
    }

    override fun getStartingPipelines(): List<PipelineSingle> = pipelinesInGroup.first().getStartingPipelines()
    override fun getEndingPipelines(): List<PipelineSingle> = pipelinesInGroup.last().getEndingPipelines()

    fun parallel(init: PipelineParallel.() -> Unit): PipelineParallel {
        val lastPipeline = pipelinesInGroup.last() as PipelineSingle
        val pipelineParallel = PipelineParallel(lastPipeline)
        pipelineParallel.init()
        pipelineParallel.addPipelineGroupToGraph()

        pipelinesInGroup.add(pipelineParallel)

        return pipelineParallel
    }
}

class PipelineParallel(private val forkPipeline: PipelineSingle) : PipelineGroup() {
    override fun addPipelineGroupToGraph() {
        pipelinesInGroup.forEach { toGroup ->
            forkPipeline.getEndingPipelines().forEach { from ->
                toGroup.getStartingPipelines().forEach { to ->
                    graph.addEdge(from, to)
                }
            }
        }
    }

    override fun getStartingPipelines() = pipelinesInGroup.flatMap { it.getStartingPipelines() }
    override fun getEndingPipelines() = pipelinesInGroup.flatMap { it.getEndingPipelines() }

    fun sequence(init: PipelineSequence.() -> Unit): PipelineSequence {
        return sequence(this, init)
    }
}

data class PipelineSingle(val name: String) : PipelineGroup() {
    var parameters : Map<String, String>? = null
    override fun addPipelineGroupToGraph() {
        graph.addVertex(this)
    }

    override fun getStartingPipelines() = listOf(this)
    override fun getEndingPipelines() = listOf(this)

    fun template(name: String) {
    }

    fun parameters(vararg parameters: Pair<String, String>) {
        this.parameters = parameters.toMap()
    }
}

