package com.symmetrytechec.listaminima

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AppAdapter(private val onAppClick: (MainActivity.AppInfo) -> Unit) : 
    RecyclerView.Adapter<AppAdapter.AppViewHolder>() {
    
    private var apps = listOf<MainActivity.AppInfo>()

    fun updateApps(newApps: List<MainActivity.AppInfo>) {
        apps = newApps
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app, parent, false)
        return AppViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        holder.bind(apps[position])
    }

    override fun getItemCount(): Int = apps.size

    inner class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val appNameTextView: TextView = itemView.findViewById(R.id.appNameTextView)

        fun bind(appInfo: MainActivity.AppInfo) {
            appNameTextView.text = appInfo.name
            itemView.setOnClickListener {
                onAppClick(appInfo)
            }
        }
    }
} 