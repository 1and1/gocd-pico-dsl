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
package net.oneandone.gocd.picodsl.dsl

import org.jgrapht.Graph
import org.jgrapht.alg.shortestpath.DijkstraShortestPath
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.SimpleDirectedGraph
import org.jgrapht.traverse.BreadthFirstIterator
import java.lang.StringBuilder
import java.util.*

@DslMarker
annotation class PipelinePicoDslMarker

@PipelinePicoDslMarker
sealed class Context(val pipelineGroup: PipelineGroup) {
    val data = mutableMapOf<String, String>()
    val postProcessors: MutableList<(PipelineSingle) -> Unit> = mutableListOf()

    fun forAll(enhancePipeline: PipelineSingle.() -> Unit) {
        postProcessors.add(enhancePipeline)
    }

    fun pipeline(name: String, block: PipelineSingle.() -> Unit): PipelineSingle {
        return pipelineGroup.pipeline(name, block)
    }

    fun stubPipeline(name: String, block: PipelineSingle.() -> Unit): PipelineSingle {
        return pipelineGroup.stubPipeline(name, block)
    }

}

class ParallelContext(val pipelineParallel: PipelineParallel) : Context(pipelineParallel) {
    fun sequence(init: PipelineSequence.() -> Unit): PipelineSequence {
        return pipelineParallel.sequence(init)
    }
}

class SequenceContext(val pipelineSequence: PipelineSequence) : Context(pipelineSequence) {
    fun parallel(init: PipelineParallel.() -> Unit): PipelineParallel {
        return pipelineSequence.parallel(init)
    }
}

object ContextStack {
    val context: Deque<Context> = LinkedList()
    val current: Context?
        get() = context.peekLast()
}

@PipelinePicoDslMarker
abstract class PipelineContainer {
    val pipelinesInContainer = mutableListOf<PipelineContainer>()
    val graphProcessors = mutableListOf<PipelineSingle.(Graph<PipelineSingle, DefaultEdge>) -> Unit>()

    abstract fun getEndingPipelines(): List<PipelineSingle>
    abstract fun getStartingPipelines(): List<PipelineSingle>

    open fun getAllPipelines(): List<PipelineSingle> = pipelinesInContainer.flatMap { it.getAllPipelines() }
    abstract fun addPipelineGroupToGraph(graph: Graph<PipelineSingle, DefaultEdge>)

    open fun runGraphProcessors(graph: Graph<PipelineSingle, DefaultEdge>) {
        pipelinesInContainer.forEach { it.runGraphProcessors(graph) }
    }

    open fun finish() {
        pipelinesInContainer.forEach(PipelineContainer::finish)
    }
}

sealed class PipelineGroup : PipelineContainer() {

    /** Creates pipeline and adds it to container */
    fun pipeline(name: String, block: PipelineSingle.() -> Unit): PipelineSingle {
        val pipelineSingle = PipelineSingle(name)
        pipelineSingle.block()

        pipelinesInContainer.add(pipelineSingle)

        return pipelineSingle
    }

    fun stubPipeline(name: String, block: PipelineSingle.() -> Unit): PipelineSingle {
        return pipeline(name, block).apply {
            stub = true
        }
    }

    fun context(init: Context.() -> Unit = {}, block: Context.() -> Unit): Context {
        val context = createContext()
        ContextStack.context.add(context)

        context.init()
        context.block()

        getAllPipelines().forEach { pipeline ->
            context.postProcessors.forEach { processor ->
                pipeline.apply(processor)
            }
        }

        ContextStack.context.removeLast()
        return context
    }

    abstract fun createContext(): Context
}

@PipelinePicoDslMarker
/**
 * @param name if defined this will be used as the name of the YAML (otherwise name is indexed)
 */
class GocdConfig(val name: String? = null) {
    val pipelines = GocdPipelines()
    val environments = GocdEnvironments()

    fun pipelines(block: GocdPipelines.() -> Unit) {
        pipelines.block()
    }

    fun environments(vararg environmentsToAdd: GocdEnvironment, block: GocdEnvironments.() -> Unit) {
        environments.add(*environmentsToAdd)
        environments.block()
    }

    fun finish(): GocdConfig {
        pipelines.finish()

        // use all environments which are referenced in pipelines
        pipelines.pipelines().forEach { pipeline ->
            pipeline.environment?.let {
                environments.environments.add(it)
            }
        }

        validate()
        return this
    }

    private fun validate() {
        environments.environments.forEach {
            require(it.pipelines.isNotEmpty() || environments.environments.size == 1) {
                "If environment does not list pipelines only one environment is allowed."
            }
        }
    }
}

@PipelinePicoDslMarker
class GocdEnvironments {
    val environments = mutableSetOf<GocdEnvironment>()

