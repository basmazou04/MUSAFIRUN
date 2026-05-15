package com.basmazou.musafirun

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PaisAdapter(
    private val listaOriginal: List<Pais>,
    private val onPaisClick: (Pais) -> Unit
) :
    RecyclerView.Adapter<PaisAdapter.PaisViewHolder>() {

    private var listaFiltrada: MutableList<Pais> = listaOriginal.toMutableList()

    class PaisViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val bandera: ImageView = itemView.findViewById(R.id.imgBandera)
        val pais: TextView = itemView.findViewById(R.id.txtPais)
        val capital: TextView = itemView.findViewById(R.id.txtCapital)
    }

    override fun onCreateViewHolder(parent: ViewGroup, tipusVista: Int): PaisViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pais, parent, false)
        return PaisViewHolder(view)
    }

    override fun onBindViewHolder(holder: PaisViewHolder, position: Int) {
        val item = listaFiltrada[position]

        holder.pais.text = item.nombre
        holder.capital.text = item.capital
        holder.bandera.setImageResource(item.bandera)
        holder.itemView.setOnClickListener { onPaisClick(item) }
    }

    override fun getItemCount(): Int {
        return listaFiltrada.size
    }

    fun filtrar(texto: String) {
        listaFiltrada = if (texto.isEmpty()) {
            listaOriginal.toMutableList()
        } else {
            listaOriginal.filter {
                it.nombre.contains(texto, ignoreCase = true) ||
                        it.capital.contains(texto, ignoreCase = true)
            }.toMutableList()
        }
        notifyDataSetChanged()
    }
}


