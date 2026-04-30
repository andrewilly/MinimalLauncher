package com.minillauncher.ui

import android.content.Intent
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
 * Activity to manage hidden apps.
 */
class HiddenAppsActivity : AppCompatActivity() {

    private lateinit var prefs: PreferencesManager
    private val allApps = mutableListOf<AppInfo>()
    private lateinit var adapter: HiddenAppsAdapter
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
                    text = getString(R.string.hidden_apps)
                    textSize = 20f
                    setTextColor(getColor(R.color.text_primary))
                    setPadding(dp(12), 0, 0, 0)
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                })
            })

            // Description
            addView(TextView(context).apply {
                text = getString(R.string.hidden_apps_description)
                textSize = 14f
                setTextColor(getColor(R.color.text_secondary))
                setPadding(dp(24), 0, dp(24), dp(16))
            })

            // RecyclerView
            recyclerView = RecyclerView(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
                layoutManager = LinearLayoutManager(context)
                adapter = HiddenAppsAdapter(allApps, prefs, this@HiddenAppsActivity) { packageName ->
                    Toast.makeText(
                        this@HiddenAppsActivity,
                        if (prefs.isAppHidden(packageName)) "App nascosta" else "App mostrata",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            addView(recyclerView)
            adapter = recyclerView.adapter as HiddenAppsAdapter
        }
    }

    private fun loadApps() {
        lifecycleScope.launch {
            val apps = withContext(Dispatchers.IO) {
                AppUtils.loadLaunchableApps(this@HiddenAppsActivity)
            }
            allApps.clear()
            allApps.addAll(apps)
            adapter.updateApps(allApps)
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}

/**
 * Adapter for the hidden apps list.
 */
class HiddenAppsAdapter(
    private val apps: MutableList<AppInfo>,
    private val prefs: PreferencesManager,
    private val context: HiddenAppsActivity,
    private val onToggle: (String) -> Unit
) : RecyclerView.Adapter<HiddenAppsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
        val container = LinearLayout(parent.context).apply {
            orientation = LinearLayout.HORIZONTAL
            val pad = dp(parent.context, 24)
            setPadding(pad, dp(parent.context, 12), pad, dp(parent.context, 12))
            gravity = android.view.Gravity.CENTER_VERTICAL
            setBackgroundColor(parent.context.getColor(R.color.settings_item_background))

            val icon = ImageView(parent.context).apply {
                id = View.generateViewId()
                scaleType = ImageView.ScaleType.FIT_CENTER
                layoutParams = LinearLayout.LayoutParams(dp(parent.context, 40), dp(parent.context, 40))
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

    fun updateApps(newApps: List<AppInfo>) {
        apps.clear()
        apps.addAll(newApps)
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val icon: ImageView = itemView as? ImageView ?: (itemView as LinearLayout).getChildAt(0) as ImageView
        private val label: TextView = (itemView as LinearLayout).getChildAt(1) as TextView
        private val toggle: SwitchCompat = (itemView as LinearLayout).getChildAt(2) as SwitchCompat

        fun bind(app: AppInfo) {
            icon.setImageDrawable(app.icon)
            label.text = app.label
            toggle.isChecked = prefs.isAppHidden(app.packageName)

            toggle.setOnCheckedChangeListener { _, isChecked ->
                prefs.toggleAppHidden(app.packageName)
                onToggle(app.packageName)
            }
        }
    }

    private fun dp(context: android.content.Context, value: Int): Int {
        return (value * context.resources.displayMetrics.density).toInt()
    }
}
