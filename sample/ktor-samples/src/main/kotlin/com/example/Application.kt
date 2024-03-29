package com.example

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.example.plugins.*
import io.ktor.serialization.kotlinx.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.thymeleaf.*
import io.ktor.server.websocket.*
import kotlinx.serialization.json.Json
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver
import java.time.Duration

@Suppress("unused")
class TestServer {
    lateinit var application: Application
    fun start(classLoader: ClassLoader) {
        application.run {
            setup(classLoader)
        }
    }


}

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
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
        contentConverter = KotlinxWebsocketSerializationConverter(Json)
    }
    install(Thymeleaf) {
        setTemplateResolver(ClassLoaderTemplateResolver(classLoader).apply {
            prefix = "templates/"
            suffix = ".html"
            characterEncoding = "utf-8"
        })
    }
    configureRouting()
    webSocketsService()
}
