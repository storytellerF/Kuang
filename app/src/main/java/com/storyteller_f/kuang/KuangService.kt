package com.storyteller_f.kuang

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dalvik.system.DexClassLoader
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.html.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.html.body
import kotlinx.html.h1
import kotlinx.html.head
import kotlinx.html.title
import java.io.File
import java.time.Duration
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.LinkedHashSet

class KuangService : Service() {
    private var binder: Kuang? = null
    override fun onBind(intent: Intent): IBinder {
        Log.d(TAG, "onBind() called with: intent = $intent")
        return Kuang().also {
            binder = it
        }
    }

    override fun onCreate() {
        super.onCreate()
        val channelId = "foreground"
        val channel = NotificationChannelCompat.Builder(channelId, NotificationManagerCompat.IMPORTANCE_MIN).apply {
            setName("running")
        }.build()
        val managerCompat = NotificationManagerCompat.from(this)
        if (managerCompat.getNotificationChannel(channelId) == null)
            managerCompat.createNotificationChannel(channel)
        val notification = NotificationCompat.Builder(this, channelId).build()
        startForeground(foreground_notification_id, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(STOP_FOREGROUND_REMOVE)
        binder?.stop()
    }

    class Kuang : Binder() {
        private var server: ApplicationEngine? = null
        fun start(context: Context) {
            try {

                this.server = embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
                    plugPlugins()
                    configureRouting()
                    val listFiles = context.filesDir.listFiles { _, name ->
                        name.endsWith(".jar")
                    }.orEmpty()
                    loadPlugin(listFiles)

                }.start(wait = false)
            } catch (th: Throwable) {
                Log.e(TAG, "start: ${th.localizedMessage}", th)
            }

        }

        private fun Application.plugPlugins() {
            install(StatusPages)
            install(CallLogging)
            install(WebSockets) {
                pingPeriod = Duration.ofSeconds(15)
                timeout = Duration.ofSeconds(15)
                maxFrameSize = Long.MAX_VALUE
                masking = false
            }
        }

        private fun Application.loadPlugin(listFiles: Array<out File>) {
            val classLoader = javaClass.classLoader
            println("当前classLoader $classLoader ${listFiles.size}")
            listFiles.forEach {
                val dexClassLoader = DexClassLoader(it.absolutePath, null, null, classLoader)
                val className = dexClassLoader.getResourceAsStream("kcon")?.bufferedReader()?.readText() ?: return@forEach
                println("${it.name} $className")
                val serverClass = dexClassLoader.loadClass(className)
                val declaredField = serverClass.getField("application")
                val newInstance = serverClass.getConstructor().newInstance()
                println("外部jar classLoader ${serverClass.classLoader}")
                declaredField.set(newInstance, this)
                try {
                    serverClass.getMethod("start").apply {
                        invoke(newInstance)
                    }
                } catch (e: Exception) {
                    serverClass.getMethod("start", ClassLoader::class.java).apply {
                        invoke(newInstance, dexClassLoader)
                    }
                }
            }
        }

        fun stop() {
            server?.stop()
        }

        fun restart(context: Context) {
            stop()
            start(context)
            Toast.makeText(context, "restarted", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val TAG = "KuangService"
        private const val foreground_notification_id = 10
    }
}

class Connection(val session: DefaultWebSocketSession) {
    companion object {
        val lastId = AtomicInteger(0)
    }
    val name = "user${lastId.getAndIncrement()}"
}

fun Application.configureRouting() {
    routing {
        val connections = Collections.synchronizedSet<Connection?>(LinkedHashSet())
        webSocket("/chat") {
            println("Adding user!")
            val thisConnection = Connection(this)
            connections += thisConnection
            try {
                send("You are connected! There are ${connections.count()} users here.")
                for (frame in incoming) {
                    frame as? Frame.Text ?: continue
                    val receivedText = frame.readText()
                    val textWithUsername = "[${thisConnection.name}]: $receivedText"
                    connections.forEach {
                        it.session.send(textWithUsername)
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