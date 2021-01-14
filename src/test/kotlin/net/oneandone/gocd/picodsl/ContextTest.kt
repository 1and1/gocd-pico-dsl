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

import net.oneandone.gocd.picodsl.dsl.ContextStack
import net.oneandone.gocd.picodsl.dsl.Template
import net.oneandone.gocd.picodsl.dsl.gocd
import org.assertj.core.api.Assertions.assertThat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object ContextTest : Spek({
    describe("misc failures in Pipeline model") {
        val timerSpec = "0 15 20 * * ? *"
        val config = gocd {
            pipelines {
                sequence {
                    group("group1") {
                        ContextStack.current!!.data["timer"] = timerSpec

                        pipeline("p1") {
                            materials {
                                repoPackage("repo")
                            }
                            template = Template("t", "s")
                            timer(ContextStack.current!!.data.getValue("timer"))
                        }
                    }
                }
            }
        }

        it("Config with multiple environments and unassociated pipelines") {
            assertThat(config.pipelines.pipelines().first().timer!!.spec).isEqualTo(timerSpec)
        }
    }
})