package com.example

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.example.plugins.*
//~/Library/Android/sdk/build-tools/29.0.1/dx --dex --output=sample.jar build/libs/com.example.ktor-samples-0.0.1.jar
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
