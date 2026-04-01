/*
 * Delivery Glyph — Developer: Gdusunen
 */
package com.nothing.deliveryglyph

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryAdapter(private val items: List<NotificationEvent>) :
    RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    private val dateFormat = SimpleDateFormat("dd MMM HH:mm", Locale("tr"))

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvAppLabel: TextView  = view.findViewById(R.id.tvAppLabel)
        val tvTitle: TextView     = view.findViewById(R.id.tvTitle)
        val tvTimestamp: TextView = view.findViewById(R.id.tvTimestamp)
        val tvStatus: TextView    = view.findViewById(R.id.tvStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val event = items[position]
        holder.tvAppLabel.text  = event.appLabel
        holder.tvTitle.text     = event.notificationTitle.ifBlank { "—" }
        holder.tvTimestamp.text = dateFormat.format(Date(event.timestampMs))
        holder.tvStatus.text    = event.statusLabel

        // Durum rengini ayarla
        val color = when (event.statusLabel) {
            DeliveryStatus.AT_DOOR.label    -> 0xFF1B5E20.toInt()
            DeliveryStatus.ON_THE_WAY.label -> 0xFF0D47A1.toInt()
            DeliveryStatus.PREPARING.label  -> 0xFFE65100.toInt()
            DeliveryStatus.CONFIRMED.label  -> 0xFF424242.toInt()
            else                            -> 0xFF9E9E9E.toInt()
        }
        holder.tvStatus.setTextColor(color)
    }

    override fun getItemCount(): Int = items.size
}
