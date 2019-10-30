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
package net.oneandone.gocd.picodsl.examples.registry

import net.oneandone.gocd.picodsl.RegisteredGocdConfig
import net.oneandone.gocd.picodsl.dsl.GocdEnvironment
import net.oneandone.gocd.picodsl.dsl.Template

val testing = Template("testing", "testing-stage")
val deploy = Template("deploy", "deploy-stage")

val prepareEnvironment = GocdEnvironment("prepareEnv").envVar("envKey", "envPrepare")
val testingEnvironment = GocdEnvironment("testingEnv").envVar("envKey", "envTesting")

object First : RegisteredGocdConfig({
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
                            script(
                                """
                                echo "executing a script"
                                echo "print envKey: '${'$'}{envKey}'"
                                """.trimIndent()
                            )
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
})