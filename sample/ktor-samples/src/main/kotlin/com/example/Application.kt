package com.example

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.example.plugins.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.thymeleaf.*
import io.ktor.server.websocket.*
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver
import java.time.Duration

class TestServer {
    lateinit var application: Application
    fun start(classLoader: ClassLoader) {
        application.run {
            setup(classLoader)
        }
    }


}
fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module, watchPaths = listOf("classes"))
        .start(wait = true)
}

fun Application.module() {
    setup(Thread.currentThread().contextClassLoader)
}

private fun Application.setup(classLoader: ClassLoader) {
    install(CallLogging)
    install(StatusPages)
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
    install(Thymeleaf) {
        setTemplateResolver(ClassLoaderTemplateResolver().apply {
            prefix = "templates/"
            suffix = ".html"
            characterEncoding = "utf-8"
        })
    }
    configureRouting()
    configureTemplating(classLoader)
    webSocketsService()
}
