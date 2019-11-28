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

import org.assertj.core.api.Assertions.assertThat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.io.File

object GeneratePipelinesTest : Spek({

    beforeEachTest {
        ConfigRegistry.clear()
    }

    describe("calling main in GeneratePipelines with custom folder") {
        val outputFolder = File("target/gocd-config-${System.currentTimeMillis()}")

        main(arrayOf("-s net.oneandone.gocd.picodsl.registry", "-o ${outputFolder.path}"))

        it("generates all objects with given base package") {
            assertThat(outputFolder.list()?.size).isEqualTo(2)
        }
    }

    describe("calling main in GeneratePipelines with default folder") {
        val outputFolder = File("target/gocd-config")

        main(arrayOf("-s net.oneandone.gocd.picodsl.registry"))

        it("generates all objects with given base package") {
            assertThat(outputFolder.list()?.size).isEqualTo(2)
        }
    }

    describe("graph export with additional parameters") {
        val outputFolder = File("target/gocd-config-${System.currentTimeMillis()}")

        main(arrayOf(
                "-s net.oneandone.gocd.picodsl.registry",
                "-o ${outputFolder.path}",
                "--plantuml",
                "--dot"
        ))

        it("has plantuml files") {
            val pumlFiles = outputFolder.listFiles { _: File, name: String ->
                name.endsWith(".puml")
            }

            assertThat(pumlFiles.size).isEqualTo(2)
        }

        it("has dot files") {
            val dotFiles = outputFolder.listFiles { _: File, name: String ->
                name.endsWith(".dot")
            }

            assertThat(dotFiles.size).isEqualTo(2)
        }

    }

})