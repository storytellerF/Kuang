package com.example.plugins

import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import io.ktor.websocket.serialization.*
import kotlinx.html.*
import kotlinx.serialization.Serializable
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.LinkedHashSet

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


class Connection(val session: DefaultWebSocketServerSession) {
    companion object {
        val lastId = AtomicInteger(0)
    }

    val name = "user${lastId.getAndIncrement()}"
}
@Serializable
class Message(val from: String, val data: String)

fun Application.webSocketsService() {
    routing {
        val connections = Collections.synchronizedSet<Connection?>(LinkedHashSet())
        webSocket("/chat") {
            println("Adding user!")
            val thisConnection = Connection(this)
            connections += thisConnection
            try {
                sendSerialized(Message("system", "You are connected! There are ${connections.count()} users here."))
                for (frame in incoming) {
                    frame as? Frame.Text ?: continue
                    val receivedText = frame.readText()

                    connections.forEach {
                        it.session.sendSerialized(Message(thisConnection.name, receivedText))
                    }
                }
            } catch (e: Exception) {
                println(e.localizedMessage)
            } finally {
                println("Removing $thisConnection!")
                connections -= thisConnection
            }
        }
    }
}
