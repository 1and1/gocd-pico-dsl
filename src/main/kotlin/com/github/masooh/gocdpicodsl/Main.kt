package com.github.masooh.gocdpicodsl

fun main() {
    pipelines {
        pipeline("prepare") {
            template("")
            parameters(
                    "a" to "b",
                    "b" to "c")
        }
        // add pipeline to parent list
        pipeline("migration") {
            template("")
            parameters(
                    "a" to "b",
                    "b" to "c")
        }
        // fork from migration
        // add group of pipeline parent list -> pipeline:group of pipeline
        parallel {
            // add to parent list
            pipeline("crms") {
                template("")
            }
            // add to parent
            // todo fork -> first of sequence
            sequence {
                pipeline("keyservice") {
                    template("")
                }

                // fork from keyservice
                parallel {
                    pipeline("ni") { }
                    pipeline("trinity") { }
                }
            }
        }
        // todo missing open endings: ni, trinity -> promote
        // add to all open endings
        pipeline("promote") {
            template("")
        }
    }

    println(graph)
}
