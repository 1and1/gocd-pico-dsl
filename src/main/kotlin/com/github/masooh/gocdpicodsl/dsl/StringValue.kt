package com.github.masooh.gocdpicodsl.dsl

/**
 * Provides possibility for late evaluation by using [LambdaStringValue]
 */
interface StringValue {
    fun getValue() : String
}

class SimpleStringValue(val simpleString: String) : StringValue {
    override fun getValue(): String {
        return simpleString
    }
}

class LambdaStringValue(val lambda: () -> String) : StringValue {
    override fun getValue(): String {
        return lambda()
    }
}