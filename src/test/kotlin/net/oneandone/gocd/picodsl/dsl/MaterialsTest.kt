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

object MaterialsTest : Spek({

    describe("material behaves like list") {
        describe("empty list") {
            val materials = Materials()

            it("size is zero") {
                assertThat(materials.size).isZero()
            }
        }

        describe("list with one element") {
            val materials = Materials()

            val p1 = materials.repoPackage("p1")
            val p1Again = Package("p1")
            val p2 = Package("p2")

            it("has size 1") {
                assertThat(materials.size).isOne()
            }

            it("contains the added material") {
                assertThat(materials.contains(p1)).isTrue()
            }

            it("contains material with same package") {
                assertThat(materials.contains(p1Again)).isTrue()
            }

            it("does not contain element with same package") {
                assertThat(materials.contains(p2)).isFalse()
            }
        }

    }
})