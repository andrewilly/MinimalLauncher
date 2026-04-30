package com.minillauncher.ui

import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.minillauncher.utils.AppInfo
import com.minillauncher.utils.AppUtils
import com.minillauncher.utils.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * Full app drawer showing all installed apps in an alphabetical list.
 * Launched by swiping gesture from home screen.
 */
class AppDrawerActivity : AppCompatActivity() {

    private lateinit var prefs: PreferencesManager
    private lateinit var recyclerApps: RecyclerView
    private lateinit var textDrawerTitle: TextView
    private lateinit var editSearch: EditText
    private lateinit var drawerRoot: View

    private val allApps = mutableListOf<AppInfo>()
    private val filteredApps = mutableListOf<AppInfo>()
    private lateinit var adapter: DrawerAppAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_drawer)
        prefs = PreferencesManager(this)

        initViews()
        loadApps()
    }

    private fun initViews() {
        drawerRoot = findViewById(R.id.drawer_root)
        textDrawerTitle = findViewById(R.id.text_drawer_title)
        recyclerApps = findViewById(R.id.recycler_drawer_apps)
        editSearch = findViewById(R.id.edit_search)

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

        // Setup search filtering
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

    private fun filterApps(query: String) {
        val trimmed = query.trim().lowercase(Locale.getDefault())
        filteredApps.clear()

        if (trimmed.isEmpty()) {
            filteredApps.addAll(allApps)
        } else {
            filteredApps.addAll(allApps.filter {
                it.label.toString().lowercase(Locale.getDefault()).contains(trimmed) ||
                it.packageName.lowercase(Locale.getDefault()).contains(trimmed)
            })
        }

        adapter.updateApps(filteredApps)

        // Scroll to top
        if (filteredApps.isNotEmpty()) {
            recyclerApps.scrollToPosition(0)
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
                        val intent = android.content.Intent(android.content.Intent.ACTION_DELETE).apply {
                            data = android.net.Uri.parse("package:${app.packageName}")
                            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        startActivity(intent)
                    } catch (e: Exception) {
                        android.widget.Toast.makeText(this, "Impossibile disinstallare", android.widget.Toast.LENGTH_SHORT).show()
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