    fun environment(name: String, block: GocdEnvironment.() -> Unit = {}): GocdEnvironment {
        val environment = GocdEnvironment(name).apply(block)
        environments.add(environment)
        return environment
    }

    fun add(vararg environmentsToAdd: GocdEnvironment) {
        environments.addAll(environmentsToAdd)
    }

    /** alias for add */
    fun environment(vararg environmentsToAdd: GocdEnvironment) {
        add(*environmentsToAdd)
    }
}

@PipelinePicoDslMarker
class GocdEnvironment(val name: String) {
    val environmentVariables = mutableMapOf<String, String>()
    val pipelines = mutableListOf<PipelineSingle>()

    fun envVar(key: String, value: String): GocdEnvironment {
        environmentVariables[key] = value
        return this
    }

    fun addPipeline(pipelineSingle: PipelineSingle) {
        pipelines.add(pipelineSingle)
    }
}

@PipelinePicoDslMarker
class GocdPipelines {
    val graph: Graph<PipelineSingle, DefaultEdge> = SimpleDirectedGraph(DefaultEdge::class.java)

    val pipelineGroups: MutableList<PipelineGroup> = mutableListOf()

    fun sequence(init: PipelineSequence.() -> Unit): PipelineSequence {
        val pipelineSequence = PipelineSequence()
        pipelineSequence.init()
        pipelineSequence.addPipelineGroupToGraph(graph)

        pipelineGroups.add(pipelineSequence)
        return pipelineSequence
    }

    fun parallel(init: PipelineParallel.() -> Unit): PipelineParallel {
        val pipelineParallel = PipelineParallel(null)
        pipelineParallel.init()
        pipelineParallel.addPipelineGroupToGraph(graph)

        pipelineGroups.add(pipelineParallel)
        return pipelineParallel
    }

    fun finish(): GocdPipelines {
        pipelineGroups.forEach {
            it.runGraphProcessors(graph)
            it.finish()
        }
        validate()
        return this
    }

    fun pipelines() = BreadthFirstIterator(graph).asSequence().toList()

    private fun validate() {
        val pipeLineWithoutStageOrTemplate = graph.vertexSet().find { pipeline ->
            pipeline.template == null && pipeline.stages.size == 0
        }

        pipeLineWithoutStageOrTemplate?.let {
            throw IllegalArgumentException("pipeline ${it.name} has neither template nor stage")
        }

        val pipelineWithoutMaterialAndUpstream = graph.vertexSet().find { pipeline ->
            (pipeline.materials?.isEmpty() ?: true) &&
                    graph.upstreamPipelines(pipeline).isEmpty() &&
                    !pipeline.stub
        }

        pipelineWithoutMaterialAndUpstream?.let {
            throw IllegalArgumentException("pipeline ${it.name} has neither material nor upstream pipeline")
        }

        val pipelineWithoutGroup = graph.vertexSet().find { pipeline ->
            pipeline.group == null
        }

        pipelineWithoutGroup?.let {
            throw IllegalArgumentException("pipeline ${it.name} has no group")
        }
    }
}

fun gocd(name: String? = null, init: GocdConfig.() -> Unit) = GocdConfig(name).apply(init).finish()

class PipelineSequence : PipelineGroup() {
    override fun createContext() = SequenceContext(this)

    override fun addPipelineGroupToGraph(graph: Graph<PipelineSingle, DefaultEdge>) {
        pipelinesInContainer.forEach { it.addPipelineGroupToGraph(graph) }

        var fromGroup = pipelinesInContainer.first()

        pipelinesInContainer.drop(1).forEach { toGroup ->
            fromGroup.getEndingPipelines().forEach { from ->
                toGroup.getStartingPipelines().forEach { to ->
                    graph.addEdge(from, to)
                }
            }
            fromGroup = toGroup
        }
    }

    override fun getStartingPipelines(): List<PipelineSingle> = pipelinesInContainer.first().getStartingPipelines()
    override fun getEndingPipelines(): List<PipelineSingle> = pipelinesInContainer.last().getEndingPipelines()

    fun parallel(init: PipelineParallel.() -> Unit): PipelineParallel {
        val lastPipeline = pipelinesInContainer.lastOrNull() as PipelineSingle?
        val pipelineParallel = PipelineParallel(lastPipeline)
        pipelineParallel.init()

        pipelinesInContainer.add(pipelineParallel)

        return pipelineParallel
    }

    fun group(groupName: String, block: SequenceContext.() -> Unit): SequenceContext {
        return context({
            postProcessors.add { pipeline ->
                if (pipeline.group == null) {
                    pipeline.group = groupName
                }
            }
        }, block as Context.() -> Unit) as SequenceContext
    }
}

class PipelineParallel(private val forkPipeline: PipelineSingle?) : PipelineGroup() {
    override fun createContext() = ParallelContext(this)

