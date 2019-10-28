package net.oneandone.gocd.picodsl.dsl

import org.assertj.core.api.Assertions
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object ConfigSuiteTest : Spek({

    describe("material behaves like list") {
        describe("empty list") {
            val materials = Materials()

            it("size is zero") {
                Assertions.assertThat(materials.size).isZero()
            }
        }

        describe("list with one element") {
            val materials = Materials()

            val p1 = Package("p1")
            val p2 = Package("p2")

            beforeEach {
                materials.repoPackage("p1")
            }
            it("has size 1") {
                Assertions.assertThat(materials.size).isOne()
            }

            it("contains single element with same package") {
                Assertions.assertThat(materials.contains(p1)).isTrue()
            }

            it("does not contains element with same package") {
                Assertions.assertThat(materials.contains(p2)).isFalse()
            }
        }

    }
})