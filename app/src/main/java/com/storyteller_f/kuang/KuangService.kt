package com.storyteller_f.kuang

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log

class KuangService : Service() {

    override fun onBind(intent: Intent): IBinder {
        Log.d(TAG, "onBind() called with: intent = $intent")
        return Kuang()
    }

    class Kuang : Binder() {
        fun start() {

        }
        fun stop() {

        }
        fun restart() {
            stop()
            start()
        }
    }

    companion object {
        private const val TAG = "KuangService"
    }
}