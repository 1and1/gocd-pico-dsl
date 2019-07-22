package com.github.masooh.gocdpicodsl

open class PipelineGroup
class PipelineSequence : PipelineGroup()
class PipelineParallel : PipelineGroup()
class PipelineSingle : PipelineGroup()

fun main() {
    sequence {
        // add pipeline to parent list
        pipeline("migration") {
            template("")
            parameters(
                    "a" to "b",
                    "b" to "c")
        }
        // add group of pipeline parent list -> pipeline:group of pipeline
        parallel {
            // add to parent list
            pipeline("crms") {
                template("")
            }
            // add to parent
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
}