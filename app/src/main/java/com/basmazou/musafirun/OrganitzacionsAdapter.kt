package com.basmazou.musafirun

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.ViewFlipper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class OrganitzacionsAdapter(
    private val onClick: (OrganitzacioViatge) -> Unit,
    private val enEliminarClick: (OrganitzacioViatge) -> Unit,
    private val onShareClick: (OrganitzacioViatge) -> Unit
) : RecyclerView.Adapter<OrganitzacionsAdapter.OrganitzacioViewHolder>() {

    private val items = mutableListOf<OrganitzacioViatge>()

    fun actualitzarLlista(novesOrganitzacions: List<OrganitzacioViatge>) {
        items.clear()
        items.addAll(novesOrganitzacions)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, tipusVista: Int): OrganitzacioViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_organitzacio_viatge, parent, false)
        return OrganitzacioViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrganitzacioViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class OrganitzacioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textTitol: TextView = itemView.findViewById(R.id.tvPaisOrganitzacio)
        private val summaryText: TextView = itemView.findViewById(R.id.tvResumOrganitzacio)
        private val botoEliminar: ImageView = itemView.findViewById(R.id.btnEliminarOrganitzacio)
        private val shareButton: ImageView = itemView.findViewById(R.id.btnCompartirOrganitzacio)
        private val albumCarrusel: ViewFlipper = itemView.findViewById(R.id.vfAlbumCarrusel)

        fun bind(organitzacio: OrganitzacioViatge) {
            textTitol.text = organitzacio.pais
            summaryText.text = itemView.context.getString(
                R.string.resum_organitzacio_viatge,
                organitzacio.duracio,
                organitzacio.persones,
                organitzacio.pressupost
            )
            configurarAlbumCarrusel(organitzacio.albumFotos)

            itemView.setOnClickListener { onClick(organitzacio) }
            botoEliminar.setOnClickListener { enEliminarClick(organitzacio) }
            shareButton.setOnClickListener { onShareClick(organitzacio) }
        }

        private fun configurarAlbumCarrusel(fotos: List<String>) {
            albumCarrusel.stopFlipping()
            albumCarrusel.removeAllViews()

            if (fotos.isEmpty()) {
                albumCarrusel.visibility = View.GONE
                return
            }

            albumCarrusel.visibility = View.VISIBLE
            fotos.forEach { fotoUri ->
                val vistaImatge = ImageView(itemView.context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    contentDescription = itemView.context.getString(R.string.descripcio_foto_album)
                }
                Glide.with(itemView)
                    .load(fotoUri)
                    .centerCrop()
                    .into(vistaImatge)
                albumCarrusel.addView(vistaImatge)
            }

            if (fotos.size > 1) {
                albumCarrusel.startFlipping()
            }
        }
    }
}


