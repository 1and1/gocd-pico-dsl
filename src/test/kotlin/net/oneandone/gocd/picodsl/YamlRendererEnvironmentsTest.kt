package net.oneandone.gocd.picodsl

import net.javacrumbs.jsonunit.JsonAssert
import net.oneandone.gocd.picodsl.dsl.GocdEnvironments
import net.oneandone.gocd.picodsl.dsl.PipelineSingle
import net.oneandone.gocd.picodsl.renderer.dumpAsYaml
import net.oneandone.gocd.picodsl.renderer.yaml.YamlEnvironments
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.io.File

object YamlRendererEnvironmentsTest: Spek({
    describe("Test DSLs against expected Yamls") {
        describe("generating environment") {
            val environments = GocdEnvironments().apply {
                environment("testing") {
                    envVar("DEPLOYMENT", "testing")
                    addPipeline(PipelineSingle("one"))
                    addPipeline(PipelineSingle("two"))
                }
            }

            val generatedYaml = YamlEnvironments(environments).dumpAsYaml()
            val expectedYamlFilename = "environment.yaml"

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