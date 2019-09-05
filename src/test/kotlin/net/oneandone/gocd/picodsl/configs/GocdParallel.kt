package net.oneandone.gocd.picodsl.configs

import net.oneandone.gocd.picodsl.dsl.gocd

val gocdParallel = gocd {
    sequence {
        startingPipelineWithMaterial()

        parallel {
            pipeline("para1") {
                template = template1
            }
            pipeline("para2") {
                template = template2
            }
        }
        pipeline("p2") {
            template = template1
        }
    }
}