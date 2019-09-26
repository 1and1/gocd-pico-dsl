package net.oneandone.gocd.picodsl.configs

import net.oneandone.gocd.picodsl.dsl.gocd

val environmentWithPipelines = gocd {
    environments {
        environment("dev") {
            envVar("envKey", "envValue")
        }
    }
    pipelines {
        sequence {
            startingPipelineWithMaterial()
            pipeline("p2") {
                template = template2
            }
        }
    }
}