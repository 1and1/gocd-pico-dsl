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

val gocdStartParallel = gocd {
    pipelines {
        parallel {
            group("dev") {
                pipeline("A") {
                    template = template1
                    materials {
                        repoPackage("material1")
                    }

                }
                sequence {
                    pipeline("B1") {
                        template = template2
                        materials {
                            repoPackage("material2")
                        }
                    }
                    pipeline("B2") {
                        template = template2
                    }
                }
            }
        }
    }
}