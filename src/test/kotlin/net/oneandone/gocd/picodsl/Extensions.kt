package net.oneandone.gocd.picodsl

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory

/** Yaml String to JSON */
fun String.toJson(): String {
    val objectMapper = ObjectMapper(YAMLFactory())
    val yamlObject = objectMapper.readValue(this, Any::class.java)
    return ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(yamlObject)
}