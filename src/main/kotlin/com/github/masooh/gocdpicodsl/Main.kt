package com.github.masooh.gocdpicodsl

fun main() {
    sequence {
        pipeline("prepare") {
            template("")
            parameters(
                    "a" to "b",
                    "b" to "c")
        }
        pipeline("migration") {
            template("")
            parameters(
                    "a" to "b",
                    "b" to "c",
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
}
