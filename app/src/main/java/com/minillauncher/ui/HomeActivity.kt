package com.minillauncher.ui

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
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
import com.minillauncher.utils.AppInfo
import com.minillauncher.utils.AppUtils
import com.minillauncher.utils.ClockUtils
import com.minillauncher.utils.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeActivity : AppCompatActivity() {

    private lateinit var prefs: PreferencesManager
    private lateinit var rootContainer: FrameLayout
    private lateinit var textClock: TextView
    private lateinit var textDate: TextView
    private lateinit var recyclerHomeApps: RecyclerView
    private lateinit var settingsTouchArea: View
    private lateinit var mainContent: LinearLayout

    private val homeApps = mutableListOf<AppInfo>()
    private val allApps = mutableListOf<AppInfo>()
    private var homeAdapter: HomeAppAdapter? = null
    private var clockHandler: Handler? = null
    private var clockRunnable: Runnable? = null
    private var gestureDetector: GestureDetectorCompat? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = PreferencesManager(this)
        applyTheme()
        setContentView(R.layout.activity_home)
        setupImmersiveMode()
        initViews()
        setupGestures()
        setupBackPressed()
        loadApps()
        startClockUpdates()
    }

    override fun onNewIntent(intent: Intent) { super.onNewIntent(intent) }
    override fun onResume() { super.onResume(); setupImmersiveMode(); loadApps() }
    override fun onDestroy() { super.onDestroy(); stopClockUpdates() }

    private fun initViews() {
        rootContainer = findViewById(R.id.root_container)
        mainContent = findViewById(R.id.main_content)
        textClock = findViewById(R.id.text_clock)
        textDate = findViewById(R.id.text_date)
        recyclerHomeApps = findViewById(R.id.recycler_home_apps)
        settingsTouchArea = findViewById(R.id.settings_touch_area)

        // Vertical list of app names (Olauncher style)
        recyclerHomeApps.layoutManager = LinearLayoutManager(this)
        homeAdapter = HomeAppAdapter(
            apps = homeApps,
            onAppClick = { app -> AppUtils.launchApp(this, app) },
            onAppLongClick = { app, view -> showAppMenu(app, view) }
        )
        recyclerHomeApps.adapter = homeAdapter

        // Long press bottom center = settings
        settingsTouchArea.setOnLongClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
            true
        }

        textClock.visibility = if (prefs.showClock()) View.VISIBLE else View.GONE
        textDate.visibility = if (prefs.showDate()) View.VISIBLE else View.GONE
    }

    // ==================== GESTURES ====================

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
                    if (dy < 0) handleSwipeUp() else handleSwipeDown()
                    return true
                }
                return false
            }
        })

        // Swipe on the clock/date area (above the list)
        mainContent.setOnTouchListener { _, event ->
            gestureDetector?.onTouchEvent(event) ?: false
        }
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
            PreferencesManager.ACTION_OPEN_SEARCH -> openAppDrawer()
            PreferencesManager.ACTION_OPEN_APP_DRAWER -> openAppDrawer()
        }
    }

    private fun handleSwipeDown() {
        when (prefs.getSwipeDownAction()) {
            PreferencesManager.ACTION_SHOW_NOTIFICATIONS -> expandNotifications()
        }
    }

    // ==================== ACTIONS ====================

    private fun openAppDrawer() {
        startActivity(Intent(this, AppDrawerActivity::class.java))
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right)
    }

    @Suppress("DEPRECATION")
    private fun lockScreen() {
        try { (getSystemService(Context.DEVICE_POLICY_SERVICE) as android.app.admin.DevicePolicyManager).lockNow() } catch (_: Exception) {}
    }

    @Suppress("DEPRECATION")
    private fun expandNotifications() {
        try {
            val sb = getSystemService("statusbar")
            if (sb != null) {
                val n = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) "expandNotifications" else "expandNotificationsPanel"
                for (m in sb.javaClass.methods) { if (m.name == n) { m.invoke(sb); return } }
            }
        } catch (_: Exception) {}
    }

    // ==================== APPS ====================

    private fun loadApps() {
        lifecycleScope.launch {
            val apps = withContext(Dispatchers.IO) { AppUtils.loadLaunchableApps(this@HomeActivity) }
            allApps.clear(); allApps.addAll(apps)
            val hidden = prefs.getHiddenApps()
            homeApps.clear()
            homeApps.addAll(apps.filter { !hidden.contains(it.packageName) })
            homeAdapter?.updateApps(homeApps)
        }
    }

    private fun showAppMenu(app: AppInfo, anchorView: View) {
        val popup = android.widget.PopupMenu(this, anchorView)
        popup.menuInflater.inflate(R.menu.menu_app_long_press, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_app_info -> { AppUtils.openAppSettings(this, app.packageName); true }
                R.id.menu_hide_app -> { prefs.toggleAppHidden(app.packageName); loadApps(); true }
                R.id.menu_uninstall -> {
                    startActivity(Intent(Intent.ACTION_DELETE, android.net.Uri.parse("package:${app.packageName}")).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    // ==================== CLOCK ====================

    private fun startClockUpdates() {
        clockHandler = Handler(Looper.getMainLooper())
        updateClock()
        clockRunnable = object : Runnable { override fun run() { updateClock(); clockHandler?.postDelayed(this, 1000) } }
        clockHandler?.post(clockRunnable!!)
    }

    private fun stopClockUpdates() { clockRunnable?.let { clockHandler?.removeCallbacks(it) } }
    private fun updateClock() { textClock.text = ClockUtils.getTime(this); textDate.text = ClockUtils.getDate(this) }

    // ==================== THEME ====================

    private fun applyTheme() {
        when (prefs.getTheme()) {
            PreferencesManager.THEME_LIGHT -> setTheme(R.style.Theme_MinimalLauncher_Drawer)
            PreferencesManager.THEME_DARK -> setTheme(R.style.Theme_MinimalLauncher)
            PreferencesManager.THEME_WALLPAPER -> setTheme(R.style.Theme_MinimalLauncher_Transparent)
            else -> setTheme(R.style.Theme_MinimalLauncher)
        }
    }

    @Suppress("DEPRECATION")
    private fun setupImmersiveMode() {
        if (prefs.showStatusBar()) return
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) window.setDecorFitsSystemWindows(false)
        else window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
    }

    private fun setupBackPressed() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {}
        })
    }
}
