package net.oneandone.gocd.picodsl.examples.registry

import net.oneandone.gocd.picodsl.RegisteredGocdConfig

object Second : RegisteredGocdConfig({
    environments() {
        environment("devEnv") {}
    }
    pipelines {
        sequence {
            deploy("first") {
                group = "dev"
                materials {
                    repoPackage("euss")
                }
            }
        }
    }
})
