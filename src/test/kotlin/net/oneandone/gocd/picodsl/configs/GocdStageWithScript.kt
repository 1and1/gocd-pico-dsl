package net.oneandone.gocd.picodsl.configs

import net.oneandone.gocd.picodsl.dsl.gocd

val gocdStageWithScript = gocd {
    pipelines {
        sequence {
            pipeline("p1") {
                materials {
                    repoPackage("material1")
                }
                stage("QA", true) {
                    job("jobOne") {
                        script("echo one")
                    }
                    job("jobTwo") {
                        script("echo two")
                    }
                }
            }
        }
    }
}