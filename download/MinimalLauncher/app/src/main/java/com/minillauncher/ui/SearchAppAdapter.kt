package com.minillauncher.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.minillauncher.utils.AppInfo

/**
 * Adapter for search results displayed in the home screen search overlay.
 * Simple list layout matching the drawer style.
 */
class SearchAppAdapter(
    private val apps: MutableList<AppInfo>,
    private val showLabels: Boolean = true,
    private val iconScale: Float = 1.0f,
    private val onAppClick: (AppInfo) -> Unit,
    private val onAppLongClick: ((AppInfo, View) -> Unit)? = null
) : RecyclerView.Adapter<SearchAppAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app_drawer, parent, false)
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
        private val container: LinearLayout = itemView.findViewById(R.id.app_item_container)
        private val icon: ImageView = itemView.findViewById(R.id.image_app_icon)
        private val label: TextView = itemView.findViewById(R.id.text_app_name)

        fun bind(app: AppInfo) {
            icon.setImageDrawable(app.icon)
            label.text = app.label
            label.visibility = if (showLabels) View.VISIBLE else View.GONE

            container.setOnClickListener { onAppClick(app) }

            if (onAppLongClick != null) {
                container.setOnLongClickListener {
                    onAppLongClick.invoke(app, container)
                    true
                }
            }
        }
    }
}
