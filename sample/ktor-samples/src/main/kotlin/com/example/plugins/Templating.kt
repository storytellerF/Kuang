package com.example.plugins

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.thymeleaf.*

fun Application.configureTemplating(classLoader: ClassLoader) {
    routing {
        get("/templates") {
            call.respond(ThymeleafContent("index", mapOf()))
        }
    }
}
