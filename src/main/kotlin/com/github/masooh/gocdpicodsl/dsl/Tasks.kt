package com.github.masooh.gocdpicodsl.dsl

/**
 *  [gocd-yaml-config-plugin Tasks](https://github.com/tomzo/gocd-yaml-config-plugin#tasks)
 */
interface Task
data class Script(val script: String) : Task

