package com.alya.pinlok

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class LocationAdapter(
    private val locations: List<LocationModel>,
    private val onItemClick: (LocationModel) -> Unit
) : RecyclerView.Adapter<LocationAdapter.LocationViewHolder>() {

    class LocationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvNote: TextView = itemView.findViewById(R.id.tvNote)
        val tvLatLng: TextView = itemView.findViewById(R.id.tvLatLng)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_location, parent, false)
        return LocationViewHolder(view)
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        val loc = locations[position]
        holder.tvName.text = loc.name
        holder.tvNote.text = loc.note
        holder.tvLatLng.text = "Lat: ${loc.lat}, Lng: ${loc.lng}"

        holder.itemView.setOnClickListener {
            onItemClick(loc)
        }
    }

    override fun getItemCount(): Int = locations.size
}