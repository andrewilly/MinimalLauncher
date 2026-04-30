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
import com.minillauncher.utils.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Manage which apps are pinned to the home screen.
 */
class HomeAppsActivity : AppCompatActivity() {

    private lateinit var prefs: PreferencesManager
    private val allApps = mutableListOf<AppInfo>()
    private lateinit var adapter: HomeAppsListAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = PreferencesManager(this)
        setContentView(buildUI())
        loadApps()
    }

    private fun buildUI(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(getColor(R.color.settings_background))

            addView(LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(dp(8), dp(16), dp(16), dp(16))
                gravity = android.view.Gravity.CENTER_VERTICAL

                addView(ImageView(context).apply {
                    setImageResource(android.R.drawable.ic_menu_revert)
                    setColorFilter(getColor(R.color.settings_back_icon))
                    setPadding(dp(8), dp(8), dp(8), dp(8))
                    isClickable = true; setOnClickListener { finish() }
                })
                addView(TextView(context).apply {
                    text = "App in home"; textSize = 20f
                    setTextColor(getColor(R.color.text_primary_light))
                    setPadding(dp(12), 0, 0, 0)
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                })
            })

            addView(TextView(context).apply {
                text = "Attiva il toggle per fissare le app sulla schermata principale. Le app fissate appariranno in cima, nell'ordine scelto."
                textSize = 13f; setTextColor(getColor(R.color.text_secondary_light))
                setPadding(dp(24), 0, dp(24), dp(8))
            })

            val count = prefs.getHomeAppCount()
            addView(TextView(context).apply {
                text = if (count == PreferencesManager.HOME_APP_COUNT_ALL)
                    "Nessuna app fissata = tutte le app visibili."
                else "Nessuna app fissata = le prime $count app in ordine alfabetico."
                textSize = 12f; setTextColor(getColor(R.color.text_hint_light))
                setPadding(dp(24), 0, dp(24), dp(16))
            })

            recyclerView = RecyclerView(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
                layoutManager = LinearLayoutManager(context)
            }
            addView(recyclerView)
        }
    }

    private fun loadApps() {
        lifecycleScope.launch {
            val apps = withContext(Dispatchers.IO) { AppUtils.loadLaunchableApps(this@HomeAppsActivity) }
            allApps.clear(); allApps.addAll(apps)
            adapter = HomeAppsListAdapter(allApps, prefs) { _, added ->
                Toast.makeText(this@HomeAppsActivity,
                    if (added) "Aggiunta a home" else "Rimossa da home", Toast.LENGTH_SHORT).show()
            }
            recyclerView.adapter = adapter
        }
    }
}

class HomeAppsListAdapter(
    private val apps: List<AppInfo>,
    private val prefs: PreferencesManager,
    private val onToggle: (String, Boolean) -> Unit
) : RecyclerView.Adapter<HomeAppsListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
        val ctx = parent.context
        val container = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp(ctx, 16), dp(ctx, 10), dp(ctx, 16), dp(ctx, 10))
            gravity = android.view.Gravity.CENTER_VERTICAL
            setBackgroundColor(ctx.getColor(R.color.settings_item_background))

            addView(ImageView(ctx).apply {
                id = View.generateViewId(); scaleType = ImageView.ScaleType.FIT_CENTER
                layoutParams = LinearLayout.LayoutParams(dp(ctx, 36), dp(ctx, 36))
            })
            addView(TextView(ctx).apply {
                id = View.generateViewId(); textSize = 15f
                setTextColor(ctx.getColor(R.color.text_primary_light))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                    marginStart = dp(ctx, 16)
                }
            })
            addView(SwitchCompat(ctx).apply { id = View.generateViewId() })
        }
        return ViewHolder(container)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(apps[position])
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
                if (isChecked) prefs.addAppToHome(app.packageName)
                else prefs.removeAppFromHome(app.packageName)
                onToggle(app.packageName, isChecked)
            }
        }
    }

    private fun dp(context: android.content.Context, value: Int): Int =
        (value * context.resources.displayMetrics.density).toInt()
}
