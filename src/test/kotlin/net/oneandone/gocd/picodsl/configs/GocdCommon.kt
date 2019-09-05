package net.oneandone.gocd.picodsl.configs

import net.oneandone.gocd.picodsl.dsl.PipelineSequence
import net.oneandone.gocd.picodsl.dsl.Template

val template1 = Template("template1", "stage1")
val template2 = Template("template2", "stage2")

/** Every config must start with a pipeline which has an material */
fun PipelineSequence.startingPipelineWithMaterial() {
    pipeline("p1") {
        materials {
            repoPackage("material1")
        }
        template = template1
    }
}