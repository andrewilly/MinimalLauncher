package com.minillauncher.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.minillauncher.R
import com.minillauncher.utils.AppInfo
import com.minillauncher.utils.capitalizeFirstLetter

/**
 * Home screen adapter: app names only, no icons. Olauncher style.
 */
class HomeAppAdapter(
    private val apps: MutableList<AppInfo>,
    private val onAppClick: (AppInfo) -> Unit,
    private val onAppLongClick: ((AppInfo, View) -> Unit)? = null
) : RecyclerView.Adapter<HomeAppAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false))
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
        private val textName: TextView = itemView.findViewById(R.id.text_app_name)

        fun bind(app: AppInfo) {
            textName.text = app.label.capitalizeFirstLetter()
            itemView.setOnClickListener { onAppClick(app) }
            if (onAppLongClick != null) {
                itemView.setOnLongClickListener {
                    itemView.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
                    onAppLongClick.invoke(app, itemView)
                    true
                }
            }
        }
    }
}
