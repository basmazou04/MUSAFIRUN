package com.basmazou.musafirun

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class LlocsViatgeAdapter(
    private val onLlocClick: (LlocViatge) -> Unit,
    private val onUbicacioClick: (LlocViatge) -> Unit,
    private val enEliminarClick: (LlocViatge) -> Unit
) : RecyclerView.Adapter<LlocsViatgeAdapter.LlocViewHolder>() {

    private val items = mutableListOf<LlocViatge>()

    fun actualitzarLlista(nousLlocs: List<LlocViatge>) {
        items.clear()
        items.addAll(nousLlocs)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, tipusVista: Int): LlocViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_lloc_viatge, parent, false)
        return LlocViewHolder(view)
    }

    override fun onBindViewHolder(holder: LlocViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class LlocViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nomText: TextView = itemView.findViewById(R.id.tvNomLloc)
        private val ubicacioText: TextView = itemView.findViewById(R.id.tvUbicacioLloc)
        private val botoEliminar: ImageView = itemView.findViewById(R.id.btnEliminarLloc)

        fun bind(lloc: LlocViatge) {
            nomText.text = lloc.nom
            itemView.setOnClickListener { onLlocClick(lloc) }
            ubicacioText.setOnClickListener { onUbicacioClick(lloc) }
            botoEliminar.setOnClickListener { enEliminarClick(lloc) }
        }
    }
}


