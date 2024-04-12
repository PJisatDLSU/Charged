package com.example.charged

import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.os.BatteryManager
import android.os.IBinder

class PowerConnectionService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        // Any initialization code you need can go here
    }

    override fun onDestroy() {
        super.onDestroy()
        // Any cleanup code you need can go here
    }
}

