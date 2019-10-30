package net.oneandone.gocd.picodsl.registry

import net.oneandone.gocd.picodsl.RegisteredGocdConfig
import net.oneandone.gocd.picodsl.configs.startingPipelineWithMaterial

object RegisterConfig1 : RegisteredGocdConfig({
    pipelines {
        sequence {
            startingPipelineWithMaterial()
        }
    }
})