package com.github.masooh.gocdpicodsl

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
        val groupName = name
        body()
        getAllPipelines().filter { it.group == null }.forEach {
            it.group = groupName
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

enum class LockBehavior {
    lockOnFailure,
    unlockWhenFinished,
    none
}

interface StringValue {
    fun getValue() : String
}

class SimpleStringValue(val simpleString: String) : StringValue {
    override fun getValue(): String {
        return simpleString
    }
}

class LambdaStringValue(val lambda: () -> String) : StringValue {
    override fun getValue(): String {
        return lambda()
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

class Materials {
    var materials = mutableListOf<Material>()

    /** package is a keyword, method therefore renamed to repoPackage */
    fun repoPackage(name: String) {
        val repoPackage = Package(name)
        materials.add(repoPackage)
    }
}
sealed class Material(val name: String)
class Package(name: String) : Material(name)


data class Template(val name: String, val stage: String)
data class Group(val name: String)

data class Stage(val name: String, val manualApproval: Boolean = false) {
    var jobs : MutableList<Job> = mutableListOf()

    fun job(name: String, init: Job.() -> Unit): Job {
        val job = Job(name)
        job.init()
        jobs.add(job)
        return job
    }
}
data class Job(val name: String) {
    val tasks: MutableList<Task> = mutableListOf()

    fun script(script: String) {
        tasks.add(Script(script))
    }
}

interface Task
data class Script(val script: String) : Task

fun PipelineSingle.shortestPath(to: PipelineSingle): String {
    val dijkstraAlg = DijkstraShortestPath(graph)
    val startPath = dijkstraAlg.getPaths(this)
    val upstreamPipelineName = startPath.getPath(to).edgeList
            .joinToString(separator = "/", postfix = "/${to.name}") { edge ->
                graph.getEdgeSource(edge).name
            }
    return upstreamPipelineName
}
