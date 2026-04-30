package com.minillauncher.ui

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.minillauncher.R
import com.minillauncher.utils.PreferencesManager

/**
 * Settings activity for the launcher.
 */
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
                addView(TextView(context).apply {
                    text = getString(R.string.settings)
                    textSize = 24f
                    setTextColor(getColor(R.color.text_primary))
                    setPadding(dp(24), 0, dp(24), dp(24))
                })

                // ===== HOME SCREEN =====
                addView(createSectionHeader(getString(R.string.home_screen)))
                addView(createSelectorItem(
                    getString(R.string.home_app_count),
                    getHomeAppCountLabel(prefs.getHomeAppCount()),
                    listOf(
                        getString(R.string.home_app_all) to PreferencesManager.HOME_APP_COUNT_ALL,
                        "4" to 4,
                        "8" to 8,
                        "12" to 12,
                        "16" to 16,
                        "24" to 24,
                        "32" to 32
                    )
                ) { prefs.setHomeAppCount(it as Int) })

                // ===== APPEARANCE =====
                addView(createSectionHeader(getString(R.string.appearance)))
                addView(createSwitchItem(getString(R.string.show_clock), prefs.showClock()) { prefs.setShowClock(it) })
                addView(createSwitchItem(getString(R.string.show_date), prefs.showDate()) { prefs.setShowDate(it) })
                addView(createSwitchItem(getString(R.string.show_battery), prefs.showBattery()) { prefs.setShowBattery(it) })
                addView(createSwitchItem(getString(R.string.use_24h_format), prefs.use24h()) { prefs.setUse24h(it) })
                addView(createSwitchItem(getString(R.string.show_icon_labels), prefs.showIconLabels()) { prefs.setShowIconLabels(it) })
                addView(createSwitchItem(getString(R.string.show_alphabet_headers), prefs.showAlphabetHeaders()) { prefs.setShowAlphabetHeaders(it) })
                addView(createSwitchItem(getString(R.string.show_status_bar), prefs.showStatusBar()) { prefs.setShowStatusBar(it) })
                addView(createSelectorItem(getString(R.string.theme), getThemeLabel(prefs.getTheme()),
                    listOf(getString(R.string.theme_system) to PreferencesManager.THEME_SYSTEM,
                        getString(R.string.theme_light) to PreferencesManager.THEME_LIGHT,
                        getString(R.string.theme_dark) to PreferencesManager.THEME_DARK,
                        getString(R.string.theme_wallpaper) to PreferencesManager.THEME_WALLPAPER
                    )) { prefs.setTheme(it) })

                // ===== GESTURES =====
                addView(createSectionHeader(getString(R.string.gestures)))
                addView(createSelectorItem(getString(R.string.swipe_left), getActionLabel(prefs.getSwipeLeftAction()), getActionOptions()) { prefs.setSwipeLeftAction(it) })
                addView(createSelectorItem(getString(R.string.swipe_right), getActionLabel(prefs.getSwipeRightAction()), getActionOptions()) { prefs.setSwipeRightAction(it) })
                addView(createSelectorItem(getString(R.string.swipe_up), getActionLabel(prefs.getSwipeUpAction()), getActionOptions()) { prefs.setSwipeUpAction(it) })
                addView(createSelectorItem(getString(R.string.swipe_down), getActionLabel(prefs.getSwipeDownAction()), getActionOptions()) { prefs.setSwipeDownAction(it) })
                addView(createSelectorItem(getString(R.string.double_tap), getActionLabel(prefs.getDoubleTapAction()), getActionOptions()) { prefs.setDoubleTapAction(it) })

                // ===== ADVANCED =====
                addView(createSectionHeader("Avanzate"))
                addView(createNavigationItem(getString(R.string.hidden_apps), getString(R.string.hidden_apps_description)) {
                    startActivity(Intent(this@SettingsActivity, HiddenAppsActivity::class.java))
                })
                addView(createNavigationItem("App in home", "Gestisci le app fissate sulla schermata principale") {
                    startActivity(Intent(this@SettingsActivity, HomeAppsActivity::class.java))
                })
                addView(createSwitchItem(getString(R.string.show_widget_area), prefs.showWidgetArea()) { prefs.setShowWidgetArea(it) })

                // ===== ABOUT =====
                addView(createSectionHeader(getString(R.string.about)))
                addView(createNavigationItem(getString(R.string.set_as_default), getString(R.string.tap_to_set_launcher)) {
                    try {
                        startActivity(Intent(android.provider.Settings.ACTION_HOME_SETTINGS))
                    } catch (e: Exception) {
                        Toast.makeText(this@SettingsActivity, "Impossibile aprire le impostazioni", Toast.LENGTH_SHORT).show()
                    }
                })
                addView(createInfoItem(getString(R.string.version), "1.1.0"))
            })
        }
    }

    private fun createSectionHeader(title: String): TextView {
        return TextView(this).apply {
            text = title.uppercase()
            textSize = 12f
            setTextColor(getColor(R.color.text_secondary))
            setPadding(dp(24), dp(24), dp(24), dp(8))
            letterSpacing = 0.1f
        }
    }

    private fun createSwitchItem(title: String, initialValue: Boolean, onToggle: (Boolean) -> Unit): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(getColor(R.color.settings_item_background))
            setPadding(dp(24), dp(16), dp(24), dp(16))
            isClickable = true
            isFocusable = true

            addView(TextView(context).apply {
                text = title
                textSize = 16f
                setTextColor(getColor(R.color.text_primary))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            })

            val switch = SwitchCompat(context).apply {
                isChecked = initialValue
                setOnCheckedChangeListener { _, isChecked -> onToggle(isChecked) }
            }
            addView(switch)

            setOnClickListener { switch.toggle() }
        }
    }

    private fun createSelectorItem(title: String, currentValue: String, options: List<Pair<String, Any>>, onSelect: (Any) -> Unit): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(getColor(R.color.settings_item_background))
            setPadding(dp(24), dp(16), dp(24), dp(16))
            isClickable = true
            isFocusable = true

            addView(TextView(context).apply {
                text = title
                textSize = 16f
                setTextColor(getColor(R.color.text_primary))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            })

            addView(TextView(context).apply {
                text = currentValue
                textSize = 14f
                setTextColor(getColor(R.color.text_secondary))
            })

            setOnClickListener { showSelectorDialog(title, options, currentValue, onSelect) }
        }
    }

    private fun createNavigationItem(title: String, subtitle: String, onClick: () -> Unit): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(getColor(R.color.settings_item_background))
            setPadding(dp(24), dp(16), dp(24), dp(16))
            isClickable = true
            isFocusable = true

            addView(TextView(context).apply {
                text = title
                textSize = 16f
                setTextColor(getColor(R.color.text_primary))
            })

            addView(TextView(context).apply {
                text = subtitle
                textSize = 12f
                setTextColor(getColor(R.color.text_secondary))
                setPadding(0, dp(4), 0, 0)
            })

            setOnClickListener { onClick() }
        }
    }

    private fun createInfoItem(title: String, value: String): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp(24), dp(16), dp(24), dp(16))

            addView(TextView(context).apply {
                text = title
                textSize = 14f
                setTextColor(getColor(R.color.text_secondary))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            })

            addView(TextView(context).apply {
                text = value
                textSize = 14f
                setTextColor(getColor(R.color.text_secondary))
            })
        }
    }

    private fun showSelectorDialog(title: String, options: List<Pair<String, Any>>, currentValue: String, onSelect: (Any) -> Unit) {
        val optionLabels = options.map { it.first }.toTypedArray()
        val currentIndex = optionLabels.indexOf(currentValue).coerceAtLeast(0)

        android.app.AlertDialog.Builder(this)
            .setTitle(title)
            .setSingleChoiceItems(optionLabels, currentIndex) { dialog, which ->
                onSelect(options[which].second)
                dialog.dismiss()
            }
            .setNegativeButton("Annulla", null)
            .show()
    }

    private fun getHomeAppCountLabel(count: Int): String = when (count) {
        PreferencesManager.HOME_APP_COUNT_ALL -> "Tutte"
        else -> "$count app"
    }

    private fun getThemeLabel(theme: String): String = when (theme) {
        PreferencesManager.THEME_LIGHT -> getString(R.string.theme_light)
        PreferencesManager.THEME_DARK -> getString(R.string.theme_dark)
        PreferencesManager.THEME_WALLPAPER -> getString(R.string.theme_wallpaper)
        else -> getString(R.string.theme_system)
    }

    private fun getActionLabel(action: String): String = when (action) {
        PreferencesManager.ACTION_OPEN_APP_DRAWER -> getString(R.string.action_open_app_drawer)
        PreferencesManager.ACTION_SHOW_NOTIFICATIONS -> getString(R.string.action_show_notifications)
        PreferencesManager.ACTION_LOCK_SCREEN -> getString(R.string.action_lock_screen)
        PreferencesManager.ACTION_OPEN_SEARCH -> getString(R.string.action_open_search)
        else -> getString(R.string.action_none)
    }

    private fun getActionOptions(): List<Pair<String, String>> = listOf(
        getString(R.string.action_none) to PreferencesManager.ACTION_NONE,
        getString(R.string.action_open_app_drawer) to PreferencesManager.ACTION_OPEN_APP_DRAWER,
        getString(R.string.action_show_notifications) to PreferencesManager.ACTION_SHOW_NOTIFICATIONS,
        getString(R.string.action_lock_screen) to PreferencesManager.ACTION_LOCK_SCREEN,
        getString(R.string.action_open_search) to PreferencesManager.ACTION_OPEN_SEARCH
    )

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
