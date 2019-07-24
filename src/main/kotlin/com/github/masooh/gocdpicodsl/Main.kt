package com.github.masooh.gocdpicodsl

import org.jgrapht.alg.shortestpath.DijkstraShortestPath

val devGroup = Group("dev")

val prepareDeployment = Template("PREPARE-DEPLOYMENT", "prepare")
val deployOneStage = Template("DEPLOY-ONE-STAGE", "PREPARE-DEPLOY-VERIFY-TEST")

fun main() {
    sequence {
        val prepare = pipeline("prepare") {
            pack("staging")
            template = prepareDeployment
            group = devGroup
            parameter("a", "b")
        }
        pipeline("migration") {
            template = deployOneStage
        }
        parallel {
            pipeline("crms") {
                template = deployOneStage
            }
            sequence {
                pipeline("keyservice") {
                    template = deployOneStage
                }
                parallel {
                    pipeline("ni") {
                        template = deployOneStage
                    }
                    pipeline("trinity") {
                        template = deployOneStage
                        parameter("UPSTREAM_PIPELINE_NAME") {
                            shortestPath(prepare, this)
                        }
                    }
                }
            }
        }
        pipeline("promote") {
            stage("APPROVE", manualApproval = true) {
                job("approve") {
                    script("""
                        echo "whatever"
                        do something
                        ${'$'}{ARTIFACT_GROUPID}:${'$'}{ARTIFACT_ID}:${'$'}{GO_PIPELINE_LABEL}
                    """.trimIndent())
                }
            }
        }
        pipeline("prepare-qa") {
            template = prepareDeployment
        }
    }

    graph.edgeSet().forEach { edge ->
        println("$edge")
    }

    println(graph.toYaml())
}

private fun shortestPath(from: PipelineSingle, to: PipelineSingle): String {
    val dijkstraAlg = DijkstraShortestPath(graph)
    val startPath = dijkstraAlg.getPaths(from)
    val upstreamPipelineName = startPath.getPath(to).edgeList
            .joinToString(separator = "/", postfix = "/${to.name}") { edge ->
                graph.getEdgeSource(edge).name
            }
    return upstreamPipelineName
}
