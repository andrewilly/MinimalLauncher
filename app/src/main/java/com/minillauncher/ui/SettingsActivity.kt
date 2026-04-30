package com.minillauncher.ui

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.minillauncher.BuildConfig
import com.minillauncher.R
import com.minillauncher.utils.PreferencesManager
import com.minillauncher.utils.dp

class SettingsActivity : AppCompatActivity() {

    private lateinit var prefs: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = PreferencesManager(this)
        setContentView(buildSettingsUI())
    }

    private fun buildSettingsUI(): android.widget.ScrollView {
        return android.widget.ScrollView(this).apply {
            isFillViewport = true
            setBackgroundColor(getColor(R.color.settings_background))
            addView(LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(0, dp(24), 0, dp(48))

                // Title
                addView(makeTitle(getString(R.string.settings)))

                // ===== HOME SCREEN =====
                addView(makeSectionHeader(getString(R.string.home_screen)))
                addView(makeSelector(
                    getString(R.string.home_app_count),
                    getHomeAppCountLabel(prefs.getHomeAppCount()),
                    listOf(
                        getString(R.string.home_app_all) to PreferencesManager.HOME_APP_COUNT_ALL,
                        "4" to 4, "8" to 8, "12" to 12, "16" to 16, "24" to 24, "32" to 32
                    )
                ) { prefs.setHomeAppCount(it as Int) })

                // ===== APPEARANCE =====
                addView(makeSectionHeader(getString(R.string.appearance)))
                addView(makeSwitch(getString(R.string.show_clock), prefs.showClock()) { prefs.setShowClock(it) })
                addView(makeSwitch(getString(R.string.show_date), prefs.showDate()) { prefs.setShowDate(it) })
                addView(makeSwitch(getString(R.string.show_battery), prefs.showBattery()) { prefs.setShowBattery(it) })
                addView(makeSwitch(getString(R.string.use_24h_format), prefs.use24h()) { prefs.setUse24h(it) })
                addView(makeSwitch(getString(R.string.show_status_bar), prefs.showStatusBar()) { prefs.setShowStatusBar(it) })
                addView(makeSwitch(getString(R.string.show_alphabet_headers), prefs.showAlphabetHeaders()) { prefs.setShowAlphabetHeaders(it) })
                addView(makeSelector(getString(R.string.theme), getThemeLabel(prefs.getTheme()),
                    listOf(getString(R.string.theme_wallpaper) to PreferencesManager.THEME_WALLPAPER,
                        getString(R.string.theme_dark) to PreferencesManager.THEME_DARK,
                        getString(R.string.theme_light) to PreferencesManager.THEME_LIGHT,
                        getString(R.string.theme_system) to PreferencesManager.THEME_SYSTEM
                    )) { prefs.setTheme(it as String) })

                // ===== GESTURES =====
                addView(makeSectionHeader(getString(R.string.gestures)))
                addView(makeSelector(getString(R.string.swipe_left), getActionLabel(prefs.getSwipeLeftAction()), getActionOptions()) { prefs.setSwipeLeftAction(it as String) })
                addView(makeSelector(getString(R.string.swipe_right), getActionLabel(prefs.getSwipeRightAction()), getActionOptions()) { prefs.setSwipeRightAction(it as String) })
                addView(makeSelector(getString(R.string.swipe_up), getActionLabel(prefs.getSwipeUpAction()), getActionOptions()) { prefs.setSwipeUpAction(it as String) })
                addView(makeSelector(getString(R.string.swipe_down), getActionLabel(prefs.getSwipeDownAction()), getActionOptions()) { prefs.setSwipeDownAction(it as String) })
                addView(makeSelector(getString(R.string.double_tap), getActionLabel(prefs.getDoubleTapAction()), getActionOptions()) { prefs.setDoubleTapAction(it as String) })

                // ===== ADVANCED =====
                addView(makeSectionHeader("Avanzate"))
                addView(makeNavigation(getString(R.string.hidden_apps), getString(R.string.hidden_apps_description)) {
                    startActivity(Intent(this@SettingsActivity, HiddenAppsActivity::class.java))
                })
                addView(makeNavigation("App in home", "Scegli quali app fissare sulla schermata principale") {
                    startActivity(Intent(this@SettingsActivity, HomeAppsActivity::class.java))
                })
                addView(makeSwitch("Tastiera automatica", prefs.autoShowKeyboard()) { prefs.setAutoShowKeyboard(it) })

                // ===== ABOUT =====
                addView(makeSectionHeader(getString(R.string.about)))
                addView(makeNavigation(getString(R.string.set_as_default), getString(R.string.tap_to_set_launcher)) {
                    try { startActivity(Intent(android.provider.Settings.ACTION_HOME_SETTINGS)) }
                    catch (e: Exception) { Toast.makeText(this@SettingsActivity, "Impossibile aprire", Toast.LENGTH_SHORT).show() }
                })
                addView(makeInfoRow(getString(R.string.version), BuildConfig.VERSION_NAME))
            })
        }
    }

    // ===== UI Builders (dark text on light background) =====

    private fun makeTitle(text: String) = TextView(this).apply {
        this.text = text; textSize = 24f
        setTextColor(getColor(R.color.text_primary_light))
        setPadding(dp(24), 0, dp(24), dp(24))
    }

    private fun makeSectionHeader(text: String) = TextView(this).apply {
        this.text = text.uppercase(); textSize = 12f
        setTextColor(getColor(R.color.text_secondary_light))
        setPadding(dp(24), dp(24), dp(24), dp(8)); letterSpacing = 0.1f
    }

    private fun makeSwitch(title: String, initial: Boolean, onToggle: (Boolean) -> Unit) = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        setBackgroundColor(getColor(R.color.settings_item_background))
        setPadding(dp(24), dp(16), dp(24), dp(16))
        isClickable = true; isFocusable = true

        addView(TextView(context).apply {
            this.text = title; textSize = 16f
            setTextColor(getColor(R.color.text_primary_light))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        })
        val sw = SwitchCompat(context).apply { isChecked = initial; setOnCheckedChangeListener { _, v -> onToggle(v) } }
        addView(sw)
        setOnClickListener { sw.toggle() }
    }

    private fun makeSelector(title: String, current: String, options: List<Pair<String, Any>>, onSelect: (Any) -> Unit) = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        setBackgroundColor(getColor(R.color.settings_item_background))
        setPadding(dp(24), dp(16), dp(24), dp(16))
        isClickable = true; isFocusable = true

        addView(TextView(context).apply {
            this.text = title; textSize = 16f
            setTextColor(getColor(R.color.text_primary_light))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        })
        addView(TextView(context).apply {
            this.text = current; textSize = 14f
            setTextColor(getColor(R.color.text_secondary_light))
        })
        setOnClickListener { showSelectorDialog(title, options, onSelect) }
    }

    private fun makeNavigation(title: String, subtitle: String, onClick: () -> Unit) = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setBackgroundColor(getColor(R.color.settings_item_background))
        setPadding(dp(24), dp(16), dp(24), dp(16))
        isClickable = true; isFocusable = true

        addView(TextView(context).apply {
            this.text = title; textSize = 16f
            setTextColor(getColor(R.color.text_primary_light))
        })
        addView(TextView(context).apply {
            this.text = subtitle; textSize = 12f
            setTextColor(getColor(R.color.text_secondary_light))
            setPadding(0, dp(4), 0, 0)
        })
        setOnClickListener { onClick() }
    }

    private fun makeInfoRow(label: String, value: String) = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL; setPadding(dp(24), dp(16), dp(24), dp(16))
        addView(TextView(context).apply {
            this.text = label; textSize = 14f
            setTextColor(getColor(R.color.text_secondary_light))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        })
        addView(TextView(context).apply {
            this.text = value; textSize = 14f
            setTextColor(getColor(R.color.text_secondary_light))
        })
    }

    private fun showSelectorDialog(title: String, options: List<Pair<String, Any>>, onSelect: (Any) -> Unit) {
        val labels = options.map { it.first }.toTypedArray()
        android.app.AlertDialog.Builder(this)
            .setTitle(title)
            .setItems(labels) { dialog, which -> onSelect(options[which].second); dialog.dismiss() }
            .setNegativeButton("Annulla", null)
            .show()
    }

    private fun getHomeAppCountLabel(count: Int) = if (count == PreferencesManager.HOME_APP_COUNT_ALL) "Tutte" else "$count app"

    private fun getThemeLabel(theme: String) = when (theme) {
        PreferencesManager.THEME_LIGHT -> getString(R.string.theme_light)
        PreferencesManager.THEME_DARK -> getString(R.string.theme_dark)
        PreferencesManager.THEME_WALLPAPER -> getString(R.string.theme_wallpaper)
        else -> getString(R.string.theme_system)
    }

    private fun getActionLabel(action: String) = when (action) {
        PreferencesManager.ACTION_OPEN_APP_DRAWER -> getString(R.string.action_open_app_drawer)
        PreferencesManager.ACTION_SHOW_NOTIFICATIONS -> getString(R.string.action_show_notifications)
        PreferencesManager.ACTION_LOCK_SCREEN -> getString(R.string.action_lock_screen)
        PreferencesManager.ACTION_OPEN_SEARCH -> getString(R.string.action_open_search)
        else -> getString(R.string.action_none)
    }

    private fun getActionOptions() = listOf(
        getString(R.string.action_none) to PreferencesManager.ACTION_NONE,
        getString(R.string.action_open_app_drawer) to PreferencesManager.ACTION_OPEN_APP_DRAWER,
        getString(R.string.action_show_notifications) to PreferencesManager.ACTION_SHOW_NOTIFICATIONS,
        getString(R.string.action_lock_screen) to PreferencesManager.ACTION_LOCK_SCREEN,
        getString(R.string.action_open_search) to PreferencesManager.ACTION_OPEN_SEARCH
    )
}
