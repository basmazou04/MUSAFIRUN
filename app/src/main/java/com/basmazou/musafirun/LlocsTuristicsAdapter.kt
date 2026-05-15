package com.basmazou.musafirun

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class LlocsTuristicsAdapter(
    private val items: List<CountryDetailFragment.LlocTuristic>
) : RecyclerView.Adapter<LlocsTuristicsAdapter.LlocTuristicViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, tipusVista: Int): LlocTuristicViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_lloc_turistic, parent, false)
        return LlocTuristicViewHolder(view)
    }

    override fun onBindViewHolder(holder: LlocTuristicViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class LlocTuristicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textTitol: TextView = itemView.findViewById(R.id.tvLlocTuristicTitle)
        private val descText: TextView = itemView.findViewById(R.id.tvLlocTuristicDesc)

        fun bind(item: CountryDetailFragment.LlocTuristic) {
            textTitol.text = itemView.context.getString(item.titleRes)
            descText.text = itemView.context.getString(item.descRes)
        }
    }
}


