package com.github.masooh.gocdpicodsl

val prepareDeployment = Template("PREPARE-DEPLOYMENT", "prepare")
val deployOneStage = Template("DEPLOY-ONE-STAGE", "PREPARE-DEPLOY-VERIFY-TEST")

fun main() {
    sequence {
        group("dev") {
            val prepare = pipeline("prepare") {
                template = prepareDeployment
                group = "init"
                parameter("a", "b")
                materials {
                    repoPackage("staging-package")
                }
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
                        /* todo pipeline(trinityArtifact, "deploy") -> basierend auf artifact upstream finden
                             Achtung ist ACDC spezifisch
                         */
                        pipeline("trinity") {
                            template = deployOneStage
                            parameter("UPSTREAM_PIPELINE_NAME") {
                                prepare.shortestPath(this)
                            }
                        }
                        deploy("ni") {
                            template = Template("foo", "sdklfj")
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
        }
        group("qa") {
            pipeline("prepare-qa") {
                template = prepareDeployment
            }
        }
    }

    graph.edgeSet().forEach { edge ->
        println("$edge")
    }

    println(graph.toYaml())
    println(graph.toDot(plantUmlWrapper = true))
}

private fun PipelineGroup.deploy(name: String, block: PipelineSingle.() -> Unit = {}) {
    pipeline(name) {
        template = deployOneStage
    }.apply(block)
}