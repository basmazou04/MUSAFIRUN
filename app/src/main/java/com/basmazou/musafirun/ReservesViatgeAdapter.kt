package com.basmazou.musafirun

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class ReservesViatgeAdapter(
    private val onReservaClick: (ReservaViatge) -> Unit,
    private val enEliminarClick: (ReservaViatge) -> Unit
) : RecyclerView.Adapter<ReservesViatgeAdapter.ReservaViewHolder>() {

    private val items = mutableListOf<ReservaViatge>()

    fun actualitzarLlista(novesReserves: List<ReservaViatge>) {
        items.clear()
        items.addAll(novesReserves)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, tipusVista: Int): ReservaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reserva_viatge, parent, false)
        return ReservaViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReservaViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ReservaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tipusText: TextView = itemView.findViewById(R.id.tvTipusReserva)
        private val resumText: TextView = itemView.findViewById(R.id.tvResumReserva)
        private val botoEliminar: ImageView = itemView.findViewById(R.id.btnEliminarReserva)

        fun bind(reserva: ReservaViatge) {
            tipusText.text = reserva.tipus
            resumText.text = itemView.context.getString(
                R.string.resum_reserva_viatge,
                reserva.codi,
                reserva.diaReserva,
                reserva.preu
            )

            itemView.setOnClickListener { onReservaClick(reserva) }
            botoEliminar.setOnClickListener { enEliminarClick(reserva) }
        }
    }
}


