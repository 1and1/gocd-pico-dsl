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

import net.oneandone.gocd.picodsl.dsl.Materials
import net.oneandone.gocd.picodsl.dsl.PipelineSingle
import net.oneandone.gocd.picodsl.dsl.Template
import net.oneandone.gocd.picodsl.dsl.gocd
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

    describe("Config with multiple environments and unassociated pipelines") {
        it("fails") {
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