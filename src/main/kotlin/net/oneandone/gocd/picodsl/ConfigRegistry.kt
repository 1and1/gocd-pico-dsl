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