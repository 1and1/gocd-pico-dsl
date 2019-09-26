package net.oneandone.gocd.picodsl

import net.javacrumbs.jsonunit.JsonAssert
import net.oneandone.gocd.picodsl.dsl.GocdConfig
import net.oneandone.gocd.picodsl.dsl.PipelineSingle
import net.oneandone.gocd.picodsl.renderer.toYaml
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.io.File

object YamlRendererEnvironmentsTest: Spek({
    describe("Test DSLs against expected Yamls") {
        describe("generating environment") {
            val gocdConfig = GocdConfig().apply {
                environments {
                    environment("testing") {
                        envVar("DEPLOYMENT", "testing")
                        addPipeline(PipelineSingle("one"))
                        addPipeline(PipelineSingle("two"))
                    }
                }
            }

            val generatedYaml = gocdConfig.toYaml()
            val expectedYamlFilename = "environment.gocd.yaml"

            val generatedFiles = File("target/test-generated-yamls")
            generatedFiles.mkdirs()
            File(generatedFiles, expectedYamlFilename).writeText(generatedYaml)

            println(expectedYamlFilename)
            val expectedYaml = YamlRendererEnvironmentsTest::class.java.getResource(expectedYamlFilename).readText()

            it("matches $expectedYamlFilename") {
                JsonAssert.assertJsonEquals(expectedYaml.toJson(), generatedYaml.toJson())
            }
        }
    }
})