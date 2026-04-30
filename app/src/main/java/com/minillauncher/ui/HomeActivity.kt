package com.minillauncher.ui

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
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
import java.util.Locale

/**
 * Main home screen activity.
 * Acts as the launcher home with clock, date, and home apps grid.
 */
class HomeActivity : AppCompatActivity() {

    private lateinit var prefs: PreferencesManager
    private lateinit var rootContainer: FrameLayout
    private lateinit var mainContent: LinearLayout
    private lateinit var textClock: TextView
    private lateinit var textDate: TextView
    private lateinit var recyclerHomeApps: RecyclerView
    private lateinit var searchContainer: FrameLayout
    private lateinit var editSearch: EditText
    private lateinit var recyclerSearchResults: RecyclerView
    private lateinit var settingsTouchArea: View
    private lateinit var widgetArea: FrameLayout

    private val homeApps = mutableListOf<AppInfo>()
    private val allApps = mutableListOf<AppInfo>()
    private var homeAdapter: HomeAppAdapter? = null
    private var searchAdapter: SearchAppAdapter? = null
    private var clockHandler: Handler? = null
    private var clockRunnable: Runnable? = null

    // Double tap detection
    private var lastTapTime = 0L
    private val doubleTapTimeout = 300L

    // Swipe detection
    private val swipeThreshold = 150
    private var touchStartX = 0f
    private var touchStartY = 0f

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

        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        if (intent.action == Intent.ACTION_MAIN) {
            searchContainer.visibility = View.GONE
            editSearch.text.clear()
            editSearch.clearFocus()
        }
    }

    override fun onResume() {
        super.onResume()
        setupImmersiveMode()
        loadApps()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopClockUpdates()
    }

    // ==================== INITIALIZATION ====================

    private fun initViews() {
        rootContainer = findViewById(R.id.root_container)
        mainContent = findViewById(R.id.main_content)
        textClock = findViewById(R.id.text_clock)
        textDate = findViewById(R.id.text_date)
        recyclerHomeApps = findViewById(R.id.recycler_home_apps)
        searchContainer = findViewById(R.id.search_container)
        editSearch = findViewById(R.id.edit_search)
        recyclerSearchResults = findViewById(R.id.recycler_search_results)
        settingsTouchArea = findViewById(R.id.settings_touch_area)
        widgetArea = findViewById(R.id.widget_area)

        // Setup home apps grid
        val spanCount = calculateSpanCount()
        recyclerHomeApps.layoutManager = GridLayoutManager(this, spanCount)
        homeAdapter = HomeAppAdapter(
            apps = homeApps,
            showLabels = prefs.showIconLabels(),
            iconScale = prefs.getIconSizeMultiplier(),
            onAppClick = { app -> AppUtils.launchApp(this, app) },
            onAppLongClick = { app, view -> showAppMenu(app, view) }
        )
        recyclerHomeApps.adapter = homeAdapter

        // Setup search
        editSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                filterApps(editSearch.text.toString())
                true
            } else {
                false
            }
        }
        editSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterApps(s.toString())
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        recyclerSearchResults.layoutManager = LinearLayoutManager(this)
        searchAdapter = SearchAppAdapter(
            apps = mutableListOf(),
            showLabels = true,
            iconScale = 1.0f,
            onAppClick = { app ->
                searchContainer.visibility = View.GONE
                editSearch.text.clear()
                AppUtils.launchApp(this, app)
            },
            onAppLongClick = null
        )
        recyclerSearchResults.adapter = searchAdapter

        // Settings touch area — long press to open settings
        settingsTouchArea.setOnLongClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
            true
        }

        // Widget area visibility
        widgetArea.visibility = if (prefs.showWidgetArea()) View.VISIBLE else View.GONE

        // Clock & date visibility
        textClock.visibility = if (prefs.showClock()) View.VISIBLE else View.GONE
        textDate.visibility = if (prefs.showDate()) View.VISIBLE else View.GONE
    }

    private fun calculateSpanCount(): Int {
        val displayMetrics = resources.displayMetrics
        val dpWidth = displayMetrics.widthPixels / displayMetrics.density
        return Math.max(4, Math.floor(dpWidth / 90.0).toInt())
    }

    // ==================== GESTURES ====================

    private fun setupGestures() {
        rootContainer.setOnTouchListener { _, event ->
            handleGesture(event)
            false
        }

        rootContainer.setOnClickListener {
            if (searchContainer.visibility == View.VISIBLE) {
                searchContainer.visibility = View.GONE
                editSearch.text.clear()
            }
        }
    }

    private fun handleGesture(event: MotionEvent) {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchStartX = event.rawX
                touchStartY = event.rawY

                val now = System.currentTimeMillis()
                if (now - lastTapTime < doubleTapTimeout) {
                    handleDoubleTap()
                }
                lastTapTime = now
            }
            MotionEvent.ACTION_UP -> {
                val deltaX = event.rawX - touchStartX
                val deltaY = event.rawY - touchStartY

                if (searchContainer.visibility != View.VISIBLE) {
                    if (Math.abs(deltaX) > Math.abs(deltaY)) {
                        if (Math.abs(deltaX) > swipeThreshold) {
                            if (deltaX > 0) handleSwipeRight() else handleSwipeLeft()
                        }
                    } else {
                        if (Math.abs(deltaY) > swipeThreshold) {
                            if (deltaY < 0) handleSwipeUp() else handleSwipeDown()
                        }
                    }
                }
            }
        }
    }

    private fun handleDoubleTap() {
        when (prefs.getDoubleTapAction()) {
            PreferencesManager.ACTION_LOCK_SCREEN -> lockScreen()
            PreferencesManager.ACTION_OPEN_APP_DRAWER -> openAppDrawer()
            PreferencesManager.ACTION_OPEN_SEARCH -> openSearch()
        }
    }

    private fun handleSwipeLeft() {
        when (prefs.getSwipeLeftAction()) {
            PreferencesManager.ACTION_OPEN_APP_DRAWER -> openAppDrawer()
            PreferencesManager.ACTION_SHOW_NOTIFICATIONS -> expandNotificationShade()
            PreferencesManager.ACTION_OPEN_SEARCH -> openSearch()
        }
    }

    private fun handleSwipeRight() {
        when (prefs.getSwipeRightAction()) {
            PreferencesManager.ACTION_OPEN_APP_DRAWER -> openAppDrawer()
            PreferencesManager.ACTION_SHOW_NOTIFICATIONS -> expandNotificationShade()
            PreferencesManager.ACTION_OPEN_SEARCH -> openSearch()
        }
    }

    private fun handleSwipeUp() {
        when (prefs.getSwipeUpAction()) {
            PreferencesManager.ACTION_OPEN_SEARCH -> openSearch()
            PreferencesManager.ACTION_OPEN_APP_DRAWER -> openAppDrawer()
        }
    }

    private fun handleSwipeDown() {
        when (prefs.getSwipeDownAction()) {
            PreferencesManager.ACTION_SHOW_NOTIFICATIONS -> expandNotificationShade()
        }
    }

    // ==================== ACTIONS ====================

    private fun openAppDrawer() {
        val intent = Intent(this, AppDrawerActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right)
    }

    private fun openSearch() {
        searchContainer.visibility = View.VISIBLE
        editSearch.requestFocus()
        if (prefs.autoShowKeyboard()) {
            editSearch.postDelayed({
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                imm.showSoftInput(editSearch, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
            }, 100)
        }
    }

    @Suppress("DEPRECATION")
    private fun lockScreen() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val policyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as android.app.admin.DevicePolicyManager
                policyManager.lockNow()
            }
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.action_lock_screen), Toast.LENGTH_SHORT).show()
        }
    }

    @Suppress("DEPRECATION")
    private fun expandNotificationShade() {
        try {
            val statusBarService = getSystemService("statusbar")
            if (statusBarService != null) {
                val methodName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    "expandNotifications"
                } else {
                    "expandNotificationsPanel"
                }
                for (method in statusBarService.javaClass.methods) {
                    if (method.name == methodName) {
                        method.invoke(statusBarService)
                        return
                    }
                }
            }
        } catch (e: Exception) {
            // Silently fail
        }
    }

    // ==================== APPS ====================

    private fun loadApps() {
        lifecycleScope.launch {
            val apps = withContext(Dispatchers.IO) {
                AppUtils.loadLaunchableApps(this@HomeActivity)
            }

            val hiddenApps = prefs.getHiddenApps()
            allApps.clear()
            allApps.addAll(apps)

            homeApps.clear()
            homeApps.addAll(apps.filter { !hiddenApps.contains(it.packageName) })

            homeAdapter?.updateApps(homeApps)
        }
    }

    private fun filterApps(query: String) {
        val trimmed = query.trim().lowercase(Locale.getDefault())
        val filtered = if (trimmed.isEmpty()) {
            emptyList()
        } else {
            allApps.filter {
                it.label.toString().lowercase(Locale.getDefault()).contains(trimmed) ||
                it.packageName.lowercase(Locale.getDefault()).contains(trimmed)
            }
        }
        searchAdapter?.updateApps(filtered)
    }

    // ==================== APP MENU (Long Press) ====================

    private fun showAppMenu(app: AppInfo, anchorView: View) {
        val popup = android.widget.PopupMenu(this, anchorView)
        popup.menuInflater.inflate(R.menu.menu_app_long_press, popup.menu)

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_app_info -> {
                    AppUtils.openAppSettings(this, app.packageName)
                    true
                }
                R.id.menu_hide_app -> {
                    prefs.toggleAppHidden(app.packageName)
                    loadApps()
                    Toast.makeText(this, R.string.menu_hide_app, Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.menu_uninstall -> {
                    try {
                        val uninstallIntent = Intent(Intent.ACTION_DELETE).apply {
                            data = android.net.Uri.parse("package:${app.packageName}")
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        startActivity(uninstallIntent)
                    } catch (e: Exception) {
                        Toast.makeText(this, "Impossibile disinstallare", Toast.LENGTH_SHORT).show()
                    }
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
        clockRunnable = object : Runnable {
            override fun run() {
                updateClock()
                clockHandler?.postDelayed(this, 1000)
            }
        }
        clockHandler?.post(clockRunnable!!)
    }

    private fun stopClockUpdates() {
        clockRunnable?.let { clockHandler?.removeCallbacks(it) }
    }

    private fun updateClock() {
        textClock.text = ClockUtils.getTime(this)
        textDate.text = ClockUtils.getDate(this)
    }

    // ==================== UI SETUP ====================

    private fun applyTheme() {
        when (prefs.getTheme()) {
            PreferencesManager.THEME_LIGHT -> setTheme(android.R.style.Theme_Material_Light_NoActionBar)
            PreferencesManager.THEME_DARK -> setTheme(android.R.style.Theme_Material_NoActionBar)
            else -> setTheme(R.style.Theme_MinimalLauncher)
        }
    }

    private fun setupImmersiveMode() {
        if (prefs.showStatusBar()) return

        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                )
        }
    }

    private fun setupBackPressed() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (searchContainer.visibility == View.VISIBLE) {
                    searchContainer.visibility = View.GONE
                    editSearch.text.clear()
                }
            }
        })
    }
}
