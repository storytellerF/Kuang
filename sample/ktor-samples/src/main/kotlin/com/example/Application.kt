package com.example

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.example.plugins.*

class TestServer {
    lateinit var application: Application
    fun start(classLoader: ClassLoader) {
        application.run {
            configureRouting()
            configureTemplating(classLoader)
        }
    }
}
fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    TestServer().apply {
        application = this@module
        start(Thread.currentThread().contextClassLoader)
    }
}
