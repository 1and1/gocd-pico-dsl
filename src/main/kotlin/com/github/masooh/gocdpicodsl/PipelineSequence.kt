package com.github.masooh.gocdpicodsl

import org.jgrapht.Graph
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.SimpleDirectedGraph
import org.jgrapht.graph.SimpleGraph

val graph : Graph<PipelineSingle, DefaultEdge> = SimpleDirectedGraph(DefaultEdge::class.java)

sealed class PipelineGroup


class PipelineSequence : PipelineGroup() {
    var lastPipeline: PipelineGroup? = null

    fun pipeline(name: String, init: PipelineSingle.() -> Unit): PipelineSingle {
        val pipelineSingle = PipelineSingle(name)
        pipelineSingle.init()
        lastPipeline = pipelineSingle

        // find all open endings
        val openEndings = graph.vertexSet().filter { graph.outgoingEdgesOf(it).isEmpty() }
        graph.vertexSet().forEach {
            println("$it: ${graph.outgoingEdgesOf(it)}")
        }

        graph.addVertex(pipelineSingle)

        if (graph.vertexSet().size >= 2 && openEndings.isNotEmpty()) {
            openEndings.forEach {
                graph.addEdge(it, pipelineSingle)
            }
        }
        return pipelineSingle
    }

    fun parallel(init: PipelineParallel.() -> Unit): PipelineParallel {
        assert(lastPipeline is PipelineSingle) { "parallel{} must pipeline{}"}
        val pipelineParallel = PipelineParallel(lastPipeline as PipelineSingle)
        pipelineParallel.init()
        lastPipeline = pipelineParallel
        return pipelineParallel
    }
}

class PipelineParallel(val forkPipeline: PipelineSingle) : PipelineGroup() {
    fun sequence(init: PipelineSequence.() -> Unit): PipelineSequence {
        val pipelineParallel = PipelineSequence()
        pipelineParallel.init()
        return pipelineParallel
    }

    fun pipeline(name: String, init: PipelineSingle.() -> Unit): PipelineSingle {
        val pipelineSingle = PipelineSingle(name)
        pipelineSingle.init()

        graph.addVertex(pipelineSingle)
        graph.addEdge(forkPipeline, pipelineSingle)
        return pipelineSingle
    }
}

data class PipelineSingle(val name: String) : PipelineGroup() {
    fun template(name: String) {
    }

    fun parameters(vararg parameters: Pair<Any, Any>) {
    }
}



fun pipelines(init: PipelineSequence.() -> Unit): PipelineSequence {
    val pipelineSequence = PipelineSequence()
    pipelineSequence.init()
    return pipelineSequence
}

