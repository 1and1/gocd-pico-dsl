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

import net.oneandone.gocd.picodsl.configs.template1
import net.oneandone.gocd.picodsl.configs.template2
import net.oneandone.gocd.picodsl.dsl.*
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlin.test.assertFailsWith

object ValidationTest : Spek({
    describe("Pipelines with missing attributes") {

        listOf(
                createPipeline(template = null),
                createPipeline(group = null),
                createPipeline(materials = null)
        ).forEach {
            it("is not valid") {
                assertFailsWith(IllegalArgumentException::class) {
                    gocd {
                        pipelines {
                            sequence {
                                pipelinesInContainer.add(it)
                            }
                        }
                    }
                }
            }
        }
    }

    describe("misc failures") {
        it("Config with multiple environments and unassociated pipelines") {
            assertFailsWith(IllegalArgumentException::class) {
                gocd {
                    environments {
                        environment("dev") { }
                        environment("qa") { }
                    }
                    pipelines {
                        sequence {
                            pipelinesInContainer.add(createPipeline())
                        }
                    }
                }
            }
        }
        it("fails if no starting pipeline is found for pathToPipeline") {
            assertFailsWith(IllegalArgumentException::class) {
                gocd {
                    pipelines {
                        sequence {
                            group("dev") {
                                pipeline("p1") {
                                    materials {
                                        repoPackage("material1")
                                    }
                                    template = template1
                                }
                                pipeline("p4") {
                                    template = template2
                                    graphProcessors.add {
                                        parameter("upstream", it.pathToPipeline(this) { pipeline ->
                                            pipeline.name == "does not exist"
                                        })
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

})

private fun createPipeline(
        template: Template? = Template("t1", "stage"),
        group: String? = "dev",
        materials: Materials? = Materials().apply {
            repoPackage("repo")
        }) = PipelineSingle("p1").apply {
    this.template = template
    this.group = group
    this.materials = materials
}