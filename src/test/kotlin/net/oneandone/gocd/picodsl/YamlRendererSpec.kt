package net.oneandone.gocd.picodsl

import net.javacrumbs.jsonunit.JsonAssert
import net.oneandone.gocd.picodsl.dsl.GocdConfig
import net.oneandone.gocd.picodsl.dsl.Template
import net.oneandone.gocd.picodsl.dsl.gocd
import net.oneandone.gocd.picodsl.renderer.toYaml
import org.assertj.core.api.Assertions
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.io.File

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

    describe("Test DSLs against expected Yamls") {
        mapOf(
                gocdWithTwoPipelines to "two-pipelines.yaml",
                gocdStageWithScript to "stage-with-script.yaml"
        ).forEach { (gocdConfig, expectedYamlFilename) ->
            describe("generating yaml for $expectedYamlFilename") {
                val generatedYaml = gocdConfig.graph.toYaml()

                val generatedFiles = File("target/test-generated-yamls")
                generatedFiles.mkdirs()
                File(generatedFiles, expectedYamlFilename).writeText(generatedYaml)

                val expectedYaml = YamlRendererSpec::class.java.getResource(expectedYamlFilename).readText()

                it("matches $expectedYamlFilename") {
                    JsonAssert.assertJsonEquals(expectedYaml.toJson(), generatedYaml.toJson())
                }
            }
        }
    }

})