package com.minillauncher.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.minillauncher.R
import com.minillauncher.utils.AppInfo
import java.util.Locale

/**
 * Adapter for the app drawer list.
 * Shows apps in a list with optional alphabet headers.
 */
class DrawerAppAdapter(
    private val apps: MutableList<AppInfo>,
    private val showLabels: Boolean = true,
    private val iconScale: Float = 1.0f,
    private val showAlphabetHeaders: Boolean = true,
    private val onAppClick: (AppInfo) -> Unit,
    private val onAppLongClick: ((AppInfo, View) -> Unit)? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_APP = 1
    }

    private val displayItems = mutableListOf<DisplayItem>()

    sealed class DisplayItem {
        data class Header(val letter: String) : DisplayItem()
        data class App(val appInfo: AppInfo) : DisplayItem()
    }

    fun updateApps(newApps: List<AppInfo>) {
        apps.clear()
        apps.addAll(newApps)
        buildDisplayItems()
        notifyDataSetChanged()
    }

    private fun buildDisplayItems() {
        displayItems.clear()
        if (!showAlphabetHeaders) {
            apps.forEach { displayItems.add(DisplayItem.App(it)) }
            return
        }

        var currentLetter: String? = null
        for (app in apps) {
            val letter = app.sortLetter.uppercaseChar().toString()
            if (letter != currentLetter) {
                displayItems.add(DisplayItem.Header(letter))
                currentLetter = letter
            }
            displayItems.add(DisplayItem.App(app))
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (displayItems[position]) {
            is DisplayItem.Header -> TYPE_HEADER
            is DisplayItem.App -> TYPE_APP
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_alphabet_header, parent, false)
                HeaderViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_app_drawer, parent, false)
                AppViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = displayItems[position]) {
            is DisplayItem.Header -> (holder as HeaderViewHolder).bind(item.letter)
            is DisplayItem.App -> (holder as AppViewHolder).bind(item.appInfo)
        }
    }

    override fun getItemCount(): Int = displayItems.size

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textHeader: TextView = itemView.findViewById(R.id.text_alphabet_header)
        fun bind(letter: String) {
            textHeader.text = letter
        }
    }

    inner class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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
