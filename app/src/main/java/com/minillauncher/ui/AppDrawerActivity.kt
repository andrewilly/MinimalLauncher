package com.minillauncher.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.minillauncher.R
import com.minillauncher.utils.AppInfo
import com.minillauncher.utils.AppUtils
import com.minillauncher.utils.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * Full app drawer showing all installed apps in an alphabetical list.
 */
class AppDrawerActivity : AppCompatActivity() {

    private lateinit var prefs: PreferencesManager
    private lateinit var recyclerApps: RecyclerView
    private lateinit var textDrawerTitle: TextView

    private val allApps = mutableListOf<AppInfo>()
    private val filteredApps = mutableListOf<AppInfo>()
    private lateinit var adapter: DrawerAppAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = PreferencesManager(this)
        setContentView(R.layout.activity_app_drawer)

        initViews()
        loadApps()
    }

    private fun initViews() {
        textDrawerTitle = findViewById(R.id.text_drawer_title)
        recyclerApps = findViewById(R.id.recycler_drawer_apps)

        recyclerApps.layoutManager = LinearLayoutManager(this)
        adapter = DrawerAppAdapter(
            apps = filteredApps,
            showLabels = prefs.showIconLabels(),
            iconScale = 1.0f,
            showAlphabetHeaders = prefs.showAlphabetHeaders(),
            onAppClick = { app ->
                AppUtils.launchApp(this, app)
                finishAfterTransition()
            },
            onAppLongClick = { app, view -> showAppMenu(app, view) }
        )
        recyclerApps.adapter = adapter
    }

    private fun loadApps() {
        lifecycleScope.launch {
            val apps = withContext(Dispatchers.IO) {
                AppUtils.loadLaunchableApps(this@AppDrawerActivity)
            }

            val hiddenApps = prefs.getHiddenApps()
            allApps.clear()
            allApps.addAll(apps.filter { !hiddenApps.contains(it.packageName) })

            filteredApps.clear()
            filteredApps.addAll(allApps)
            adapter.updateApps(filteredApps)
        }
    }

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
                    true
                }
                R.id.menu_uninstall -> {
                    try {
                        val intent = Intent(Intent.ACTION_DELETE).apply {
                            data = android.net.Uri.parse("package:${app.packageName}")
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        startActivity(intent)
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

    override fun onBackPressed() {
        finishAfterTransition()
    }
}
