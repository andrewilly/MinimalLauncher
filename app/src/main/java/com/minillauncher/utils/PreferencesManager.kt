package com.minillauncher.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Manages all launcher preferences.
 */
class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("minillauncher_prefs", Context.MODE_PRIVATE)

    companion object {
        // Hidden apps
        const val KEY_HIDDEN_APPS = "hidden_apps"
        // Pinned home apps (ordered list)
        const val KEY_PINNED_HOME_APPS = "pinned_home_apps"
        // Max apps on home screen (0 = show all)
        const val KEY_HOME_APP_COUNT = "home_app_count"
        // Gestures
        const val KEY_SWIPE_LEFT_ACTION = "swipe_left_action"
        const val KEY_SWIPE_RIGHT_ACTION = "swipe_right_action"
        const val KEY_SWIPE_UP_ACTION = "swipe_up_action"
        const val KEY_SWIPE_DOWN_ACTION = "swipe_down_action"
        const val KEY_DOUBLE_TAP_ACTION = "double_tap_action"
        // Display
        const val KEY_SHOW_CLOCK = "show_clock"
        const val KEY_SHOW_DATE = "show_date"
        const val KEY_SHOW_BATTERY = "show_battery"
        const val KEY_USE_24H = "pref_use_24h"
        const val KEY_DATE_FORMAT = "pref_date_format"
        const val KEY_STATUS_BAR = "show_status_bar"
        const val KEY_SHOW_ALPHABET_HEADERS = "show_alphabet_headers"
        const val KEY_AUTO_SHOW_KEYBOARD = "auto_show_keyboard"
        const val KEY_THEME = "theme"

        // Gesture action values
        const val ACTION_NONE = "none"
        const val ACTION_LOCK_SCREEN = "lock_screen"
        const val ACTION_OPEN_APP_DRAWER = "open_app_drawer"
        const val ACTION_SHOW_NOTIFICATIONS = "show_notifications"
        const val ACTION_OPEN_SEARCH = "open_search"

        // Theme values
        const val THEME_SYSTEM = "system"
        const val THEME_LIGHT = "light"
        const val THEME_DARK = "dark"
        const val THEME_WALLPAPER = "wallpaper"

        // Home app count: show all
        const val HOME_APP_COUNT_ALL = 0
    }

    // ==================== Hidden Apps ====================

    fun getHiddenApps(): Set<String> {
        val json = prefs.getString(KEY_HIDDEN_APPS, null) ?: return emptySet()
        return try { Gson().fromJson(json, object : TypeToken<Set<String>>() {}.type) } catch (_: Exception) { emptySet() }
    }

    fun setHiddenApps(packageNames: Set<String>) {
        prefs.edit().putString(KEY_HIDDEN_APPS, Gson().toJson(packageNames)).apply()
    }

    fun isAppHidden(packageName: String): Boolean = getHiddenApps().contains(packageName)

    fun toggleAppHidden(packageName: String) {
        val hidden = getHiddenApps().toMutableSet()
        if (hidden.contains(packageName)) hidden.remove(packageName) else hidden.add(packageName)
        setHiddenApps(hidden)
    }

    // ==================== Pinned Home Apps ====================

    fun getPinnedHomeApps(): List<String> {
        val json = prefs.getString(KEY_PINNED_HOME_APPS, null) ?: return emptyList()
        return try { Gson().fromJson(json, object : TypeToken<List<String>>() {}.type) ?: emptyList() } catch (_: Exception) { emptyList() }
    }

    fun setPinnedHomeApps(packageNames: List<String>) {
        prefs.edit().putString(KEY_PINNED_HOME_APPS, Gson().toJson(packageNames)).apply()
    }

    fun isAppPinnedToHome(packageName: String): Boolean = getPinnedHomeApps().contains(packageName)

    fun addAppToHome(packageName: String) {
        val pinned = getPinnedHomeApps().toMutableList()
        if (!pinned.contains(packageName)) { pinned.add(packageName); setPinnedHomeApps(pinned) }
    }

    fun removeAppFromHome(packageName: String) {
        val pinned = getPinnedHomeApps().toMutableList()
        if (pinned.remove(packageName)) setPinnedHomeApps(pinned)
    }

    fun toggleAppOnHome(packageName: String) {
        if (isAppPinnedToHome(packageName)) removeAppFromHome(packageName) else addAppToHome(packageName)
    }

    // ==================== Home App Count ====================

    fun getHomeAppCount(): Int = prefs.getInt(KEY_HOME_APP_COUNT, HOME_APP_COUNT_ALL)

    fun setHomeAppCount(count: Int) = prefs.edit().putInt(KEY_HOME_APP_COUNT, count).apply()

    // ==================== Gestures ====================

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

    // ==================== Display ====================

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

    fun showStatusBar(): Boolean = prefs.getBoolean(KEY_STATUS_BAR, false)
    fun setShowStatusBar(show: Boolean) = prefs.edit().putBoolean(KEY_STATUS_BAR, show).apply()

    fun showAlphabetHeaders(): Boolean = prefs.getBoolean(KEY_SHOW_ALPHABET_HEADERS, true)
    fun setShowAlphabetHeaders(show: Boolean) = prefs.edit().putBoolean(KEY_SHOW_ALPHABET_HEADERS, show).apply()

    fun autoShowKeyboard(): Boolean = prefs.getBoolean(KEY_AUTO_SHOW_KEYBOARD, false)
    fun setAutoShowKeyboard(auto: Boolean) = prefs.edit().putBoolean(KEY_AUTO_SHOW_KEYBOARD, auto).apply()

    // ==================== Theme ====================

    fun getTheme(): String = prefs.getString(KEY_THEME, THEME_WALLPAPER) ?: THEME_WALLPAPER
    fun setTheme(theme: String) = prefs.edit().putString(KEY_THEME, theme).apply()
}
