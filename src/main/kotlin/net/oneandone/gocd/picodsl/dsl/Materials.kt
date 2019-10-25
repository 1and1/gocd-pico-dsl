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
package net.oneandone.gocd.picodsl.dsl

/**
 *  [gocd-yaml-config-plugin Materials](https://github.com/tomzo/gocd-yaml-config-plugin#materials)
 */

sealed class Material(val name: String)

class Package(name: String) : Material(name) {
    init {
        require(name.isNotBlank()) { "package must be named"}
    }
}

class Materials {
    val materials = mutableListOf<Material>()

    /** package is a keyword, method therefore renamed to repoPackage */
    fun repoPackage(name: String) {
        val repoPackage = Package(name)
        materials.add(repoPackage)
    }
}