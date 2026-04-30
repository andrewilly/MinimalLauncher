package com.minillauncher.utils

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import java.util.Locale

/**
 * Data class representing an installed application.
 */
data class AppInfo(
    val label: CharSequence,
    val packageName: String,
    val className: String,
    val icon: Drawable,
    var isHidden: Boolean = false,
    val firstInstallTime: Long = 0L
) {
    val componentName: ComponentName
        get() = ComponentName(packageName, className)

    val sortLetter: Char
        get() = label.toString().uppercase(Locale.getDefault()).firstOrNull() ?: '#'

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AppInfo) return false
        return packageName == other.packageName && className == other.className
    }

    override fun hashCode(): Int {
        var result = packageName.hashCode()
        result = 31 * result + className.hashCode()
        return result
    }
}

/**
 * Capitalize only the first letter of each word.
 * e.g. "GOOGLE CHROME" → "Google Chrome", "telegram" → "Telegram"
 */
fun CharSequence.capitalizeFirstLetter(): String {
    if (this.isEmpty()) return this.toString()
    return this.toString().split(" ").joinToString(" ") { word ->
        if (word.isEmpty()) word
        else word.substring(0, 1).uppercase(Locale.getDefault()) +
             word.substring(1).lowercase(Locale.getDefault())
    }
}

/**
 * Convert dp to pixels.
 */
fun Context.dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
