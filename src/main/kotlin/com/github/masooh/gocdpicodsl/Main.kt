package com.github.masooh.gocdpicodsl

fun main() {
    sequence {
        val prepare = pipeline("prepare") {
            template("")
            parameters(
                    "a" to "b",
                    "b" to "c")
        }
        pipeline("migration") {
            template("") // todo enum/const hinterlegne für templates
            /* todo jeder value muss potentiell eine closure sein, die beim rendern
                 ausgewertet wird
             */
            /*
                todo ? wie kann ich default Params anreichern -> template pipeline, merge mode
             */
            parameters(
                    "a" to "b",
                    "b" to "c",
                    "upstream" to prepare.name)
//            "upstream_name" to { upstreams().filter{ "/.*prepare$/".toRegex() }.first().allNodes.join("/") })
        }
        parallel {
            pipeline("crms") {
                template("")
            }
            sequence {
                pipeline("keyservice") {
                    template("")
                }
                parallel {
                    pipeline("ni") { }
                    pipeline("trinity") { }
                }
            }
        }
        pipeline("promote") {
            template("")
        }
    }

    graph.edgeSet().forEach { edge ->
        println("$edge")
    }

    println(graph.toYaml())
}
