package com.minillauncher.utils

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Utility object for time, date, and battery formatting.
 * Configurable from preferences (12h/24h, date format).
 */
object ClockUtils {

    private const val PREF_USE_24H = "pref_use_24h"
    private const val PREF_SHOW_DATE = "pref_show_date"
    private const val PREF_DATE_FORMAT = "pref_date_format"

    /**
     * Get the formatted time string.
     */
    fun getTime(context: Context): String {
        val prefs = getPrefs(context)
        val use24h = prefs.getBoolean(PREF_USE_24H, true)
        val pattern = if (use24h) "HH:mm" else "hh:mm"
        return SimpleDateFormat(pattern, Locale.getDefault()).format(Date())
    }

    /**
     * Get the formatted date string.
     */
    fun getDate(context: Context): String {
        val prefs = getPrefs(context)
        val showDate = prefs.getBoolean(PREF_SHOW_DATE, true)
        if (!showDate) return ""

        val formatKey = prefs.getString(PREF_DATE_FORMAT, "EEEE, d MMMM")
        val pattern = when (formatKey) {
            "EEEE, d MMMM" -> "EEEE, d MMMM"
            "dd/MM/yyyy" -> "dd/MM/yyyy"
            "MM/dd/yyyy" -> "MM/dd/yyyy"
            "yyyy-MM-dd" -> "yyyy-MM-dd"
            "d MMMM yyyy" -> "d MMMM yyyy"
            else -> "EEEE, d MMMM"
        }
        return SimpleDateFormat(pattern, Locale.getDefault()).format(Date())
    }

    /**
     * Get battery percentage as a string (e.g., "85%").
     * Uses a sticky broadcast - no receiver needed.
     */
    fun getBatteryPercentage(context: Context): String {
        return try {
            val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            if (batteryIntent != null) {
                val level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                if (level >= 0 && scale > 0) {
                    val pct = Math.round(level * 100.0 / scale)
                    "$pct%"
                } else {
                    ""
                }
            } else {
                ""
            }
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Check if device is currently charging.
     */
    fun isCharging(context: Context): Boolean {
        return try {
            val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) == BatteryManager.BATTERY_STATUS_CHARGING
        } catch (e: Exception) {
            false
        }
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences("minillauncher_prefs", Context.MODE_PRIVATE)
    }
}
