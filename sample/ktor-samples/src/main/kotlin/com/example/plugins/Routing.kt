package com.example.plugins

import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.html.*

fun Application.configureRouting() {

    routing {
        route("/samples") {
            get("/") {
                call.respondText("Hello World!")
            }
            get("/html") {
                val name = "samples"
                call.respondHtml {
                    head {
                        title {
                            +name
                        }
                    }
                    body {
                        h1 {
                            +"Hello from $name!"
                        }
                        p {
                            +"paragraph"
                        }
                    }
                }
            }
        }

    }
}
