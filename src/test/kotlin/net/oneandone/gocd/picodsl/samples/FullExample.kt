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
package net.oneandone.gocd.picodsl.samples

import net.oneandone.gocd.picodsl.ConfigSuite
import net.oneandone.gocd.picodsl.dsl.*
import net.oneandone.gocd.picodsl.renderer.toDot
import net.oneandone.gocd.picodsl.renderer.toYaml
import java.io.File

val testing = Template("testing", "testing-stage")
val deploy = Template("deploy", "deploy-stage")

val prepareEnvironment = GocdEnvironment("prepareEnv").envVar("envKey", "envPrepare")
val testingEnvironment = GocdEnvironment("testingEnv").envVar("envKey", "envTesting")

fun main(args: Array<String>) {
    val gocd1 = gocd {
        pipelines {
            sequence {
                group("dev") {
                    forAll {
                        if (environment == null) {
                            environment = testingEnvironment
                        }
                    }

                    pipeline("prepare") {
                        template = testing
                        group = "init"
                        environment = prepareEnvironment
                        parameter("param1", "value1")
                        materials {
                            repoPackage("broker")
                        }
                    }
                    pipeline("migration") {
                        template = deploy
                    }
                    parallel {
                        pipeline("one") {
                            template = deploy
                        }
                        sequence {
                            pipeline("two") {
                                template = deploy
                            }
                            parallel {
                                pipeline("three") {
                                    template = deploy
                                }
                                pipeline("four") {
                                    template = deploy
                                }
                                deploy("five") {
                                    template = Template("foo", "sdklfj")
                                }
                            }
                        }
                    }
                    pipeline("promote") {
                        stage("APPROVE", manualApproval = true) {
                            job("approve") {
                                script("""
                        echo "executing a script"
                        echo "print envKey: '${'$'}{envKey}'"
                    """.trimIndent())
                            }
                        }
                    }
                }
                group("qa") {
                    pipeline("prepare-qa") {
                        template = testing
                        // todo bind parameter to template
                        parameter("param1", "value2")
                        environment = prepareEnvironment
                    }
                }
            }
        }
    }

    gocd1.pipelines.graph.edgeSet().forEach { edge ->
        println("$edge")
    }

    File("graph.yml").writeText(gocd1.toYaml())
    File("graph.dot").writeText(gocd1.toDot(plantUmlWrapper = true))

    val gocd2 = gocd("second-pipeline") {
        environments() {
            environment("devEnv") {}
        }
        pipelines {
            sequence {
                deploy("first") {
                    group = "dev"
                    materials {
                        repoPackage("euss")
                    }
                }
            }
        }
    }

    val outputFolder = if (args.isNotEmpty()) args[0] else "target/gocd-config"
    ConfigSuite(gocd1, gocd2, outputFolder = File(outputFolder)).writeYamlFiles()
}

private fun PipelineGroup.deploy(name: String, block: PipelineSingle.() -> Unit = {}) {
    this.pipeline(name, block).apply {
        template = deploy
    }
}