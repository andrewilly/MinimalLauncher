package com.minillauncher.utils

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.core.content.pm.ShortcutInfoCompat
import java.util.Locale

/**
 * Data class representing an installed application.
 * Holds all the info needed to display and launch an app.
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

    /** First letter for alphabetical grouping (locale-aware) */
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
