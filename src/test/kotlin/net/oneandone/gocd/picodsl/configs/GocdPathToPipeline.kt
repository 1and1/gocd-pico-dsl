/**
 * Copyright 1&1 Internet AG, https://github.com/1and1/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.oneandone.gocd.picodsl.configs

import net.oneandone.gocd.picodsl.dsl.gocd
import net.oneandone.gocd.picodsl.dsl.pathToPipeline

val gocdPathToPipeline = gocd {
    pipelines {
        sequence {
            group("dev") {
                pipeline("p1") {
                    tag("artifact", "p1-artifact")
                    materials {
                        repoPackage("material1")
                    }
                    template = template1
                }
                parallel {
                    pipeline("p2-a") {
                        template = template2
                    }
                    pipeline("p2-b") {
                        template = template2
                    }
                }
                pipeline("p3") {
                    template = template2
                }
                pipeline("p4") {
                    template = template2
                    graphProcessors.add {
                        parameter("upstream", it.pathToPipeline(this) { pipeline ->
                            pipeline.tags["artifact"] == "p1-artifact"
                        })
                    }
                }
            }
        }
    }
}