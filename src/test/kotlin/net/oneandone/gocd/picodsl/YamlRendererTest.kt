package net.oneandone.gocd.picodsl

import net.javacrumbs.jsonunit.JsonAssert
import net.oneandone.gocd.picodsl.configs.*
import net.oneandone.gocd.picodsl.dsl.gocd
import net.oneandone.gocd.picodsl.renderer.toYaml
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.io.File

object YamlRendererTest: Spek({
    describe("Test DSLs against expected Yamls") {
        mapOf(
                gocd {  } to "no-pipeline.yaml",
                gocdTwoPipelines to "two-pipelines.yaml",
                environmentWithPipelines to "environment-with-pipelines.yaml",
                gocdStageWithScript to "stage-with-script.yaml",
                gocdParallel to "parallel.yaml",
                gocdGrouping to "grouping.yaml"
        ).forEach { (gocdConfig, expectedYamlFilename) ->
            describe("generating yaml for $expectedYamlFilename") {
                val generatedYaml = gocdConfig.toYaml()

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