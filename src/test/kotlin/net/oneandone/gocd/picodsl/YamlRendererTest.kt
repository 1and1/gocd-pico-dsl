package net.oneandone.gocd.picodsl

import net.javacrumbs.jsonunit.JsonAssert
import net.oneandone.gocd.picodsl.configs.gocdGrouping
import net.oneandone.gocd.picodsl.configs.gocdParallel
import net.oneandone.gocd.picodsl.configs.gocdStageWithScript
import net.oneandone.gocd.picodsl.configs.gocdTwoPipelines
import net.oneandone.gocd.picodsl.dsl.gocd
import net.oneandone.gocd.picodsl.renderer.toYaml
import org.assertj.core.api.Assertions
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.io.File

object YamlRendererTest: Spek({
    describe("empty DSL") {
        val gocd = gocd { }
        describe("generating yaml") {
            val yaml = gocd.pipelines.graph.toYaml()

            it("empty YAML is generated") {
                Assertions.assertThat(yaml).matches("\\{.*}\\s*".toRegex(RegexOption.DOT_MATCHES_ALL).toPattern())
            }
        }
    }

    describe("Test DSLs against expected Yamls") {
        mapOf(
                gocdTwoPipelines to "two-pipelines.yaml",
                gocdStageWithScript to "stage-with-script.yaml",
                gocdParallel to "parallel.yaml",
                gocdGrouping to "grouping.yaml"
        ).forEach { (gocdConfig, expectedYamlFilename) ->
            describe("generating yaml for $expectedYamlFilename") {
                val generatedYaml = gocdConfig.pipelines.graph.toYaml()

                val generatedFiles = File("target/test-generated-yamls")
                generatedFiles.mkdirs()
                File(generatedFiles, expectedYamlFilename).writeText(generatedYaml)

                println(expectedYamlFilename)
                val expectedYaml = YamlRendererTest::class.java.getResource(expectedYamlFilename).readText()

                it("matches $expectedYamlFilename") {
                    JsonAssert.assertJsonEquals(expectedYaml.toJson(), generatedYaml.toJson())
                }
            }
        }
    }

})