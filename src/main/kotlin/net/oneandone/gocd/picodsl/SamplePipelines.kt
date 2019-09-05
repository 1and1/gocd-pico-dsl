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
package net.oneandone.gocd.picodsl

import net.oneandone.gocd.picodsl.dsl.PipelineGroup
import net.oneandone.gocd.picodsl.dsl.PipelineSingle
import net.oneandone.gocd.picodsl.dsl.Template
import net.oneandone.gocd.picodsl.dsl.gocd
import net.oneandone.gocd.picodsl.renderer.toDot
import net.oneandone.gocd.picodsl.renderer.toYaml
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