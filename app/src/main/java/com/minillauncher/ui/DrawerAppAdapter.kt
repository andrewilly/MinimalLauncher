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
 * Drawer adapter: alphabetical app list with headers.
 * FIX: Click and long-click handlers properly wired.
 */
class DrawerAppAdapter(
    private val apps: MutableList<AppInfo>,
    private val showAlphabetHeaders: Boolean = true,
    private val onAppClick: (AppInfo) -> Unit,
    private val onAppLongClick: ((AppInfo, View) -> Unit)? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_APP = 1
    }

    private val items = mutableListOf<Item>()
    sealed class Item { data class Header(val letter: String) : Item(); data class App(val info: AppInfo) : Item() }

    fun updateApps(newApps: List<AppInfo>) {
        apps.clear()
        apps.addAll(newApps)
        items.clear()
        var cur: String? = null
        for (app in apps) {
            if (showAlphabetHeaders) {
                val letter = app.sortLetter.uppercaseChar().toString()
                if (letter != cur) { items.add(Item.Header(letter)); cur = letter }
            }
            items.add(Item.App(app))
        }
        notifyDataSetChanged()
    }

    override fun getItemViewType(pos: Int) = if (items[pos] is Item.Header) TYPE_HEADER else TYPE_APP

    override fun onCreateViewHolder(parent: ViewGroup, vt: Int): RecyclerView.ViewHolder {
        return if (vt == TYPE_HEADER) {
            HeaderViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_alphabet_header, parent, false))
        } else {
            AppViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_app_drawer, parent, false))
        }
    }

    override fun onBindViewHolder(h: RecyclerView.ViewHolder, pos: Int) {
        when (val it = items[pos]) {
            is Item.Header -> (h as HeaderViewHolder).bind(it.letter)
            is Item.App -> (h as AppViewHolder).bind(it.info, onAppClick, onAppLongClick)
        }
    }

    override fun getItemCount() = items.size

    class HeaderViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val tv: TextView = v.findViewById(R.id.text_alphabet_header)
        fun bind(l: String) { tv.text = l }
    }

    class AppViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val tv: TextView = v.findViewById(R.id.text_app_name)

        fun bind(app: AppInfo, click: (AppInfo) -> Unit, longClick: ((AppInfo, View) -> Unit)?) {
            tv.text = app.label.capitalizeFirstLetter()
            itemView.setOnClickListener { click(app) }
            if (longClick != null) {
                itemView.setOnLongClickListener {
                    itemView.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
                    longClick.invoke(app, itemView)
                    true
                }
            }
        }
    }
}
