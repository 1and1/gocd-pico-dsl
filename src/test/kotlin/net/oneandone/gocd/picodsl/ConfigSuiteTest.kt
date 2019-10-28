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

import net.oneandone.gocd.picodsl.dsl.Template
import net.oneandone.gocd.picodsl.dsl.gocd
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

object ConfigSuiteTest : Spek({
    val outputFolder = File("target/gocd-config-${System.currentTimeMillis()}")

    val t1Template = Template("t", "stage")
    describe("simple pipeline") {
        val gocd = gocd {
            pipelines {
                sequence {
                    pipeline("p1") {
                        materials {
                            repoPackage("repo")
                        }
                        template = t1Template
                        group = "g"
                    }
                    pipeline("p2") {
                        group = "g"
                        stage("stage1") {
                            job("j1") {
                                script("echo 'bla'")
                            }
                        }
                    }
                }
            }
        }

        it("writes Gocd YAML") {
            val files = ConfigSuite(gocd, outputFolder = outputFolder).writeYamlFiles()

            assertEquals(1, files.size)
            assertTrue { files[0].name.endsWith("gocd.yaml") }
        }

        it("writes Dot") {
            val files = ConfigSuite(gocd, outputFolder = outputFolder).writeDotFiles()

            assertEquals(1, files.size)
            assertTrue { files[0].name.endsWith("dot") }
        }

        it("writes Plantuml") {
            val files = ConfigSuite(gocd, outputFolder = outputFolder).writePlantUmlDotFiles()

            assertEquals(1, files.size)
            assertTrue { files[0].name.endsWith("puml") }
        }
    }

    describe("named pipeline") {
        val gocd = gocd("named") {
            pipelines {
                sequence {
                    pipeline("p1") {
                        template = t1Template
                        group = "g"
                        materials {
                            repoPackage("repo")
                        }
                    }
                }
            }
        }

        it("has config name in file name") {
            val files = ConfigSuite(gocd, outputFolder = outputFolder).writeYamlFiles()

            assertTrue { files[0].name.endsWith("-named.gocd.yaml") }
        }
    }

    afterGroup {
        outputFolder.deleteRecursively()
    }

})