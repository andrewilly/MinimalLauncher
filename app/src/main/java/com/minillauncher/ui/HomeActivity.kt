package com.minillauncher.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GestureDetectorCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.minillauncher.R
import com.minillauncher.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeActivity : AppCompatActivity() {

    private lateinit var prefs: PreferencesManager
    private lateinit var rootContainer: FrameLayout
    private lateinit var textClock: TextView
    private lateinit var textDate: TextView
    private lateinit var textBattery: TextView
    private lateinit var dateBatterySep: TextView
    private lateinit var dateBatteryRow: LinearLayout
    private lateinit var recyclerHomeApps: RecyclerView
    private lateinit var settingsTouchArea: View

    private val homeApps = mutableListOf<AppInfo>()
    private val allApps = mutableListOf<AppInfo>()
    private var homeAdapter: HomeAppAdapter? = null
    private var clockHandler: Handler? = null
    private var clockRunnable: Runnable? = null
    private var gestureDetector: GestureDetectorCompat? = null
    private var appsLoadJob: kotlinx.coroutines.Job? = null

    // Battery change receiver
    private var batteryReceiver: BroadcastReceiver? = null
    private var cachedBattery = ClockUtils.getBatteryInfo(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            prefs = PreferencesManager(this)
            applyTheme()
            setContentView(R.layout.activity_home)
            window.setBackgroundDrawableResource(android.R.color.transparent)
            setupImmersiveMode()
            initViews()
            setupGestures()
            setupBackPressed()
            registerBatteryReceiver()
            loadApps()
            startClockUpdates()
        } catch (e: Exception) {
            Toast.makeText(this, "Errore avvio: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()
        setupImmersiveMode()
        loadApps()
    }

    override fun onPause() {
        super.onPause()
        unregisterBatteryReceiver()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopClockUpdates()
        unregisterBatteryReceiver()
    }

    // ==================== Touch Dispatch (FIX: gestures work everywhere) ====================

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        gestureDetector?.onTouchEvent(ev)
        return super.dispatchTouchEvent(ev)
    }

    // ==================== Init ====================

    private fun initViews() {
        rootContainer = findViewById(R.id.root_container)
        textClock = findViewById(R.id.text_clock)
        textDate = findViewById(R.id.text_date)
        textBattery = findViewById(R.id.text_battery)
        dateBatterySep = findViewById(R.id.text_date_battery_sep)
        dateBatteryRow = findViewById(R.id.date_battery_row)
        recyclerHomeApps = findViewById(R.id.recycler_home_apps)
        settingsTouchArea = findViewById(R.id.settings_touch_area)

        recyclerHomeApps.layoutManager = LinearLayoutManager(this)
        homeAdapter = HomeAppAdapter(
            apps = homeApps,
            onAppClick = { app -> AppUtils.launchApp(this, app) },
            onAppLongClick = { app, view -> showAppMenu(app, view) }
        )
        recyclerHomeApps.adapter = homeAdapter

        settingsTouchArea.setOnLongClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
            true
        }

        updateVisibilityPrefs()
    }

    private fun updateVisibilityPrefs() {
        textClock.visibility = if (prefs.showClock()) View.VISIBLE else View.GONE
        val showDate = prefs.showDate()
        val showBattery = prefs.showBattery()
        textDate.visibility = if (showDate) View.VISIBLE else View.GONE
        textBattery.visibility = if (showBattery) View.VISIBLE else View.GONE
        dateBatterySep.visibility = if (showDate && showBattery) View.VISIBLE else View.GONE
        dateBatteryRow.visibility = if (showDate || showBattery) View.VISIBLE else View.GONE
    }

    // ==================== Gestures ====================

    private fun setupGestures() {
        gestureDetector = GestureDetectorCompat(this, object : GestureDetector.SimpleOnGestureListener() {

            override fun onDoubleTap(e: MotionEvent): Boolean {
                when (prefs.getDoubleTapAction()) {
                    PreferencesManager.ACTION_LOCK_SCREEN -> lockScreen()
                    PreferencesManager.ACTION_OPEN_APP_DRAWER -> openAppDrawer()
                    PreferencesManager.ACTION_OPEN_SEARCH -> openAppDrawer()
                }
                return true
            }

            override fun onFling(e1: MotionEvent?, e2: MotionEvent, vx: Float, vy: Float): Boolean {
                if (e1 == null) return false
                val dx = e2.x - e1.x
                val dy = e2.y - e1.y
                val threshold = 80f

                if (Math.abs(dx) > Math.abs(dy) && Math.abs(dx) > threshold) {
                    if (dx < 0) handleSwipeLeft() else handleSwipeRight()
                    return true
                } else if (Math.abs(dy) > threshold) {
                    // Vertical fling: only in upper 35% of screen
                    val screenH = rootContainer.height.toFloat()
                    if (e1.y < screenH * 0.35f) {
                        if (dy < 0) handleSwipeUp() else handleSwipeDown()
                        return true
                    }
                }
                return false
            }
        })
    }

    private fun handleSwipeLeft() {
        when (prefs.getSwipeLeftAction()) {
            PreferencesManager.ACTION_OPEN_APP_DRAWER -> openAppDrawer()
            PreferencesManager.ACTION_SHOW_NOTIFICATIONS -> expandNotifications()
        }
    }

    private fun handleSwipeRight() {
        when (prefs.getSwipeRightAction()) {
            PreferencesManager.ACTION_SHOW_NOTIFICATIONS -> expandNotifications()
            PreferencesManager.ACTION_OPEN_APP_DRAWER -> openAppDrawer()
        }
    }

    private fun handleSwipeUp() {
        when (prefs.getSwipeUpAction()) {
            PreferencesManager.ACTION_OPEN_SEARCH,
            PreferencesManager.ACTION_OPEN_APP_DRAWER -> openAppDrawer()
        }
    }

    private fun handleSwipeDown() {
        when (prefs.getSwipeDownAction()) {
            PreferencesManager.ACTION_SHOW_NOTIFICATIONS -> expandNotifications()
        }
    }

    // ==================== Actions ====================

    private fun openAppDrawer() {
        startActivity(Intent(this, AppDrawerActivity::class.java))
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right)
    }

    @Suppress("DEPRECATION")
    private fun lockScreen() {
        try {
            (getSystemService(Context.DEVICE_POLICY_SERVICE) as android.app.admin.DevicePolicyManager).lockNow()
        } catch (_: Exception) {}
    }

    @Suppress("DEPRECATION")
    private fun expandNotifications() {
        try {
            val sb = getSystemService("statusbar")
            if (sb != null) {
                val methodName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    "expandNotifications" else "expandNotificationsPanel"
                for (m in sb.javaClass.methods) {
                    if (m.name == methodName) { m.invoke(sb); return }
                }
            }
        } catch (_: Exception) {}
    }

    // ==================== Apps ====================

    private fun loadApps() {
        // Cancel previous load to avoid duplicate work
        appsLoadJob?.cancel()
        appsLoadJob = lifecycleScope.launch {
            val apps = withContext(Dispatchers.IO) { AppUtils.loadLaunchableApps(this@HomeActivity) }

            // Show feedback if no apps found
            if (apps.isEmpty()) {
                android.util.Log.w("MinimalLauncher", "No apps loaded!")
            }

            allApps.clear()
            allApps.addAll(apps)

            val hidden = prefs.getHiddenApps()
            val pinned = prefs.getPinnedHomeApps()
            val maxCount = prefs.getHomeAppCount()
            val nonHidden = apps.filter { !hidden.contains(it.packageName) }

            homeApps.clear()

            if (pinned.isNotEmpty()) {
                // Show pinned apps first in saved order
                val orderedPinned = pinned.mapNotNull { pkg ->
                    nonHidden.find { it.packageName == pkg }
                }
                homeApps.addAll(orderedPinned)
                // Fill remaining with alphabetical apps
                if (maxCount == PreferencesManager.HOME_APP_COUNT_ALL || homeApps.size < maxCount) {
                    val remaining = nonHidden.filter { !pinned.contains(it.packageName) }
                    val limit = if (maxCount == PreferencesManager.HOME_APP_COUNT_ALL) remaining.size
                    else maxCount - homeApps.size
                    homeApps.addAll(remaining.take(limit))
                }
            } else {
                // Olauncher default: alphabetical
                if (maxCount == PreferencesManager.HOME_APP_COUNT_ALL) {
                    homeApps.addAll(nonHidden)
                } else {
                    homeApps.addAll(nonHidden.take(maxCount))
                }
            }

            homeAdapter?.updateApps(homeApps)
        }
    }

    private fun showAppMenu(app: AppInfo, anchorView: View) {
        val popup = android.widget.PopupMenu(this, anchorView)
        popup.menuInflater.inflate(R.menu.menu_app_long_press, popup.menu)
        val isPinned = prefs.isAppPinnedToHome(app.packageName)
        popup.menu.add(0, 100, 0,
            if (isPinned) getString(R.string.menu_remove_from_home) else getString(R.string.menu_add_to_home)
        )

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_app_info -> { AppUtils.openAppSettings(this, app.packageName); true }
                R.id.menu_hide_app -> { prefs.toggleAppHidden(app.packageName); loadApps(); true }
                R.id.menu_uninstall -> {
                    startActivity(Intent(Intent.ACTION_DELETE,
                        android.net.Uri.parse("package:${app.packageName}")).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        })
                    true
                }
                100 -> { prefs.toggleAppOnHome(app.packageName); loadApps(); true }
                else -> false
            }
        }
        popup.show()
    }

    // ==================== Clock + Battery ====================

    private fun startClockUpdates() {
        clockHandler = Handler(Looper.getMainLooper())
        updateDisplay()
        clockRunnable = object : Runnable {
            override fun run() {
                updateDisplay()
                clockHandler?.postDelayed(this, 1000)
            }
        }
        clockHandler?.post(clockRunnable!!)
    }

    private fun stopClockUpdates() {
        clockRunnable?.let { clockHandler?.removeCallbacks(it) }
    }

    private fun updateDisplay() {
        textClock.text = ClockUtils.getTime(prefs.use24h())
        if (prefs.showDate()) {
            textDate.text = ClockUtils.getDate(prefs.getDateFormat())
        }
        if (prefs.showBattery()) {
            val info = cachedBattery
            if (info.percentage >= 0) {
                textBattery.text = if (info.isCharging) "${info.percentage}% \u26A1" else "${info.percentage}%"
            }
        }
    }

    // ==================== Battery Receiver (efficient) ====================

    private fun registerBatteryReceiver() {
        if (batteryReceiver != null) return
        batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == Intent.ACTION_BATTERY_CHANGED) {
                    cachedBattery = ClockUtils.getBatteryInfo(context)
                }
            }
        }
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        registerReceiver(batteryReceiver, filter)
    }

    private fun unregisterBatteryReceiver() {
        try {
            batteryReceiver?.let { unregisterReceiver(it) }
        } catch (_: Exception) {}
        batteryReceiver = null
    }

    // ==================== Theme ====================

    private fun applyTheme() {
        when (prefs.getTheme()) {
            PreferencesManager.THEME_LIGHT -> setTheme(R.style.Theme_MinimalLauncher_Drawer)
            PreferencesManager.THEME_DARK -> setTheme(R.style.Theme_MinimalLauncher)
            PreferencesManager.THEME_WALLPAPER -> setTheme(R.style.Theme_MinimalLauncher_Transparent)
            PreferencesManager.THEME_SYSTEM -> {
                val nightMode = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
                val isNight = nightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES
                if (isNight) setTheme(R.style.Theme_MinimalLauncher) else setTheme(R.style.Theme_MinimalLauncher_Transparent)
            }
            else -> setTheme(R.style.Theme_MinimalLauncher_Transparent)
        }
    }

    @Suppress("DEPRECATION")
    private fun setupImmersiveMode() {
        if (prefs.showStatusBar()) return
        // Use FLAG_LAYOUT_NO_LIMITS like Olauncher for proper immersive mode
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        } else {
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        }
    }

    private fun setupBackPressed() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { /* Home screen — do nothing */ }
        })
    }
}
