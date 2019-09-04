package net.oneandone.gocd.picodsl

import net.oneandone.gocd.picodsl.dsl.gocd

val gocdStageWithScript = gocd {
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