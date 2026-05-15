package com.basmazou.musafirun

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class LlistesViatgeAdapter(
    private val onLlistaClick: (LlistaViatge) -> Unit,
    private val enEliminarClick: (LlistaViatge) -> Unit
) : RecyclerView.Adapter<LlistesViatgeAdapter.LlistaViewHolder>() {

    private val items = mutableListOf<LlistaViatge>()

    fun actualitzarLlista(novesLlistes: List<LlistaViatge>) {
        items.clear()
        items.addAll(novesLlistes)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, tipusVista: Int): LlistaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_llista_viatge, parent, false)
        return LlistaViewHolder(view)
    }

    override fun onBindViewHolder(holder: LlistaViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class LlistaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textTitol: TextView = itemView.findViewById(R.id.tvNomLlista)
        private val textComptador: TextView = itemView.findViewById(R.id.tvItemsCount)
        private val botoEliminar: ImageView = itemView.findViewById(R.id.btnEliminarLlista)

        fun bind(llista: LlistaViatge) {
            textTitol.text = llista.nom
            textComptador.text = itemView.context.getString(
                R.string.resum_items_llista,
                llista.items.count { !it.completat },
                llista.items.size
            )

            itemView.setOnClickListener { onLlistaClick(llista) }
            botoEliminar.setOnClickListener { enEliminarClick(llista) }
        }
    }
}


