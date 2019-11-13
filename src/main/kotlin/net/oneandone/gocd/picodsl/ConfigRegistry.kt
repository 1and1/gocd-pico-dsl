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
package net.oneandone.gocd.picodsl

import net.oneandone.gocd.picodsl.dsl.GocdConfig

object ConfigRegistry : HashSet<GocdConfig>()

fun GocdConfig.register(): GocdConfig {
    ConfigRegistry.add(this)
    return this
}

/**
 * All derived objects are registered
 *
 * ```
 * object MyPipeline : RegisteredGocdConfig("my-pipeline", {
 *    pipelines {
 *      // ...
 *    }
 * })
 * ```
 * @see net.oneandone.gocd.picodsl.GeneratePipelinesKt
 */
abstract class RegisteredGocdConfig(init: GocdConfig.() -> Unit, name: String? = null) {
    init {
        GocdConfig(name ?: this.javaClass.simpleName.toHyphenCase()).apply(init).finish().register()
    }
}

fun String.toHyphenCase() = this.replace("(?<=.)([A-Z])".toRegex(), "-$1").toLowerCase()