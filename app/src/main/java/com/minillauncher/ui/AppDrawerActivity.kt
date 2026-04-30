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

class AppDrawerActivity : AppCompatActivity() {

    private lateinit var prefs: PreferencesManager
    private lateinit var recyclerApps: RecyclerView
    private lateinit var editSearch: EditText

    private val allApps = mutableListOf<AppInfo>()
    private val filteredApps = mutableListOf<AppInfo>()
    private lateinit var adapter: DrawerAppAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = PreferencesManager(this)
        setContentView(R.layout.activity_app_drawer)

        recyclerApps = findViewById(R.id.recycler_drawer_apps)
        editSearch = findViewById(R.id.edit_search)

        recyclerApps.layoutManager = LinearLayoutManager(this)
        adapter = DrawerAppAdapter(
            apps = filteredApps,
            showAlphabetHeaders = true,
            onAppClick = { app ->
                AppUtils.launchApp(this, app)
                finishAfterTransition()
            },
            onAppLongClick = { app, view -> showAppMenu(app, view) }
        )
        recyclerApps.adapter = adapter

        // Search filtering
        editSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) { filterApps(editSearch.text.toString()); true }
            else false
        }
        editSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { filterApps(s.toString()) }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        loadApps()
    }

    private fun loadApps() {
        lifecycleScope.launch {
            val apps = withContext(Dispatchers.IO) { AppUtils.loadLaunchableApps(this@AppDrawerActivity) }
            val hidden = prefs.getHiddenApps()
            allApps.clear()
            allApps.addAll(apps.filter { !hidden.contains(it.packageName) })
            filteredApps.clear()
            filteredApps.addAll(allApps)
            adapter.updateApps(filteredApps)
        }
    }

    private fun filterApps(query: String) {
        val q = query.trim().lowercase(Locale.getDefault())
        filteredApps.clear()
        filteredApps.addAll(if (q.isEmpty()) allApps else allApps.filter {
            it.label.toString().lowercase(Locale.getDefault()).contains(q)
        })
        adapter.updateApps(filteredApps)
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

    override fun onBackPressed() { finishAfterTransition() }
}
