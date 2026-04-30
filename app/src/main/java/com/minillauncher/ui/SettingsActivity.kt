package com.minillauncher.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.minillauncher.utils.PreferencesManager

/**
 * Settings activity for the launcher.
 * Allows customization of appearance, gestures, and hidden apps.
 */
class SettingsActivity : AppCompatActivity() {

    private lateinit var prefs: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = PreferencesManager(this)
        setContentView(buildSettingsUI())
    }

    private fun buildSettingsUI(): ScrollView {
        return ScrollView(this).apply {
            isFillViewport = true
            setBackgroundColor(getColor(R.color.settings_background))
            addView(LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(0, 24.dp, 0, 48.dp)

                // Title
                addView(TextView(context).apply {
                    text = getString(R.string.settings)
                    textSize = 24f
                    setTextColor(getColor(R.color.text_primary))
                    setPadding(24.dp, 0, 24.dp, 24.dp)
                })

                // ===== APPEARANCE SECTION =====
                addView(createSectionHeader(getString(R.string.appearance)))

                // Show Clock
                addView(createSwitchItem(
                    getString(R.string.show_clock),
                    prefs.showClock(),
                    onToggle = { prefs.setShowClock(it) }
                ))

                // Show Date
                addView(createSwitchItem(
                    getString(R.string.show_date),
                    prefs.showDate(),
                    onToggle = { prefs.setShowDate(it) }
                ))

                // 24h format
                addView(createSwitchItem(
                    getString(R.string.use_24h_format),
                    prefs.use24h(),
                    onToggle = { prefs.setUse24h(it) }
                ))

                // Show icon labels
                addView(createSwitchItem(
                    getString(R.string.show_icon_labels),
                    prefs.showIconLabels(),
                    onToggle = { prefs.setShowIconLabels(it) }
                ))

                // Alphabet headers
                addView(createSwitchItem(
                    getString(R.string.show_alphabet_headers),
                    prefs.showAlphabetHeaders(),
                    onToggle = { prefs.setShowAlphabetHeaders(it) }
                ))

                // Status bar
                addView(createSwitchItem(
                    getString(R.string.show_status_bar),
                    prefs.showStatusBar(),
                    onToggle = { prefs.setShowStatusBar(it) }
                ))

                // Theme selector
                addView(createSelectorItem(
                    getString(R.string.theme),
                    getThemeLabel(prefs.getTheme()),
                    listOf(
                        getString(R.string.theme_system) to PreferencesManager.THEME_SYSTEM,
                        getString(R.string.theme_light) to PreferencesManager.THEME_LIGHT,
                        getString(R.string.theme_dark) to PreferencesManager.THEME_DARK,
                        getString(R.string.theme_wallpaper) to PreferencesManager.THEME_WALLPAPER
                    )
                ) { prefs.setTheme(it) })

                // ===== GESTURES SECTION =====
                addView(createSectionHeader(getString(R.string.gestures)))

                // Swipe Left
                addView(createSelectorItem(
                    getString(R.string.swipe_left),
                    getActionLabel(prefs.getSwipeLeftAction()),
                    getActionOptions()
                ) { prefs.setSwipeLeftAction(it) })

                // Swipe Right
                addView(createSelectorItem(
                    getString(R.string.swipe_right),
                    getActionLabel(prefs.getSwipeRightAction()),
                    getActionOptions()
                ) { prefs.setSwipeRightAction(it) })

                // Swipe Up
                addView(createSelectorItem(
                    getString(R.string.swipe_up),
                    getActionLabel(prefs.getSwipeUpAction()),
                    getActionOptions()
                ) { prefs.setSwipeUpAction(it) })

                // Swipe Down
                addView(createSelectorItem(
                    getString(R.string.swipe_down),
                    getActionLabel(prefs.getSwipeDownAction()),
                    getActionOptions()
                ) { prefs.setSwipeDownAction(it) })

                // Double Tap
                addView(createSelectorItem(
                    getString(R.string.double_tap),
                    getActionLabel(prefs.getDoubleTapAction()),
                    getActionOptions()
                ) { prefs.setDoubleTapAction(it) })

                // ===== ADVANCED SECTION =====
                addView(createSectionHeader("Avanzate"))

                // Hidden Apps
                addView(createNavigationItem(
                    getString(R.string.hidden_apps),
                    getString(R.string.hidden_apps_description)
                ) {
                    startActivity(Intent(this@SettingsActivity, HiddenAppsActivity::class.java))
                })

                // Widget Area (future)
                addView(createSwitchItem(
                    getString(R.string.show_widget_area),
                    prefs.showWidgetArea(),
                    onToggle = { prefs.setShowWidgetArea(it) }
                ))

                // ===== ABOUT SECTION =====
                addView(createSectionHeader(getString(R.string.about)))

                // Set as default
                addView(createNavigationItem(
                    getString(R.string.set_as_default),
                    getString(R.string.tap_to_set_launcher)
                ) {
                    try {
                        val intent = Intent(android.provider.Settings.ACTION_HOME_SETTINGS)
                        startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(this@SettingsActivity, "Impossibile aprire le impostazioni", Toast.LENGTH_SHORT).show()
                    }
                })

                // Version
                addView(createInfoItem(
                    getString(R.string.version),
                    "1.0.0"
                ))

            })
        }
    }

    // ==================== UI COMPONENTS ====================

    private fun createSectionHeader(title: String): TextView {
        return TextView(this).apply {
            text = title.uppercase()
            textSize = 12f
            setTextColor(getColor(R.color.text_secondary))
            setPadding(24.dp, 24.dp, 24.dp, 8.dp)
            letterSpacing = 0.1f
        }
    }

    private fun createSwitchItem(
        title: String,
        initialValue: Boolean,
        onToggle: (Boolean) -> Unit
    ): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(getColor(R.color.settings_item_background))
            setPadding(24.dp, 16.dp, 24.dp, 16.dp)
            isClickable = true
            isFocusable = true

            addView(TextView(context).apply {
                text = title
                textSize = 16f
                setTextColor(getColor(R.color.text_primary))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            })

            val switch = android.widget.SwitchCompat(context).apply {
                isChecked = initialValue
                setOnCheckedChangeListener { _, isChecked ->
                    onToggle(isChecked)
                }
            }
            addView(switch)

            setOnClickListener { switch.toggle() }
        }
    }

    private fun createSelectorItem(
        title: String,
        currentValue: String,
        options: List<Pair<String, String>>,
        onSelect: (String) -> Unit
    ): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(getColor(R.color.settings_item_background))
            setPadding(24.dp, 16.dp, 24.dp, 16.dp)
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

            setOnClickListener {
                showSelectorDialog(title, options, currentValue, onSelect)
            }
        }
    }

    private fun createNavigationItem(
        title: String,
        subtitle: String,
        onClick: () -> Unit
    ): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(getColor(R.color.settings_item_background))
            setPadding(24.dp, 16.dp, 24.dp, 16.dp)
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
                setPadding(0, 4.dp, 0, 0)
            })

            setOnClickListener { onClick() }
        }
    }

    private fun createInfoItem(title: String, value: String): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(24.dp, 16.dp, 24.dp, 16.dp)

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

    private fun showSelectorDialog(
        title: String,
        options: List<Pair<String, String>>,
        currentValue: String,
        onSelect: (String) -> Unit
    ) {
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

    // ==================== HELPERS ====================

    private fun getThemeLabel(theme: String): String {
        return when (theme) {
            PreferencesManager.THEME_LIGHT -> getString(R.string.theme_light)
            PreferencesManager.THEME_DARK -> getString(R.string.theme_dark)
            PreferencesManager.THEME_WALLPAPER -> getString(R.string.theme_wallpaper)
            else -> getString(R.string.theme_system)
        }
    }

    private fun getActionLabel(action: String): String {
        return when (action) {
            PreferencesManager.ACTION_OPEN_APP_DRAWER -> getString(R.string.action_open_app_drawer)
            PreferencesManager.ACTION_SHOW_NOTIFICATIONS -> getString(R.string.action_show_notifications)
            PreferencesManager.ACTION_LOCK_SCREEN -> getString(R.string.action_lock_screen)
            PreferencesManager.ACTION_OPEN_SEARCH -> getString(R.string.action_open_search)
            else -> getString(R.string.action_none)
        }
    }

    private fun getActionOptions(): List<Pair<String, String>> {
        return listOf(
            getString(R.string.action_none) to PreferencesManager.ACTION_NONE,
            getString(R.string.action_open_app_drawer) to PreferencesManager.ACTION_OPEN_APP_DRAWER,
            getString(R.string.action_show_notifications) to PreferencesManager.ACTION_SHOW_NOTIFICATIONS,
            getString(R.string.action_lock_screen) to PreferencesManager.ACTION_LOCK_SCREEN,
            getString(R.string.action_open_search) to PreferencesManager.ACTION_OPEN_SEARCH
        )
    }

    // Extension: dp to px
    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).toInt()
}
