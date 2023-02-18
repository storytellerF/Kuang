package com.storyteller_f.kuang

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.storyteller_f.kuang.ui.theme.KuangTheme
import java.io.File

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            var plugins by remember {
                mutableStateOf(pluginManager.pluginsName())
            }
            KuangTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Simple TopAppBar") },
                            navigationIcon = {
                                IconButton(onClick = { /* doSomething() */ }) {
                                    Icon(Icons.Filled.Menu, contentDescription = null)
                                }
                            },
                            actions = {
                                // RowScope here, so these icons will be placed horizontally
                                IconButton(onClick = {
                                    fileOperateBinder?.restart(context)
                                    plugins = pluginManager.pluginsName()
                                }) {
                                    Icon(Icons.Filled.Refresh, contentDescription = "Localized description")
                                }
                                IconButton(onClick = {
                                    fileOperateBinder?.stop()
                                }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Localized description")
                                }
                            }
                        )
                    }
                ) { padding ->
                    // Screen content
                    // A surface container using the 'background' color from the theme
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding), color = MaterialTheme.colorScheme.background
                    ) {
                        Main(plugins)
                    }
                }
            }
        }
        //连接服务
        val intent = Intent(this, KuangService::class.java)
        startService(intent)
        bindService(intent, connection, 0)
    }

    var fileOperateBinder: KuangService.Kuang? = null
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Toast.makeText(this@MainActivity, "服务已连接", Toast.LENGTH_SHORT).show()
            val fileOperateBinderLocal = service as KuangService.Kuang
            Log.i(TAG, "onServiceConnected: $fileOperateBinderLocal")
            fileOperateBinder = fileOperateBinderLocal
            fileOperateBinderLocal.start()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Toast.makeText(this@MainActivity, "服务已关闭", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }

}

@Composable
fun Main(plugins: Set<String>) {
    LazyColumn(content = {
        plugins.forEach {
            item {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(text = it)
                }
            }
        }
    })
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    KuangTheme {
        Main(setOf("plugin-name"))
    }
}