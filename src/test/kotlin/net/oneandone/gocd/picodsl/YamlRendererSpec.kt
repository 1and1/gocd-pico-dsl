package net.oneandone.gocd.picodsl

import net.javacrumbs.jsonunit.JsonAssert
import net.oneandone.gocd.picodsl.dsl.Template
import net.oneandone.gocd.picodsl.dsl.gocd
import net.oneandone.gocd.picodsl.renderer.toYaml
import org.assertj.core.api.Assertions
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature
import org.spekframework.spek2.style.specification.describe

object YamlRendererSpec: Spek({
    describe("empty DSL") {
        val gocd = gocd { }
        describe("generating yaml") {
            val yaml = gocd.graph.toYaml()

            it("empty YAML is generated") {
                Assertions.assertThat(yaml).matches("\\{.*}\\s*".toRegex(RegexOption.DOT_MATCHES_ALL).toPattern())
            }
        }
    }

    describe("dsl with with pipeline") {
        val template1 = Template("template1", "stage1")
        val gocd = gocd {
            sequence {
                pipeline("p1") {
                    materials {
                        repoPackage("material1")
                    }
                    template = template1
                }
                pipeline("p2") {
                    template = template1
                }
            }
        }

        describe("generating yaml") {
            val generatedYaml = gocd.graph.toYaml()

            println(generatedYaml)

            val expectedYaml = YamlRendererSpec::class.java.getResource("expected.yaml").readText()

            it("upstream pipeline is used as material") {
                JsonAssert.assertJsonEquals(expectedYaml.toJson(), generatedYaml.toJson())
            }
        }
    }
})