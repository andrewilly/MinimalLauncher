package com.minillauncher.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.minillauncher.R
import com.minillauncher.utils.AppInfo
import java.util.Locale

/**
 * Olauncher-style adapter: just text, no icons.
 * Displays app names in a vertical list with only first letter capitalized.
 */
class HomeAppAdapter(
    private val apps: MutableList<AppInfo>,
    private val onAppClick: (AppInfo) -> Unit,
    private val onAppLongClick: ((AppInfo, View) -> Unit)? = null
) : RecyclerView.Adapter<HomeAppAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app, parent, false)
        return ViewHolder(view)
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
            // Show name with only first letter capitalized
            textName.text = capitalizeFirstLetter(app.label.toString())
            textName.setOnClickListener { onAppClick(app) }
            if (onAppLongClick != null) {
                textName.setOnLongClickListener {
                    onAppLongClick.invoke(app, textName)
                    true
                }
            }
        }
    }

    /**
     * Capitalize only the first letter of each word, keep the rest lowercase.
     * e.g., "GOOGLE CHROME" -> "Google Chrome", "telegram" -> "Telegram"
     */
    private fun capitalizeFirstLetter(text: String): String {
        if (text.isEmpty()) return text
        return text.split(" ").joinToString(" ") { word ->
            if (word.isEmpty()) word
            else word.substring(0, 1).uppercase(Locale.getDefault()) +
                 word.substring(1).lowercase(Locale.getDefault())
        }
    }
}
