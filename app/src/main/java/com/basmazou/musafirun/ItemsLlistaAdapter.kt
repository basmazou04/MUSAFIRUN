package com.basmazou.musafirun

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ItemsLlistaAdapter(
    private val mostrarCheckbox: Boolean,
    private val onCheckedChange: ((LlistaItem, Boolean) -> Unit)? = null,
    private val enEliminarClick: (LlistaItem) -> Unit
) : RecyclerView.Adapter<ItemsLlistaAdapter.ItemViewHolder>() {

    private val items = mutableListOf<LlistaItem>()

    fun actualitzarLlista(nousItems: List<LlistaItem>) {
        items.clear()
        items.addAll(nousItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, tipusVista: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_llista_item_viatge, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val checkBox: CheckBox = itemView.findViewById(R.id.cbItemCompletat)
        private val itemText: TextView = itemView.findViewById(R.id.tvItemText)
        private val botoEliminar: ImageView = itemView.findViewById(R.id.btnEliminarItem)

        fun bind(item: LlistaItem) {
            itemText.text = item.text
            checkBox.visibility = if (mostrarCheckbox) View.VISIBLE else View.GONE
            checkBox.setOnCheckedChangeListener(null)
            checkBox.isChecked = item.completat
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                onCheckedChange?.invoke(item, isChecked)
            }

            botoEliminar.setOnClickListener { enEliminarClick(item) }
        }
    }
}


