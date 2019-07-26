package com.github.masooh.gocdpicodsl

import com.github.masooh.gocdpicodsl.dsl.Template
import com.github.masooh.gocdpicodsl.renderer.toDot
import com.github.masooh.gocdpicodsl.renderer.toYaml
import java.io.File

val prepareDeployment = Template("PREPARE-DEPLOYMENT", "prepare")
val deployOneStage = Template("DEPLOY-ONE-STAGE", "PREPARE-DEPLOY-VERIFY-TEST")

fun main() {
    val gocd = gocd {
        sequence {
            group("dev") {
                pipeline("prepare") {
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
                            pipeline("trinity") {
                                template = deployOneStage
                            }
                            deploy("ni") {
                                template = Template("foo", "sdklfj")
                            }
                        }
                    }
                }
                pipeline("promote") {
                    // todo stage
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

    }

    gocd.graph.edgeSet().forEach { edge ->
        println("$edge")
    }

    File("graph.yml").writeText(gocd.graph.toYaml())
    File("graph.dot").writeText(gocd.graph.toDot(plantUmlWrapper = true))
}

private fun PipelineGroup.deploy(name: String, block: PipelineSingle.() -> Unit = {}) {
    pipeline(name) {
        template = deployOneStage
    }.apply(block)
}