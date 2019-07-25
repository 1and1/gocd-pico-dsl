package com.github.masooh.gocdpicodsl.dsl

/**
 *  [gocd-yaml-config-plugin Job](https://github.com/tomzo/gocd-yaml-config-plugin#job)
 */
data class Job(val name: String) {
    val tasks: MutableList<Task> = mutableListOf()

    fun script(script: String) {
        tasks.add(Script(script))
    }
}