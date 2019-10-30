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
    val outputFolder = File("target/gocd-config-${System.currentTimeMillis()}")

    beforeGroup {
        ConfigRegistry.clear()
    }

    describe("calling main in GeneratePipelines") {
        main(arrayOf("net.oneandone.gocd.picodsl.registry", outputFolder.path))

        it("generates all objects with given base package") {
            assertThat(outputFolder.list()?.size).isEqualTo(2)
        }
    }
})