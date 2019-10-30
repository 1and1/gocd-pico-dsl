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

import net.oneandone.gocd.picodsl.configs.startingPipelineWithMaterial
import net.oneandone.gocd.picodsl.dsl.gocd
import org.assertj.core.api.Assertions.assertThat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object Config1 : RegisteredGocdConfig({
    pipelines {
        sequence {
            startingPipelineWithMaterial()
        }
    }
})

object Config2 : RegisteredGocdConfig({
    pipelines {
        sequence {
            startingPipelineWithMaterial()
        }
    }
})

object ConfigRegistryTest : Spek({
    beforeGroup {
        ConfigRegistry.clear()
    }

    describe("having defined two RegisteredGocdConfig objects") {
        val gocd = gocd { }

        it("is empty before classes are instantiated") {
            assertThat(ConfigRegistry.size).isZero()
        }

        it("collects all RegisteredGocdConfig classes") {
            Config1
            Config2

            assertThat(ConfigRegistry.size).isEqualTo(2)
        }

        it("can be registered via extension function") {

            gocd.register()

            assertThat(ConfigRegistry.size).isEqualTo(3)
            assertThat(ConfigRegistry.contains(gocd))
        }

        it("can be removed from registry") {
            ConfigRegistry.remove(gocd)
            assertThat(ConfigRegistry.size).isEqualTo(2)
        }
    }
})