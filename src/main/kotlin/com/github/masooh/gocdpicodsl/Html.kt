package com.github.masooh.gocdpicodsl

import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.html.*
import kotlinx.html.stream.appendHTML
import kotlinx.html.stream.createHTML

fun main() {
    createHTML().html {
        body {  }
    }

    System.out.appendHTML().html {
        body {
            div {
                a("https://kotlinlang.org") {
                    target = ATarget.blank
                    +"Main site"
                }
            }
        }
    }
}
