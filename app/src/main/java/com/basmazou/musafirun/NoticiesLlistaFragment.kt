package com.basmazou.musafirun

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class NoticiesLlistaFragment : Fragment() {

    private lateinit var rv: RecyclerView
    private lateinit var tvBuit: TextView
    private lateinit var adapter: NoticiesGuardadesAdapter
    private var mode: NoticiesGuardadesAdapter.Mode = NoticiesGuardadesAdapter.Mode.FAVORITES

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        estatInstanciaGuardat: Bundle?
    ): View = inflater.inflate(R.layout.fragment_noticies_llista, container, false)

    override fun onViewCreated(view: View, estatInstanciaGuardat: Bundle?) {
        super.onViewCreated(view, estatInstanciaGuardat)
        mode = NoticiesGuardadesAdapter.Mode.valueOf(
            arguments?.getString(ARG_MODE) ?: NoticiesGuardadesAdapter.Mode.FAVORITES.name
        )

        rv = view.findViewById(R.id.rvNoticiesGuardades)
        tvBuit = view.findViewById(R.id.tvNoticiesGuardadesBuit)
        rv.layoutManager = LinearLayoutManager(requireContext())
        adapter = NoticiesGuardadesAdapter(
            items = mutableListOf(),
            mode = mode,
            onClick = { obrirNoticia(it.enllac) },
            onDelete = { eliminarNoticia(it) }
        )
        rv.adapter = adapter

        tvBuit.setText(textBuitPerMode())
        carregarNoticies()
    }

    override fun onResume() {
        super.onResume()
        carregarNoticies()
    }

    private fun textBuitPerMode(): Int = when (mode) {
        NoticiesGuardadesAdapter.Mode.REBUDES -> R.string.guardades_buit_rebudes
        NoticiesGuardadesAdapter.Mode.ENVIADES -> R.string.guardades_buit_enviades
        NoticiesGuardadesAdapter.Mode.FAVORITES -> R.string.guardades_buit_favorites
    }

    private fun nomColleccio(): String = when (mode) {
        NoticiesGuardadesAdapter.Mode.REBUDES -> "noticies_rebudes"
        NoticiesGuardadesAdapter.Mode.ENVIADES -> "noticies_enviades"
        NoticiesGuardadesAdapter.Mode.FAVORITES -> "noticies_guardades"
    }

    private fun carregarNoticies() {
        val ctx = requireContext()
        val rawId = UserSessionManager.getCurrentUserId(ctx)
        if (rawId.startsWith("guest_")) {
            adapter.submit(emptyList())
            mostrarEstatBuit(true)
            return
        }
        FirebaseFirestore.getInstance()
            .collection("usuaris").document(rawId)
            .collection(nomColleccio())
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snap ->
                if (!isAdded) return@addOnSuccessListener
                val items = snap.documents.map { doc ->
                    NoticiaGuardada(
                        docId = doc.id,
                        titol = doc.getString("titol").orEmpty(),
                        enllac = doc.getString("enllac").orEmpty(),
                        urlImatge = doc.getString("urlImatge").takeUnless { it.isNullOrBlank() },
                        font = doc.getString("font").takeUnless { it.isNullOrBlank() },
                        snippet = doc.getString("snippet").takeUnless { it.isNullOrBlank() },
                        pubDateMillis = doc.getLong("pubDateMillis").takeIf { it != null && it > 0L },
                        sharedBy = doc.getString("sharedBy"),
                        sharedTo = doc.getString("sharedTo"),
                        timestamp = doc.getLong("timestamp") ?: 0L
                    )
                }
                adapter.submit(items)
                mostrarEstatBuit(items.isEmpty())
            }
            .addOnFailureListener {
                if (!isAdded) return@addOnFailureListener
                adapter.submit(emptyList())
                mostrarEstatBuit(true)
            }
    }

    private fun eliminarNoticia(noticia: NoticiaGuardada) {
        val ctx = requireContext()
        val rawId = UserSessionManager.getCurrentUserId(ctx)
        if (rawId.startsWith("guest_")) return
        FirebaseFirestore.getInstance()
            .collection("usuaris").document(rawId)
            .collection(nomColleccio())
            .document(noticia.docId)
            .delete()
            .addOnSuccessListener {
                if (!isAdded) return@addOnSuccessListener
                adapter.removeById(noticia.docId)
                mostrarEstatBuit(adapter.itemCount == 0)
                Toast.makeText(ctx, R.string.guardades_item_removed, Toast.LENGTH_SHORT).show()
            }
    }

    private fun mostrarEstatBuit(buit: Boolean) {
        tvBuit.visibility = if (buit) View.VISIBLE else View.GONE
        rv.visibility = if (buit) View.GONE else View.VISIBLE
    }

    private fun obrirNoticia(url: String) {
        if (url.isBlank()) return
        try {
            CustomTabsIntent.Builder().setShowTitle(true).build()
                .launchUrl(requireContext(), Uri.parse(url))
        } catch (_: Exception) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    }

    companion object {
        private const val ARG_MODE = "mode"
        fun newInstance(mode: NoticiesGuardadesAdapter.Mode) = NoticiesLlistaFragment().apply {
            arguments = Bundle().apply { putString(ARG_MODE, mode.name) }
        }
    }
}


