package com.basmazou.musafirun

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NoticiesGuardadesAdapter(
    private val items: MutableList<NoticiaGuardada>,
    private val mode: Mode,
    private val onClick: (NoticiaGuardada) -> Unit,
    private val onDelete: (NoticiaGuardada) -> Unit
) : RecyclerView.Adapter<NoticiesGuardadesAdapter.VH>() {

    enum class Mode { REBUDES, ENVIADES, FAVORITES }

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val imatge: ImageView = view.findViewById(R.id.ivNoticiaGuardada)
        val titol: TextView = view.findViewById(R.id.tvNoticiaGuardadaTitol)
        val meta: TextView = view.findViewById(R.id.tvNoticiaGuardadaMeta)
        val context: TextView = view.findViewById(R.id.tvNoticiaGuardadaContext)
        val eliminar: ImageView = view.findViewById(R.id.btnNoticiaGuardadaEliminar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, tipusVista: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_noticia_guardada, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val n = items[position]
        val ctx = holder.itemView.context
        holder.titol.text = n.titol

        val metaParts = mutableListOf<String>()
        formatarData(n.pubDateMillis)?.let { metaParts.add(it) }
        n.font?.takeIf { it.isNotBlank() }?.let { metaParts.add(it) }
        holder.meta.text = metaParts.joinToString(" Â· ")
        holder.meta.visibility = if (metaParts.isEmpty()) View.GONE else View.VISIBLE

        when (mode) {
            Mode.REBUDES -> {
                val by = n.sharedBy
                if (!by.isNullOrBlank()) {
                    holder.context.visibility = View.VISIBLE
                    holder.context.text = ctx.getString(R.string.guardades_shared_by, by)
                } else holder.context.visibility = View.GONE
            }
            Mode.ENVIADES -> {
                val to = n.sharedTo
                if (!to.isNullOrBlank()) {
                    holder.context.visibility = View.VISIBLE
                    holder.context.text = ctx.getString(R.string.guardades_shared_to, to)
                } else holder.context.visibility = View.GONE
            }
            Mode.FAVORITES -> holder.context.visibility = View.GONE
        }

        if (n.urlImatge.isNullOrBlank()) {
            Glide.with(holder.imatge).clear(holder.imatge)
            holder.imatge.setImageDrawable(null)
        } else {
            Glide.with(holder.imatge)
                .load(n.urlImatge)
                .centerCrop()
                .into(holder.imatge)
        }

        holder.itemView.setOnClickListener { onClick(n) }
        holder.eliminar.setOnClickListener { onDelete(n) }
    }

    override fun getItemCount(): Int = items.size

    fun submit(newItems: List<NoticiaGuardada>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun removeById(docId: String) {
        val idx = items.indexOfFirst { it.docId == docId }
        if (idx >= 0) {
            items.removeAt(idx)
            notifyItemRemoved(idx)
        }
    }

    private fun formatarData(millis: Long?): String? {
        if (millis == null || millis <= 0L) return null
        return try {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(millis))
        } catch (_: Exception) {
            null
        }
    }
}


