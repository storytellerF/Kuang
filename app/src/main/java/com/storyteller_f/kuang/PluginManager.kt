package com.storyteller_f.kuang

import androidx.annotation.WorkerThread
import dalvik.system.DexClassLoader
import java.io.File

class PluginConfiguration(val version: String, val path: String, val name: String, val classLoader: DexClassLoader, val className: String) {

}

class PluginManager {
    /**
     * key name
     * value configuration
     */
    private val map = mutableMapOf<String, PluginConfiguration>()
    private val raw = mutableMapOf<String, String>()

    @Synchronized
    fun findPlugin(file: File) {
        val name = file.name
        raw[name] = file.absolutePath
    }

    /**
     * 涉及io 读写应该在线程中执行
     */
    @WorkerThread
    @Synchronized
    fun revolvePlugin(path: String): PluginConfiguration {
        val name = File(path).name
        map[name]?.let {
            return it
        }
        val dexClassLoader = DexClassLoader(path, null, null, javaClass.classLoader)
        val className = dexClassLoader.getResourceAsStream("kcon")?.bufferedReader()?.readText()!!

        if (raw.contains(name)) raw.remove(name)
        val pluginConfiguration = PluginConfiguration("1.0", path, name, dexClassLoader, className)
        map[name] = pluginConfiguration
        return pluginConfiguration
    }

    fun pluginsName(): Set<String> {
        return map.keys + raw.keys
    }

    fun pluginPath(name: String): String {
        if (raw.contains(name)) {
            return raw[name]!!
        }
        return map[name]!!.path
    }

    @WorkerThread
    @Synchronized
    fun getClass(path: String): Class<*> {
        val name = File(path).name
        if (!map.contains(name)) {
            revolvePlugin(path)
        }
        return map[name]!!.let {
            it.classLoader.loadClass(it.className)
        }
    }

    @Synchronized
    fun removeAllPlugin() {
        map.clear()
        raw.clear()
    }
}