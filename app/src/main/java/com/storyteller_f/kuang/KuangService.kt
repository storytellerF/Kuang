package com.storyteller_f.kuang

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dalvik.system.DexClassLoader
import io.ktor.server.engine.*
import io.ktor.server.netty.*

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
        val s = "foreground"
        val channel = NotificationChannelCompat.Builder(s, NotificationManagerCompat.IMPORTANCE_MIN).apply {
            setName("running")
            setSound(null, null)
        }.build()
        val from = NotificationManagerCompat.from(this)
        if (from.getNotificationChannel(s) == null)
            from.createNotificationChannel(channel)
        val notification = NotificationCompat.Builder(this, s).apply {
            setSound(null)
        }.build()
        startForeground(foreground_notification_id, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true)
        binder?.stop()
    }

    class Kuang : Binder() {
        private var server: ApplicationEngine? = null
        fun start() {
            val s = "/data/data/com.storyteller_f.kuang/files/sampledex.jar"
            try {
                val dexClassLoader = DexClassLoader(s, null, null, javaClass.classLoader)
                val className = dexClassLoader.getResourceAsStream("kcon")?.bufferedReader()?.readText()
                println(className)
                val serverClass = dexClassLoader.loadClass(className)
                val declaredField = serverClass.getField("application")
                val newInstance = serverClass.getConstructor().newInstance()
                val method = serverClass.getMethod("start")
                println(serverClass)
                this.server = embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
//                configureRouting()
                    declaredField.set(newInstance, this)
                    method.invoke(newInstance)
                }.start(wait = false)
            } catch (th: Throwable) {

            }

        }

        fun stop() {
            server?.stop()
        }

        fun restart() {
            stop()
            start()
        }
    }

    companion object {
        private const val TAG = "KuangService"
        private const val foreground_notification_id = 10
    }
}