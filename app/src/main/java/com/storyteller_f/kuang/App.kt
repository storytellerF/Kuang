package com.storyteller_f.kuang

import android.app.Application
import android.content.Context
import java.io.File

val pluginManager = PluginManager()

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        refreshPluginList()
    }


}

fun Context.refreshPluginList() {
    val listFiles = File(filesDir, "plugins").listFiles { _, name ->
        name.endsWith(".jar")
    }.orEmpty()
    listFiles.forEach {
        pluginManager.findPlugin(File(it.absolutePath))
    }
}