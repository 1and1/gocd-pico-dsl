package com.github.masooh.gocdpicodsl

import com.github.masooh.gocdpicodsl.Template.*

enum class Template(val stage: String) {
    PREPARE_DEPLOYMENT("prepare"),
    DEPLOY_ONE_STAGE_SINGLE_JOB("PREPARE-DEPLOY-VERIFY-TEST"),
    PROMOTE("APPROVE") // FIXME ist kein Template -> manual stage
}

fun main() {
    sequence {
        val prepare = pipeline("prepare") {
            template = PREPARE_DEPLOYMENT
            parameter("a", "b")
        }
        pipeline("migration") {
            template = DEPLOY_ONE_STAGE_SINGLE_JOB
            /* todo jeder value muss potentiell eine closure sein, die beim rendern
                 ausgewertet wird
             */
            /*
                todo ? wie kann ich default Params anreichern -> template pipeline, merge mode
             */
//            "upstream_name" to { upstreams().filter{ "/.*prepare$/".toRegex() }.first().allNodes.join("/") })
        }
        parallel {
            pipeline("crms") {
                template = DEPLOY_ONE_STAGE_SINGLE_JOB
            }
            sequence {
                pipeline("keyservice") {
                    template = DEPLOY_ONE_STAGE_SINGLE_JOB
                }
                parallel {
                    pipeline("ni") {
                        template = DEPLOY_ONE_STAGE_SINGLE_JOB
                    }
                    pipeline("trinity") {
                        template = DEPLOY_ONE_STAGE_SINGLE_JOB
                    }
                }
            }
        }
        pipeline("promote") {
            template = PROMOTE
        }
    }

    graph.edgeSet().forEach { edge ->
        println("$edge")
    }

    println(graph.toYaml())
}
