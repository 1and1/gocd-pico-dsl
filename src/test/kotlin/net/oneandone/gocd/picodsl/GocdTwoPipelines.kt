package net.oneandone.gocd.picodsl

import net.oneandone.gocd.picodsl.dsl.Template
import net.oneandone.gocd.picodsl.dsl.gocd

val template1 = Template("template1", "stage1")
val gocdWithTwoPipelines = gocd {
    sequence {
        pipeline("p1") {
            materials {
                repoPackage("material1")
            }
            template = template1
        }
        pipeline("p2") {
            template = template1
        }
    }
}