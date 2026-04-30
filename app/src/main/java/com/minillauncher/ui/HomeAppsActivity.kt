package com.minillauncher.ui

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
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

/**
 * Activity to manage which apps are pinned to the home screen.
 * Users can add/remove apps from the home screen.
 */
class HomeAppsActivity : AppCompatActivity() {

    private lateinit var prefs: PreferencesManager
    private val allApps = mutableListOf<AppInfo>()
    private lateinit var adapter: HomeAppsListAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = PreferencesManager(this)

        val rootView = buildUI()
        setContentView(rootView)
        loadApps()
    }

    private fun buildUI(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(getColor(R.color.settings_background))

            // Toolbar
            addView(LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(dp(8), dp(16), dp(16), dp(16))
                gravity = android.view.Gravity.CENTER_VERTICAL

                val backBtn = ImageView(context).apply {
                    setImageResource(android.R.drawable.ic_menu_revert)
                    setColorFilter(getColor(R.color.text_primary))
                    setPadding(dp(8), dp(8), dp(8), dp(8))
                    isClickable = true
                    setOnClickListener { finish() }
                }
                addView(backBtn)

                addView(TextView(context).apply {
                    text = "App in home"
                    textSize = 20f
                    setTextColor(getColor(R.color.text_primary))
                    setPadding(dp(12), 0, 0, 0)
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                })
            })

            // Description
            addView(TextView(context).apply {
                text = "Attiva il toggle per fissare le app sulla schermata principale. Le app fissate appariranno in cima, nell'ordine scelto."
                textSize = 13f
                setTextColor(getColor(R.color.text_secondary))
                setPadding(dp(24), 0, dp(24), dp(16))
            })

            // Instructions
            addView(TextView(context).apply {
                text = "Nessuna app fissata = le prime ${prefs.getHomeAppCount()} app (in ordine alfabetico) verranno mostrate automaticamente."
                textSize = 12f
                setTextColor(getColor(R.color.text_hint))
                setPadding(dp(24), 0, dp(24), dp(8))
            })

            // RecyclerView
            recyclerView = RecyclerView(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
                layoutManager = LinearLayoutManager(context)
            }
            addView(recyclerView)
        }
    }

    private fun loadApps() {
        lifecycleScope.launch {
            val apps = withContext(Dispatchers.IO) {
                AppUtils.loadLaunchableApps(this@HomeAppsActivity)
            }
            allApps.clear()
            allApps.addAll(apps)

            adapter = HomeAppsListAdapter(allApps, prefs) { packageName, added ->
                Toast.makeText(
                    this@HomeAppsActivity,
                    if (added) "Aggiunta a home" else "Rimossa da home",
                    Toast.LENGTH_SHORT
                ).show()
            }
            recyclerView.adapter = adapter
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}

/**
 * Adapter for the home apps management list.
 */
class HomeAppsListAdapter(
    private val apps: List<AppInfo>,
    private val prefs: PreferencesManager,
    private val onToggle: (String, Boolean) -> Unit
) : RecyclerView.Adapter<HomeAppsListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
        val container = LinearLayout(parent.context).apply {
            orientation = LinearLayout.HORIZONTAL
            val pad = dp(parent.context, 16)
            setPadding(pad, dp(parent.context, 10), pad, dp(parent.context, 10))
            gravity = android.view.Gravity.CENTER_VERTICAL
            setBackgroundColor(parent.context.getColor(R.color.settings_item_background))

            val icon = ImageView(parent.context).apply {
                id = View.generateViewId()
                scaleType = ImageView.ScaleType.FIT_CENTER
                layoutParams = LinearLayout.LayoutParams(dp(parent.context, 36), dp(parent.context, 36))
            }
            addView(icon)

            val label = TextView(parent.context).apply {
                id = View.generateViewId()
                textSize = 15f
                setTextColor(parent.context.getColor(R.color.text_primary))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                    marginStart = dp(parent.context, 16)
                }
            }
            addView(label)

            val toggle = SwitchCompat(parent.context).apply {
                id = View.generateViewId()
            }
            addView(toggle)
        }
        return ViewHolder(container)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(apps[position])
    }

    override fun getItemCount(): Int = apps.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val icon: ImageView = (itemView as LinearLayout).getChildAt(0) as ImageView
        private val label: TextView = (itemView as LinearLayout).getChildAt(1) as TextView
        private val toggle: SwitchCompat = (itemView as LinearLayout).getChildAt(2) as SwitchCompat

        fun bind(app: AppInfo) {
            icon.setImageDrawable(app.icon)
            label.text = app.label
            toggle.isChecked = prefs.isAppPinnedToHome(app.packageName)

            toggle.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    prefs.addAppToHome(app.packageName)
                } else {
                    prefs.removeAppFromHome(app.packageName)
                }
                onToggle(app.packageName, isChecked)
            }
        }
    }

    private fun dp(context: android.content.Context, value: Int): Int {
        return (value * context.resources.displayMetrics.density).toInt()
    }
}
