package com.github.masooh.gocdpicodsl.dsl

/*
    Classes defining https://github.com/tomzo/gocd-yaml-config-plugin#pipeline
 */

/**
 * ```
 * mypipe1:
 *  lock_behavior: none
 * ```
 */
enum class LockBehavior {
    lockOnFailure,
    unlockWhenFinished,
    none
}

/**
 * ```
 * mypipe1:
 *  template: template1
 * ```
 */
data class Template(val name: String, val stage: String)