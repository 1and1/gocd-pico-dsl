package net.oneandone.gocd.picodsl.configs

import net.oneandone.gocd.picodsl.dsl.gocd

val gocdTwoPipelines = gocd {
    pipelines {
        sequence {
            pipeline("p1") {
                materials {
                    repoPackage("material1")
                }
                template = template1
            }
            pipeline("p2") {
                template = template2
            }
        }
    }
}