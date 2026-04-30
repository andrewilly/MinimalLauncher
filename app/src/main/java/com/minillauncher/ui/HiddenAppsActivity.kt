package com.minillauncher.ui

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
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

/**
 * Activity to manage hidden apps.
 * Shows a list of all apps with toggle to hide/unhide each.
 */
class HiddenAppsActivity : AppCompatActivity() {

    private lateinit var prefs: PreferencesManager
    private val allApps = mutableListOf<AppInfo>()
    private lateinit var adapter: HiddenAppsAdapter

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

            // Toolbar area
            addView(LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(8.dp, 16.dp, 16.dp, 16.dp)
                gravity = android.view.Gravity.CENTER_VERTICAL

                val backButton = ImageView(context).apply {
                    setImageResource(android.R.drawable.ic_menu_revert)
                    setColorFilter(getColor(R.color.text_primary))
                    setPadding(8.dp, 8.dp, 8.dp, 8.dp)
                    isClickable = true
                    setOnClickListener { finish() }
                }
                addView(backButton)

                addView(TextView(context).apply {
                    text = getString(R.string.hidden_apps)
                    textSize = 20f
                    setTextColor(getColor(R.color.text_primary))
                    setPadding(12.dp, 0, 0, 0)
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                })
            })

            // Description
            addView(TextView(context).apply {
                text = getString(R.string.hidden_apps_description)
                textSize = 14f
                setTextColor(getColor(R.color.text_secondary))
                setPadding(24.dp, 0, 24.dp, 16.dp)
            })

            // RecyclerView
            val recyclerView = RecyclerView(context).apply {
                layoutManager = LinearLayoutManager(context)
                adapter = HiddenAppsAdapter(allApps, prefs) { packageName ->
                    Toast.makeText(
                        this@HiddenAppsActivity,
                        if (prefs.isAppHidden(packageName)) "App nascosta" else "App mostrata",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                id = View.generateViewId()
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

    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).toInt()
}

/**
 * Simple adapter for the hidden apps list.
 */
class HiddenAppsAdapter(
    private val apps: MutableList<AppInfo>,
    private val prefs: PreferencesManager,
    private val onToggle: (String) -> Unit
) : RecyclerView.Adapter<HiddenAppsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
        val view = LinearLayout(parent.context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(24.dp, 12.dp, 24.dp, 12.dp)
            gravity = android.view.Gravity.CENTER_VERTICAL
            setBackgroundColor(parent.context.getColor(R.color.settings_item_background))

            addView(ImageView(context).apply {
                id = View.generateViewId()
                scaleType = ImageView.ScaleType.FIT_CENTER
                layoutParams = LinearLayout.LayoutParams(40.dp, 40.dp)
            })

            addView(TextView(context).apply {
                id = View.generateViewId()
                textSize = 15f
                setTextColor(context.getColor(R.color.text_primary))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                    marginStart = 16.dp
                }
            })

            addView(android.widget.SwitchCompat(context).apply {
                id = View.generateViewId()
            })
        }
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = apps[position]
        holder.bind(app)
    }

    override fun getItemCount(): Int = apps.size

    fun updateApps(newApps: List<AppInfo>) {
        apps.clear()
        apps.addAll(newApps)
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val icon: ImageView = itemView.findViewById(itemView.findViewById<View>(android.R.id.icon)?.id ?: 1)
        private val label: TextView
        private val toggle: android.widget.SwitchCompat

        init {
            val children = (itemView as LinearLayout)
            icon = children.getChildAt(0) as ImageView
            label = children.getChildAt(1) as TextView
            toggle = children.getChildAt(2) as android.widget.SwitchCompat
        }

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

    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).toInt()
}
