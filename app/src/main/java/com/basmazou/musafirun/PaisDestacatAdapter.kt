package com.basmazou.musafirun

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PaisDestacatAdapter(
    private val paisos: List<Pais>,
    private val onClick: (Pais) -> Unit
) : RecyclerView.Adapter<PaisDestacatAdapter.PaisDestacatVH>() {

    class PaisDestacatVH(view: View) : RecyclerView.ViewHolder(view) {
        val bandera: ImageView = view.findViewById(R.id.imgBanderaDestacat)
        val pais: TextView = view.findViewById(R.id.txtPaisDestacat)
        val capital: TextView = view.findViewById(R.id.txtCapitalDestacat)
    }

    override fun onCreateViewHolder(parent: ViewGroup, tipusVista: Int): PaisDestacatVH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_pais_destacat, parent, false)
        return PaisDestacatVH(v)
    }

    override fun onBindViewHolder(holder: PaisDestacatVH, position: Int) {
        val item = paisos[position]
        holder.pais.text = item.nombre
        holder.capital.text = item.capital
        holder.bandera.setImageResource(item.bandera)
        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount(): Int = paisos.size
}


