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
abstract class RegisteredGocdConfig(name: String? = null, init: GocdConfig.() -> Unit) {
    init {
        GocdConfig(name ?: this.javaClass.simpleName.toKepabCase()).apply(init).finish().register()
    }
}

fun String.toKepabCase() = this.replace("(?<=.)([A-Z])".toRegex(), "-$1").toLowerCase()