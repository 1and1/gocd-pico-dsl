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
package net.oneandone.gocd.picodsl.dsl

import org.assertj.core.api.Assertions.assertThat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object PipelineSingleTest : Spek({

    describe("toString() has optional tags and stub information") {
        describe("pipeline without tags") {
            val pipeline = PipelineSingle("p1")

            it("toString() contains name") {
                assertThat(pipeline.toString()).isEqualTo("pipeline(name=p1)")
            }
        }

        describe("pipeline with tags") {
            val pipeline = PipelineSingle("p1")
            pipeline.tag("key", "value")

            it("toString() contains name and tags") {
                assertThat(pipeline.toString()).isEqualTo("pipeline(name=p1, tags={key=value})")
            }
        }

        describe("stub pipeline") {
            val pipeline = PipelineSingle("p1")
            pipeline.stub = true

            it("toString() stub information") {
                assertThat(pipeline.toString()).isEqualTo("pipeline(name=p1, stub=true)")
            }
        }


    }
})