package net.oneandone.gocd.picodsl

import org.reflections.Reflections
import java.io.File

/**
 * Class can be used for generating all pipelines which are registered in [ConfigRegistry].
 *
 * @param args first: base package of GocdRegistries,
 *             second: outputFolder
 */
fun main(args: Array<String>) {
    writeYamlFiles(args[0], if (args.size > 1) args[1] else "target/gocd-config")
}

fun writeYamlFiles(basePackage: String, outputFolder: String) {
    val reflections = Reflections(basePackage)

    // instantiate classes in order to get them registered
    val configs = reflections.getSubTypesOf(RegisteredGocdConfig::class.java)
    configs.forEach {
        it.getDeclaredField("INSTANCE").get(null)
    }

    println("found the following registered config classes $configs")

    ConfigSuite(*ConfigRegistry.toTypedArray(), outputFolder = File(outputFolder)).writeYamlFiles()
}

