package net.oneandone.gocd.picodsl.examples.registry

import net.oneandone.gocd.picodsl.RegisteredGocdConfig

object SecondUsingExtension : RegisteredGocdConfig({
    environments {
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