    override fun addPipelineGroupToGraph(graph: Graph<PipelineSingle, DefaultEdge>) {
        pipelinesInContainer.forEach { it.addPipelineGroupToGraph(graph) }
        if (forkPipeline != null) {
            pipelinesInContainer.forEach { toGroup ->
                forkPipeline.getEndingPipelines().forEach { from ->
                    toGroup.getStartingPipelines().forEach { to ->
                        graph.addEdge(from, to)
                    }
                }
            }
        }
    }

    override fun getStartingPipelines() = pipelinesInContainer.flatMap { it.getStartingPipelines() }
    override fun getEndingPipelines() = pipelinesInContainer.flatMap { it.getEndingPipelines() }

    fun sequence(init: PipelineSequence.() -> Unit): PipelineSequence {
        val pipelineSequence = PipelineSequence()
        pipelineSequence.init()

        this.pipelinesInContainer.add(pipelineSequence)
        return pipelineSequence
    }

    fun group(groupName: String, block: ParallelContext.() -> Unit): ParallelContext {
        return context({
            postProcessors.add { pipeline ->
                if (pipeline.group == null) {
                    pipeline.group = groupName
                }
            }
        }, block as Context.() -> Unit) as ParallelContext
    }
}

class PipelineSingle(val name: String) : PipelineContainer() {
    val tags = mutableMapOf<String, String>()
    val definitionException = IllegalArgumentException(name)
    var stub = false

    override fun toString(): String {
        val builder = StringBuilder()
        builder.append("pipeline(name=$name")

        if (tags.isNotEmpty()) {
            builder.append(", tags=$tags")
        }
        if (stub) {
            builder.append(", stub=true")
        }
        builder.append(")")
        return builder.toString()
    }

    init {
        require(name.isNotBlank()) { "pipeline must be named" }

        definitionException.fillInStackTrace()
        val filtered = definitionException.stackTrace.filter { !it.className.startsWith("net.oneandone.gocd.picodsl.dsl") }

        definitionException.stackTrace = filtered.toTypedArray()
    }

    /**
     *  Tag is not represented in yaml config.
     **/
    fun tag(key: String, value: String) {
        tags[key] = value
    }

    var lockBehavior = LockBehavior.unlockWhenFinished

    val lastStage: String
        get() =
            requireNotNull(template?.lastStage ?: stages.lastOrNull()?.name) {
                "Pipeline has neither template nor stages"
            }


    val parameters = mutableMapOf<String, String>()
    val environmentVariables = mutableMapOf<String, String>()

    var template: Template? = null
    var group: String? = null
    var environment: GocdEnvironment? = null

    // todo trennung zwischen Builder f. DSL und Objekt
    val stages: MutableList<Stage> = mutableListOf()
    var materials: Materials? = null
    var timer: QuartzTimer? = null

    override fun addPipelineGroupToGraph(graph: Graph<PipelineSingle, DefaultEdge>) {
        graph.addVertex(this)
    }

    override fun runGraphProcessors(graph: Graph<PipelineSingle, DefaultEdge>) {
        graphProcessors.forEach { processor ->
            this.apply { processor(graph) }
        }
    }

    override fun getStartingPipelines() = listOf(this)
    override fun getEndingPipelines() = listOf(this)
    override fun getAllPipelines() = listOf(this)

    fun parameter(key: String, value: String) {
        parameters[key] = value
    }

    fun envVar(key: String, value: String) {
        environmentVariables[key] = value
    }

    fun envVar(key: String, value: Any) {
        environmentVariables[key] = value.toString()
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

    override fun finish() {
        environment?.addPipeline(this)
    }

    fun timer(spec: String, onlyOnChanges: Boolean? = null) {
        timer = QuartzTimer(spec, onlyOnChanges)
    }
}

fun Graph<PipelineSingle, DefaultEdge>.pathToPipeline(to: PipelineSingle, startMatcher: (PipelineSingle) -> Boolean): String {
    val startPipelineCandidates: List<PipelineSingle> = this.vertexSet().filter(startMatcher)
    val dijkstraAlg = DijkstraShortestPath(this)

    val shortestPath = startPipelineCandidates.mapNotNull { startCandidate ->
        dijkstraAlg.getPaths(startCandidate).getPath(to)?.edgeList
    }.minBy { it.size } ?: throw IllegalArgumentException("no path found to $to", to.definitionException)

    return shortestPath.joinToString(separator = "/") { edge ->
        this.getEdgeSource(edge).name
    }
}

fun Graph<PipelineSingle, DefaultEdge>.upstreamPipelines(pipelineSingle: PipelineSingle): List<PipelineSingle> {
    val incomingEdges = this.incomingEdgesOf(pipelineSingle)
    return incomingEdges.map { this.getEdgeSource(it) }
}
