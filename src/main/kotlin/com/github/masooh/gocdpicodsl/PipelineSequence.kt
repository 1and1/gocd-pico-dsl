package com.github.masooh.gocdpicodsl

import com.github.masooh.gocdpicodsl.dsl.*
import org.jgrapht.Graph
import org.jgrapht.alg.shortestpath.DijkstraShortestPath
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

    open fun getAllPipelines(): List<PipelineSingle> = pipelinesInGroup.flatMap { it.getAllPipelines() }

    abstract fun addPipelineGroupToGraph()

    fun pipeline(name: String, init: PipelineSingle.() -> Unit): PipelineSingle {
        val pipelineSingle = PipelineSingle(name)
        pipelineSingle.init()
        pipelineSingle.addPipelineGroupToGraph()

        pipelinesInGroup.add(pipelineSingle)

        return pipelineSingle
    }

    fun group(name: String, body: () -> Unit) {
        body()
        getAllPipelines().filter { it.group == null }.forEach {
            it.group = name
        }
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
        val lastPipeline = if (pipelinesInGroup.isNotEmpty()) pipelinesInGroup.last() as PipelineSingle else null
        val pipelineParallel = PipelineParallel(lastPipeline)
        pipelineParallel.init()
        pipelineParallel.addPipelineGroupToGraph()

        pipelinesInGroup.add(pipelineParallel)

        return pipelineParallel
    }
}

class PipelineParallel(private val forkPipeline: PipelineSingle?) : PipelineGroup() {
    override fun addPipelineGroupToGraph() {
        if (forkPipeline != null) {
            pipelinesInGroup.forEach { toGroup ->
                forkPipeline.getEndingPipelines().forEach { from ->
                    toGroup.getStartingPipelines().forEach { to ->
                        graph.addEdge(from, to)
                    }
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
    var lockBehavior: LockBehavior = LockBehavior.unlockWhenFinished

    val lastStage: String
        get() {
            return template?.stage ?: stages.last().name
        }

    var parameters = mutableMapOf<String, StringValue>()
    var environmentVariables = mutableMapOf<String, String>()

    var template : Template? = null
    var group : String? = null

    var stages : MutableList<Stage> = mutableListOf()
    var materials: Materials? = null

    override fun addPipelineGroupToGraph() {
        graph.addVertex(this)
    }

    override fun getStartingPipelines() = listOf(this)
    override fun getEndingPipelines() = listOf(this)
    override fun getAllPipelines() = listOf(this)

    fun parameter(key: String, value: String) {
        parameters[key] = SimpleStringValue(value)
    }

    fun parameter(key: String, value: () -> String) {
        parameters[key] = LambdaStringValue(value)
    }

    fun environment(key: String, value: String) {
        environmentVariables[key] = value
    }

    fun stage(name: String, manualApproval: Boolean = false, init: Stage.() -> Unit): Stage {
        val stage = Stage(name, manualApproval)
        stage.init()
        stages.add(stage)
        return stage
    }

    fun materials(init: Materials.() -> Unit): Materials {
        val materials = Materials()
        materials.init()
        this.materials = materials
        return materials
    }
}

fun PipelineSingle.shortestPath(to: PipelineSingle): String {
    val dijkstraAlg = DijkstraShortestPath(graph)
    val startPath = dijkstraAlg.getPaths(this)
    val upstreamPipelineName = startPath.getPath(to).edgeList
            .joinToString(separator = "/", postfix = "/${to.name}") { edge ->
                graph.getEdgeSource(edge).name
            }
    return upstreamPipelineName
}
