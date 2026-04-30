package com.minillauncher.utils

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utility object for time, date, and battery formatting.
 */
object ClockUtils {

    /**
     * Get the formatted time string.
     */
    fun getTime(use24h: Boolean): String {
        val pattern = if (use24h) "HH:mm" else "hh:mm"
        return SimpleDateFormat(pattern, Locale.getDefault()).format(Date())
    }

    /**
     * Get the formatted date string.
     */
    fun getDate(dateFormat: String): String {
        return SimpleDateFormat(dateFormat, Locale.getDefault()).format(Date())
    }

    /**
     * Get battery info: percentage string + charging status.
     * Uses sticky broadcast — no receiver needed.
     */
    fun getBatteryInfo(context: Context): BatteryInfo {
        return try {
            val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            if (batteryIntent != null) {
                val level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                val status = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                val pct = if (level >= 0 && scale > 0) Math.round(level * 100.0 / scale).toInt() else -1
                val charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                               status == BatteryManager.BATTERY_STATUS_FULL
                BatteryInfo(percentage = pct, isCharging = charging)
            } else {
                BatteryInfo(percentage = -1, isCharging = false)
            }
        } catch (e: Exception) {
            BatteryInfo(percentage = -1, isCharging = false)
        }
    }
}

data class BatteryInfo(val percentage: Int, val isCharging: Boolean) {
    fun formatted(): String = if (percentage >= 0) "$percentage%" else ""
}
