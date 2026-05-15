package com.basmazou.musafirun

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GastosViatgeAdapter(
    private val onGastoClick: (GastoViatge) -> Unit,
    private val enEliminarClick: (GastoViatge) -> Unit
) : RecyclerView.Adapter<GastosViatgeAdapter.GastoViewHolder>() {

    private val items = mutableListOf<GastoViatge>()

    fun actualitzarLlista(nousGastos: List<GastoViatge>) {
        items.clear()
        items.addAll(nousGastos)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, tipusVista: Int): GastoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_gasto_viatge, parent, false)
        return GastoViewHolder(view)
    }

    override fun onBindViewHolder(holder: GastoViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class GastoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tipusText: TextView = itemView.findViewById(R.id.tvTipusGasto)
        private val importText: TextView = itemView.findViewById(R.id.tvImportGasto)
        private val botoEliminar: ImageView = itemView.findViewById(R.id.btnEliminarGasto)

        fun bind(gasto: GastoViatge) {
            tipusText.text = gasto.tipus
            importText.text = itemView.context.getString(R.string.resum_gasto_viatge, gasto.importGasto)

            itemView.setOnClickListener { onGastoClick(gasto) }
            botoEliminar.setOnClickListener { enEliminarClick(gasto) }
        }
    }
}


