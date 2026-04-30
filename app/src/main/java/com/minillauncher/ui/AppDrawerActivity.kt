package com.minillauncher.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.minillauncher.R
import com.minillauncher.utils.*
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
            showAlphabetHeaders = prefs.showAlphabetHeaders(),
            onAppClick = { app ->
                AppUtils.launchApp(this, app)
                finishAfterTransition()
            },
            onAppLongClick = { app, view -> showAppMenu(app, view) }
        )
        recyclerApps.adapter = adapter

        // Auto-show keyboard
        if (prefs.autoShowKeyboard()) {
            editSearch.postDelayed({
                editSearch.requestFocus()
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(editSearch, InputMethodManager.SHOW_IMPLICIT)
            }, 300)
        }

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

        // FIX: Use OnBackPressedDispatcher instead of deprecated onBackPressed
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { finishAfterTransition() }
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
        val isPinned = prefs.isAppPinnedToHome(app.packageName)
        popup.menu.add(0, 100, 0,
            if (isPinned) getString(R.string.menu_remove_from_home) else getString(R.string.menu_add_to_home)
        )

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_app_info -> { AppUtils.openAppSettings(this, app.packageName); true }
                R.id.menu_hide_app -> {
                    prefs.toggleAppHidden(app.packageName)
                    loadApps()
                    Toast.makeText(this, if (prefs.isAppHidden(app.packageName)) "App nascosta" else "App mostrata", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.menu_uninstall -> {
                    startActivity(Intent(Intent.ACTION_DELETE,
                        android.net.Uri.parse("package:${app.packageName}")).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        })
                    true
                }
                100 -> {
                    prefs.toggleAppOnHome(app.packageName)
                    Toast.makeText(this,
                        if (prefs.isAppPinnedToHome(app.packageName)) "Aggiunta a home" else "Rimossa da home",
                        Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
        popup.show()
    }
}
