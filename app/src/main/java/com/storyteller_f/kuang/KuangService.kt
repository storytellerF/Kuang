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
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlin.collections.forEach
import kotlin.collections.mutableMapOf
import kotlin.collections.set

class KuangService : Service() {
    private var binder: Kuang? = null
    override fun onBind(intent: Intent): IBinder {
        Log.d(TAG, "onBind() called with: intent = $intent")
        return Kuang().also {
            binder = it
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand() called with: intent = $intent, flags = $flags, startId = $startId")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        Log.d(TAG, "onCreate() called")
        super.onCreate()
        val channelId = "foreground"
        val channel = NotificationChannelCompat.Builder(channelId, NotificationManagerCompat.IMPORTANCE_MIN).apply {
            setName("running")
            this.setDescription("前台服务")
        }.build()
        val managerCompat = NotificationManagerCompat.from(this)
        if (managerCompat.getNotificationChannel(channelId) == null)
            managerCompat.createNotificationChannel(channel)
        val notification =
            NotificationCompat.Builder(this, channelId).setSmallIcon(R.mipmap.ic_launcher).setContentTitle("kuang").setContentText("waiting").build()
        startForeground(foreground_notification_id, notification)
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy() called")
        super.onDestroy()
        stopForeground(STOP_FOREGROUND_REMOVE)
        binder?.stop()
    }

    class Kuang : Binder() {
        private val servers = mutableMapOf<String, ApplicationEngine>()
        fun start() {
            Log.d(TAG, "start() called")
            try {
                pluginManager.pluginsName().forEach {
                    Log.i(TAG, "loadPlugin: plugin name $it")
                    val server = createServer(it)
                    servers[it] = server
                }

            } catch (th: Throwable) {
                Log.e(TAG, "start: ${th.localizedMessage}", th)
            }

        }

        private fun createServer(it: String): NettyApplicationEngine {
            val server = embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
                val revolvePlugin = pluginManager.revolvePlugin(pluginManager.pluginPath(it))
                val serverClass = pluginManager.getClass(revolvePlugin.path)
                val declaredField = serverClass.getField("application")
                val newInstance = serverClass.getConstructor().newInstance()
                declaredField.set(newInstance, this)
                try {
                    serverClass.getMethod("start").apply {
                        invoke(newInstance)
                    }
                } catch (e: Exception) {
                    serverClass.getMethod("start", ClassLoader::class.java).apply {
                        invoke(newInstance, revolvePlugin.classLoader)
                    }
                }
            }.start(wait = false)
            return server
        }
        fun stop() {
            servers.values.forEach(ApplicationEngine::stop)
            servers.clear()
        }

        fun restart(context: Context) {
            stop()
            pluginManager.removeAllPlugin()
            context.refreshPluginList()
            start()
            Toast.makeText(context, "restarted", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val TAG = "KuangService"
        private const val foreground_notification_id = 10
    }
}
