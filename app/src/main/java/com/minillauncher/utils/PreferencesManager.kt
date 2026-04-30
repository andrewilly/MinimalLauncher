package com.minillauncher.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Manages launcher preferences: hidden apps, home apps, theme, gestures, layout settings.
 */
class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("minillauncher_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_HIDDEN_APPS = "hidden_apps"
        private const val KEY_PINNED_HOME_APPS = "pinned_home_apps"
        private const val KEY_HOME_APP_COUNT = "home_app_count"
        private const val KEY_SWIPE_LEFT_ACTION = "swipe_left_action"
        private const val KEY_SWIPE_RIGHT_ACTION = "swipe_right_action"
        private const val KEY_SWIPE_UP_ACTION = "swipe_up_action"
        private const val KEY_SWIPE_DOWN_ACTION = "swipe_down_action"
        private const val KEY_DOUBLE_TAP_ACTION = "double_tap_action"
        private const val KEY_SHOW_CLOCK = "show_clock"
        private const val KEY_SHOW_DATE = "pref_show_date"
        private const val KEY_SHOW_BATTERY = "pref_show_battery"
        private const val KEY_USE_24H = "pref_use_24h"
        private const val KEY_DATE_FORMAT = "pref_date_format"
        private const val KEY_STATUS_BAR = "show_status_bar"
        private const val KEY_AUTO_SHOW_KEYBOARD = "auto_show_keyboard"
        private const val KEY_ICON_SIZE = "icon_size"
        private const val KEY_SHOW_ICON_LABELS = "show_icon_labels"
        private const val KEY_SHOW_ALPHABET_HEADERS = "show_alphabet_headers"
        private const val KEY_THEME = "theme"
        private const val KEY_SHOW_WIDGET_AREA = "show_widget_area"

        // Gesture actions
        const val ACTION_NONE = "none"
        const val ACTION_LOCK_SCREEN = "lock_screen"
        const val ACTION_OPEN_APP_DRAWER = "open_app_drawer"
        const val ACTION_SHOW_NOTIFICATIONS = "show_notifications"
        const val ACTION_OPEN_SEARCH = "open_search"

        // Theme modes
        const val THEME_SYSTEM = "system"
        const val THEME_LIGHT = "light"
        const val THEME_DARK = "dark"
        const val THEME_WALLPAPER = "wallpaper"

        // Home app count options
        const val HOME_APP_COUNT_ALL = 0 // Show all non-hidden apps
    }

    // ========== Hidden Apps ==========

    fun getHiddenApps(): Set<String> {
        val json = prefs.getString(KEY_HIDDEN_APPS, null) ?: return emptySet()
        return try {
            Gson().fromJson(json, object : TypeToken<Set<String>>() {}.type)
        } catch (e: Exception) {
            emptySet()
        }
    }

    fun setHiddenApps(packageNames: Set<String>) {
        prefs.edit().putString(KEY_HIDDEN_APPS, Gson().toJson(packageNames)).apply()
    }

    fun isAppHidden(packageName: String): Boolean {
        return getHiddenApps().contains(packageName)
    }

    fun toggleAppHidden(packageName: String) {
        val hidden = getHiddenApps().toMutableSet()
        if (hidden.contains(packageName)) {
            hidden.remove(packageName)
        } else {
            hidden.add(packageName)
        }
        setHiddenApps(hidden)
    }

    // ========== Pinned Home Apps ==========

    /**
     * Get ordered list of pinned home app package names.
     * If empty, the home screen shows the first N alphabetical apps (Olauncher default).
     */
    fun getPinnedHomeApps(): List<String> {
        val json = prefs.getString(KEY_PINNED_HOME_APPS, null) ?: return emptyList()
        return try {
            Gson().fromJson(json, object : TypeToken<List<String>>() {}.type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun setPinnedHomeApps(packageNames: List<String>) {
        prefs.edit().putString(KEY_PINNED_HOME_APPS, Gson().toJson(packageNames)).apply()
    }

    fun isAppPinnedToHome(packageName: String): Boolean {
        return getPinnedHomeApps().contains(packageName)
    }

    fun addAppToHome(packageName: String) {
        val pinned = getPinnedHomeApps().toMutableList()
        if (!pinned.contains(packageName)) {
            pinned.add(packageName)
            setPinnedHomeApps(pinned)
        }
    }

    fun removeAppFromHome(packageName: String) {
        val pinned = getPinnedHomeApps().toMutableList()
        pinned.remove(packageName)
        setPinnedHomeApps(pinned)
    }

    fun toggleAppOnHome(packageName: String) {
        if (isAppPinnedToHome(packageName)) {
            removeAppFromHome(packageName)
        } else {
            addAppToHome(packageName)
        }
    }

    // ========== Home App Count ==========

    /**
     * Get max number of apps on home screen.
     * 0 = show all non-hidden apps (Olauncher default).
     */
    fun getHomeAppCount(): Int = prefs.getInt(KEY_HOME_APP_COUNT, 16)

    fun setHomeAppCount(count: Int) = prefs.edit().putInt(KEY_HOME_APP_COUNT, count).apply()

    // ========== Gesture Actions ==========

    fun getSwipeLeftAction(): String = prefs.getString(KEY_SWIPE_LEFT_ACTION, ACTION_OPEN_APP_DRAWER) ?: ACTION_OPEN_APP_DRAWER
    fun setSwipeLeftAction(action: String) = prefs.edit().putString(KEY_SWIPE_LEFT_ACTION, action).apply()

    fun getSwipeRightAction(): String = prefs.getString(KEY_SWIPE_RIGHT_ACTION, ACTION_SHOW_NOTIFICATIONS) ?: ACTION_SHOW_NOTIFICATIONS
    fun setSwipeRightAction(action: String) = prefs.edit().putString(KEY_SWIPE_RIGHT_ACTION, action).apply()

    fun getSwipeUpAction(): String = prefs.getString(KEY_SWIPE_UP_ACTION, ACTION_OPEN_APP_DRAWER) ?: ACTION_OPEN_APP_DRAWER
    fun setSwipeUpAction(action: String) = prefs.edit().putString(KEY_SWIPE_UP_ACTION, action).apply()

    fun getSwipeDownAction(): String = prefs.getString(KEY_SWIPE_DOWN_ACTION, ACTION_SHOW_NOTIFICATIONS) ?: ACTION_SHOW_NOTIFICATIONS
    fun setSwipeDownAction(action: String) = prefs.edit().putString(KEY_SWIPE_DOWN_ACTION, action).apply()

    fun getDoubleTapAction(): String = prefs.getString(KEY_DOUBLE_TAP_ACTION, ACTION_LOCK_SCREEN) ?: ACTION_LOCK_SCREEN
    fun setDoubleTapAction(action: String) = prefs.edit().putString(KEY_DOUBLE_TAP_ACTION, action).apply()

    // ========== Display Settings ==========

    fun showClock(): Boolean = prefs.getBoolean(KEY_SHOW_CLOCK, true)
    fun setShowClock(show: Boolean) = prefs.edit().putBoolean(KEY_SHOW_CLOCK, show).apply()

    fun showDate(): Boolean = prefs.getBoolean(KEY_SHOW_DATE, true)
    fun setShowDate(show: Boolean) = prefs.edit().putBoolean(KEY_SHOW_DATE, show).apply()

    fun showBattery(): Boolean = prefs.getBoolean(KEY_SHOW_BATTERY, true)
    fun setShowBattery(show: Boolean) = prefs.edit().putBoolean(KEY_SHOW_BATTERY, show).apply()

    fun use24h(): Boolean = prefs.getBoolean(KEY_USE_24H, true)
    fun setUse24h(use: Boolean) = prefs.edit().putBoolean(KEY_USE_24H, use).apply()

    fun getDateFormat(): String = prefs.getString(KEY_DATE_FORMAT, "EEEE, d MMMM") ?: "EEEE, d MMMM"
    fun setDateFormat(format: String) = prefs.edit().putString(KEY_DATE_FORMAT, format).apply()

    fun showStatusBar(): Boolean = prefs.getBoolean(KEY_STATUS_BAR, true)
    fun setShowStatusBar(show: Boolean) = prefs.edit().putBoolean(KEY_STATUS_BAR, show).apply()

    fun showIconLabels(): Boolean = prefs.getBoolean(KEY_SHOW_ICON_LABELS, true)
    fun setShowIconLabels(show: Boolean) = prefs.edit().putBoolean(KEY_SHOW_ICON_LABELS, show).apply()

    fun showAlphabetHeaders(): Boolean = prefs.getBoolean(KEY_SHOW_ALPHABET_HEADERS, true)
    fun setShowAlphabetHeaders(show: Boolean) = prefs.edit().putBoolean(KEY_SHOW_ALPHABET_HEADERS, show).apply()

    fun getIconSizeMultiplier(): Float = prefs.getFloat(KEY_ICON_SIZE, 1.0f)
    fun setIconSizeMultiplier(size: Float) = prefs.edit().putFloat(KEY_ICON_SIZE, size).apply()

    fun autoShowKeyboard(): Boolean = prefs.getBoolean(KEY_AUTO_SHOW_KEYBOARD, false)
    fun setAutoShowKeyboard(auto: Boolean) = prefs.edit().putBoolean(KEY_AUTO_SHOW_KEYBOARD, auto).apply()

    // ========== Theme ==========

    fun getTheme(): String = prefs.getString(KEY_THEME, THEME_WALLPAPER) ?: THEME_WALLPAPER
    fun setTheme(theme: String) = prefs.edit().putString(KEY_THEME, theme).apply()

    // ========== Widget Area (reserved for future) ==========

    fun showWidgetArea(): Boolean = prefs.getBoolean(KEY_SHOW_WIDGET_AREA, false)
    fun setShowWidgetArea(show: Boolean) = prefs.edit().putBoolean(KEY_SHOW_WIDGET_AREA, show).apply()
}
