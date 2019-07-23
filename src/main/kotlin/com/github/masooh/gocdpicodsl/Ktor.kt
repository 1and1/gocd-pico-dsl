package com.github.masooh.gocdpicodsl

import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main() {
//    System.out.appendHTML().html {
//        body {
//            div {
//                a("https://kotlinlang.org") {
//                    target = ATarget.blank
//                    +"Main site"
//                }
//            }
//        }
//    }

    embeddedServer(Netty, port = 8080) {
        routing {
            get("/") {
                call.respondText("Hello World!", ContentType.Text.Plain)
            }
        }
    }.start(true)
}
