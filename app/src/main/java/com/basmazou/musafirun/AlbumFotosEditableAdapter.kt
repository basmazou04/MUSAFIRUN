package com.basmazou.musafirun

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide


class AlbumFotosEditableAdapter(
    private val enEliminarClick: (String) -> Unit
) : RecyclerView.Adapter<AlbumFotosEditableAdapter.AlbumFotoViewHolder>() {

    private val fotos = mutableListOf<String>()

    fun actualitzarLlista(novesFotos: List<String>) {
        fotos.clear()
        fotos.addAll(novesFotos)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, tipusVista: Int): AlbumFotoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_album_foto_editable, parent, false)
        return AlbumFotoViewHolder(view)
    }


    override fun onBindViewHolder(holder: AlbumFotoViewHolder, position: Int) {
        holder.bind(fotos[position])
    }

    override fun getItemCount(): Int = fotos.size

    inner class AlbumFotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val vistaImatge: ImageView = itemView.findViewById(R.id.ivAlbumFoto)
        private val botoEliminar: ImageView = itemView.findViewById(R.id.btnEliminarAlbumFoto)

        fun bind(fotoUri: String) {
            Glide.with(itemView)
                .load(fotoUri)
                .centerCrop()
                .into(vistaImatge)

            botoEliminar.setOnClickListener { enEliminarClick(fotoUri) }
        }
    }
}


