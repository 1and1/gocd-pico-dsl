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

import net.oneandone.gocd.picodsl.dsl.QuartzTimer
import net.oneandone.gocd.picodsl.dsl.Template
import net.oneandone.gocd.picodsl.dsl.gocd

val timer3 = QuartzTimer("0 15 20 * * ? *", true)

val gocdStageWithScript = gocd {
    pipelines {
        sequence {
            pipeline("p1") {
                group = "dev"
                materials {
                    repoPackage("material1")
                }
                timer("0 15 10 * * ? *", true)
                stage("DEV", true) {
                    job("jobOne") {
                        script("echo one")
                    }
                    job("jobTwo") {
                        script("echo two")
                    }
                }
            }

            pipeline("p2") {
                group = "qa"
                template = Template("t1", "stage")
                timer("0 15 5 * * ? *")
            }

            pipeline("p3") {
                group = "qa"
                template = Template("t1", "stage")
                timer = timer3
            }
        }
    }
}