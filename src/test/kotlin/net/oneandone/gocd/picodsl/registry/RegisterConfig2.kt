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
package net.oneandone.gocd.picodsl.registry

import net.oneandone.gocd.picodsl.RegisteredGocdConfig
import net.oneandone.gocd.picodsl.configs.template1

object RegisterConfig2 : RegisteredGocdConfig({
    pipelines {
        sequence {
            pipeline("config2-p1") {
                environment = env1
                group = "dev"
                materials {
                    repoPackage("material1")
                }
                template = template1
            }
        }
    }
}, "config2")