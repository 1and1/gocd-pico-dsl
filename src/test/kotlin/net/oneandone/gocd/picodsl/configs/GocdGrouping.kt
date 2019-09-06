package net.oneandone.gocd.picodsl.configs

import net.oneandone.gocd.picodsl.dsl.gocd

val gocdGrouping = gocd {
    pipelines {
        sequence {
            group("dev") {
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

            group("qa") {
                pipeline("qa-p1") {
                    template = template1
                }

                pipeline("qa-p2") {
                    template = template2
                }
            }
        }
    }
}