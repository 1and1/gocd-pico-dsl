package com.github.masooh.gocdpicodsl.dsl

/**
 *  [gocd-yaml-config-plugin Materials](https://github.com/tomzo/gocd-yaml-config-plugin#materials)
 */

sealed class Material(val name: String)
class Package(name: String) : Material(name)

class Materials {
    var materials = mutableListOf<Material>()

    /** package is a keyword, method therefore renamed to repoPackage */
    fun repoPackage(name: String) {
        val repoPackage = Package(name)
        materials.add(repoPackage)
    }
}