package com.example.charged

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.os.BatteryManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class PowerConnectionReceiver(private val context: Context) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val chargeIMG = (context as MainActivity).findViewById<ImageView>(R.id.chargeIND)
        if (intent.action == Intent.ACTION_POWER_CONNECTED) {
            chargeIMG.alpha = 1f
        } else if (intent.action == Intent.ACTION_POWER_DISCONNECTED){
            chargeIMG.alpha = 0f
        }
    }
}

class MainActivity : AppCompatActivity() {
    private lateinit var chargeTone: MediaPlayer
    private lateinit var chargeIMG: ImageView
    private lateinit var powPercent: TextView
    private lateinit var indicateCol: TextView
    private var isButtonPressed = false
    private val handler = Handler(Looper.getMainLooper())
    private val powerConnectionReceiver by lazy { PowerConnectionReceiver(this) }
    private val batteryUpdateHandler = Handler(Looper.getMainLooper())
    private val batteryUpdateRunnable = object : Runnable {
        override fun run() {
            val batteryStatus: Intent? = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val batteryPct = level.toFloat() / scale.toFloat() * 100

            if (batteryPct >= 90 && !chargeTone.isPlaying) {
                indicateCol.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.green))
                chargeTone.start()
                chargeTone.isLooping = true
            } else if (batteryPct < 90 && chargeTone.isPlaying) {
                chargeTone.pause()
                chargeTone.seekTo(0)
                chargeTone.isLooping = false
                indicateCol.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.white))
            }


            powPercent.text = "${batteryPct.toInt()}%"

            batteryUpdateHandler.postDelayed(this, 30000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        powPercent = findViewById(R.id.power)
        indicateCol = findViewById(R.id.indicate)
        chargeIMG = findViewById(R.id.chargeIND)
        val chargeButton = findViewById<Button>(R.id.btnSOUND)
        val chargingButton = findViewById<Button>(R.id.btnCHARGE)

        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_POWER_CONNECTED)
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED)
        registerReceiver(powerConnectionReceiver, filter)

        updateBatteryPercentage()

        chargeButton.setOnTouchListener { view, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                isButtonPressed = true
                handler.postDelayed({
                    if (isButtonPressed) {
                        if (chargeTone.isPlaying) {
                            chargeTone.pause()
                            chargeTone.seekTo(0)
                        }
                        chargeTone.start()
                        chargeTone.isLooping = true
                        indicateCol.setTextColor(ContextCompat.getColor(this, R.color.green))
                    }
                }, 0)
            } else if (motionEvent.action == MotionEvent.ACTION_UP || motionEvent.action == MotionEvent.ACTION_CANCEL) {
                isButtonPressed = false
                chargeTone.pause()
                chargeTone.seekTo(0)
                chargeTone.isLooping = false
                indicateCol.setTextColor(ContextCompat.getColor(this, R.color.black))
            }
            true
        }

        chargingButton.setOnTouchListener { view, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                chargeIMG.alpha = 1f
            } else if (motionEvent.action == MotionEvent.ACTION_UP || motionEvent.action == MotionEvent.ACTION_CANCEL){
                chargeIMG.alpha = 0f
            }
            true
        }
    }

    private fun updateBatteryPercentage() {
        val batteryStatus: Intent? = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct = level.toFloat() / scale.toFloat() * 100
        powPercent.text = "${batteryPct.toInt()}%"
    }

    override fun onResume() {
        super.onResume()
        chargeTone = MediaPlayer.create(this, R.raw.beep)
        batteryUpdateHandler.post(batteryUpdateRunnable)
    }

    override fun onPause() {
        super.onPause()
        chargeTone.release()
        batteryUpdateHandler.removeCallbacks(batteryUpdateRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        chargeTone.release()
        unregisterReceiver(powerConnectionReceiver)
    }
}
