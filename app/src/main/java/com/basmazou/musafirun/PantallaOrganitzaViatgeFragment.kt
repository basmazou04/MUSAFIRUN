package com.basmazou.musafirun

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class PantallaOrganitzaViatgeFragment : Fragment() {

    private lateinit var llistesAdapter: LlistesViatgeAdapter
    private lateinit var reservesAdapter: ReservesViatgeAdapter
    private lateinit var gastosAdapter: GastosViatgeAdapter
    private lateinit var llocsAdapter: LlocsViatgeAdapter
    private lateinit var albumFotosAdapter: AlbumFotosEditableAdapter

    private val seleccionarFotosLauncher =
        registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
            if (uris.isNullOrEmpty()) return@registerForActivityResult
            persistirPermisosUris(uris)
            OrganitzaViatgeRepository.afegirFotosAlbum(
                organitzacioId = null,
                fotos = uris.map(Uri::toString)
            )
            view?.findViewById<TextView>(R.id.tvEmptyAlbum)?.let { actualitzarAlbum(it) }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        estatInstanciaGuardat: Bundle?
    ): View {
        return inflater.inflate(R.layout.pantalla_organitza_viatge, container, false)
    }

    override fun onViewCreated(view: View, estatInstanciaGuardat: Bundle?) {
        super.onViewCreated(view, estatInstanciaGuardat)

        val paisEditText = view.findViewById<EditText>(R.id.etPais)
        val duracioEditText = view.findViewById<EditText>(R.id.etDuracio)
        val personesEditText = view.findViewById<EditText>(R.id.etPersones)
        val pressupostEditText = view.findViewById<EditText>(R.id.etPressupost)
        val emptyLlistesText = view.findViewById<TextView>(R.id.tvEmptyLlistes)
        val llistesRecyclerView = view.findViewById<RecyclerView>(R.id.rvLlistesViatge)
        val emptyReservesText = view.findViewById<TextView>(R.id.tvEmptyReserves)
        val reservesRecyclerView = view.findViewById<RecyclerView>(R.id.rvReservesViatge)
        val emptyGastosText = view.findViewById<TextView>(R.id.tvEmptyGastos)
        val gastosRecyclerView = view.findViewById<RecyclerView>(R.id.rvGastosViatge)
        val resumPressupostText = view.findViewById<TextView>(R.id.tvResumPressupostGastos)
        val emptyLlocsText = view.findViewById<TextView>(R.id.tvEmptyLlocs)
        val llocsRecyclerView = view.findViewById<RecyclerView>(R.id.rvLlocsViatge)
        val emptyAlbumText = view.findViewById<TextView>(R.id.tvEmptyAlbum)
        val albumRecyclerView = view.findViewById<RecyclerView>(R.id.rvAlbumFotos)

        llistesAdapter = LlistesViatgeAdapter(
            onLlistaClick = { llista ->
                parentFragmentManager.beginTransaction()
                    .replace(
                        R.id.fragment_container,
                        DetallLlistaFragment.newInstance(llista.id, null)
                    )
                    .addToBackStack(null)
                    .commit()
            },
            enEliminarClick = { llista ->
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.confirmacio_eliminar_llista_titol)
                    .setMessage(getString(R.string.confirmacio_eliminar_llista_missatge, llista.nom))
                    .setPositiveButton(R.string.confirmar_accio) { _, _ ->
                        OrganitzaViatgeRepository.eliminarLlista(null, llista.id)
                        actualitzarLlistes(emptyLlistesText)
                    }
                    .setNegativeButton(R.string.cancelar_accio, null)
                    .show()
            }
        )

        llistesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        llistesRecyclerView.adapter = llistesAdapter

        reservesAdapter = ReservesViatgeAdapter(
            onReservaClick = { reserva ->
                parentFragmentManager.beginTransaction()
                    .replace(
                        R.id.fragment_container,
                        EditarReservaFragment.newInstance(null, reserva.id)
                    )
                    .addToBackStack(null)
                    .commit()
            },
            enEliminarClick = { reserva ->
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.confirmacio_eliminar_reserva_titol)
                    .setMessage(getString(R.string.confirmacio_eliminar_reserva_missatge, reserva.tipus))
                    .setPositiveButton(R.string.confirmar_accio) { _, _ ->
                        OrganitzaViatgeRepository.eliminarReserva(null, reserva.id)
                        actualitzarReserves(emptyReservesText)
                    }
                    .setNegativeButton(R.string.cancelar_accio, null)
                    .show()
            }
        )

        reservesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        reservesRecyclerView.adapter = reservesAdapter

        gastosAdapter = GastosViatgeAdapter(
            onGastoClick = { gasto ->
                parentFragmentManager.beginTransaction()
                    .replace(
                        R.id.fragment_container,
                        EditarGastoFragment.newInstance(null, gasto.id)
                    )
                    .addToBackStack(null)
                    .commit()
            },
            enEliminarClick = { gasto ->
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.confirmacio_eliminar_gasto_titol)
                    .setMessage(getString(R.string.confirmacio_eliminar_gasto_missatge, gasto.tipus))
                    .setPositiveButton(R.string.confirmar_accio) { _, _ ->
                        OrganitzaViatgeRepository.eliminarGasto(null, gasto.id)
                        actualitzarGastos(emptyGastosText, resumPressupostText, pressupostEditText)
                    }
                    .setNegativeButton(R.string.cancelar_accio, null)
                    .show()
            }
        )

        gastosRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        gastosRecyclerView.adapter = gastosAdapter

        llocsAdapter = LlocsViatgeAdapter(
            onLlocClick = { lloc ->
                parentFragmentManager.beginTransaction()
                    .replace(
                        R.id.fragment_container,
                        EditarLlocFragment.newInstance(null, lloc.id)
                    )
                    .addToBackStack(null)
                    .commit()
            },
            onUbicacioClick = { lloc ->
                obrirUbicacioMaps(lloc.ubicacio)
            },
            enEliminarClick = { lloc ->
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.confirmacio_eliminar_lloc_titol)
                    .setMessage(getString(R.string.confirmacio_eliminar_lloc_missatge, lloc.nom))
                    .setPositiveButton(R.string.confirmar_accio) { _, _ ->
                        OrganitzaViatgeRepository.eliminarLloc(null, lloc.id)
                        actualitzarLlocs(emptyLlocsText)
                    }
                    .setNegativeButton(R.string.cancelar_accio, null)
                    .show()
            }
        )

        llocsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        llocsRecyclerView.adapter = llocsAdapter

        albumFotosAdapter = AlbumFotosEditableAdapter(
            enEliminarClick = { fotoUri ->
                OrganitzaViatgeRepository.eliminarFotoAlbum(null, fotoUri)
                actualitzarAlbum(emptyAlbumText)
            }
        )
        albumRecyclerView.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )
        albumRecyclerView.adapter = albumFotosAdapter

        view.findViewById<View>(R.id.btnCrearLlista).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, CrearLlistaFragment.newInstance(null))
                .addToBackStack(null)
                .commit()
        }

        view.findViewById<View>(R.id.btnCrearReserva).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, EditarReservaFragment.newInstance(null, null))
                .addToBackStack(null)
                .commit()
        }

        view.findViewById<View>(R.id.btnCrearGasto).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, EditarGastoFragment.newInstance(null, null))
                .addToBackStack(null)
                .commit()
        }

        view.findViewById<View>(R.id.btnCrearLloc).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, EditarLlocFragment.newInstance(null, null))
                .addToBackStack(null)
                .commit()
        }

        view.findViewById<View>(R.id.btnAfegirFotoAlbum).setOnClickListener {
            seleccionarFotosLauncher.launch(arrayOf("image/*"))
        }

        pressupostEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                actualitzarResumPressupost(resumPressupostText, pressupostEditText)
            }
        })

        view.findViewById<View>(R.id.btnGuardarViatge).setOnClickListener {
            val pais = paisEditText.text.toString().trim()
            val duracio = duracioEditText.text.toString().trim()
            val persones = personesEditText.text.toString().trim()
            val pressupost = pressupostEditText.text.toString().trim()

            if (pais.isEmpty() || duracio.isEmpty() || persones.isEmpty() || pressupost.isEmpty()) {
                Toast.makeText(requireContext(), R.string.error_camps_viatge_buits, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            parentFragmentManager.setFragmentResult(
                Organizacion.REQUEST_GUARDAR_ORGANITZACIO,
                bundleOf(
                    Organizacion.KEY_PAIS to pais,
                    Organizacion.KEY_DURACIO to duracio,
                    Organizacion.KEY_PERSONES to persones,
                    Organizacion.KEY_PRESSUPOST to pressupost
                )
            )
            parentFragmentManager.popBackStack()
        }

        actualitzarLlistes(emptyLlistesText)
        actualitzarReserves(emptyReservesText)
        actualitzarGastos(emptyGastosText, resumPressupostText, pressupostEditText)
        actualitzarLlocs(emptyLlocsText)
        actualitzarAlbum(emptyAlbumText)
    }

    override fun onResume() {
        super.onResume()
        view?.findViewById<TextView>(R.id.tvEmptyLlistes)?.let { actualitzarLlistes(it) }
        view?.findViewById<TextView>(R.id.tvEmptyReserves)?.let { actualitzarReserves(it) }
        val emptyGastos = view?.findViewById<TextView>(R.id.tvEmptyGastos)
        val resumPressupost = view?.findViewById<TextView>(R.id.tvResumPressupostGastos)
        val pressupostEditText = view?.findViewById<EditText>(R.id.etPressupost)
        if (emptyGastos != null && resumPressupost != null && pressupostEditText != null) {
            actualitzarGastos(emptyGastos, resumPressupost, pressupostEditText)
        }
        view?.findViewById<TextView>(R.id.tvEmptyLlocs)?.let { actualitzarLlocs(it) }
        view?.findViewById<TextView>(R.id.tvEmptyAlbum)?.let { actualitzarAlbum(it) }
    }

    private fun actualitzarLlistes(textBuit: TextView) {
        val llistes = OrganitzaViatgeRepository.obtenirLlistes(null)
        llistesAdapter.actualitzarLlista(llistes)
        textBuit.visibility = if (llistes.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun actualitzarReserves(textBuit: TextView) {
        val reserves = OrganitzaViatgeRepository.obtenirReserves(null)
        reservesAdapter.actualitzarLlista(reserves)
        textBuit.visibility = if (reserves.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun actualitzarGastos(
        textBuit: TextView,
        resumPressupostText: TextView,
        pressupostEditText: EditText
    ) {
        val gastos = OrganitzaViatgeRepository.obtenirGastos(null)
        gastosAdapter.actualitzarLlista(gastos)
        textBuit.visibility = if (gastos.isEmpty()) View.VISIBLE else View.GONE
        actualitzarResumPressupost(resumPressupostText, pressupostEditText)
    }

    private fun actualitzarLlocs(textBuit: TextView) {
        val llocs = OrganitzaViatgeRepository.obtenirLlocs(null)
        llocsAdapter.actualitzarLlista(llocs)
        textBuit.visibility = if (llocs.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun actualitzarAlbum(textBuit: TextView) {
        val fotos = OrganitzaViatgeRepository.obtenirAlbumFotos(null)
        albumFotosAdapter.actualitzarLlista(fotos)
        textBuit.visibility = if (fotos.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun actualitzarResumPressupost(
        resumPressupostText: TextView,
        pressupostEditText: EditText
    ) {
        val pressupost = parseEuros(pressupostEditText.text.toString())
        val totalGastos = OrganitzaViatgeRepository.obtenirGastos(null).sumOf {
            parseEuros(it.importGasto)
        }
        resumPressupostText.text = getString(
            R.string.resum_pressupost_gastos,
            formatEuros(pressupost),
            formatEuros(totalGastos)
        )
    }

    private fun parseEuros(valor: String): Double {
        return valor.trim().replace(',', '.').toDoubleOrNull() ?: 0.0
    }

    private fun formatEuros(valor: Double): String {
        return String.format(java.util.Locale.getDefault(), "%.2f", valor)
    }

    private fun obrirUbicacioMaps(ubicacio: String) {
        val link = ubicacio.trim()
        if (link.isEmpty()) return
        val normalized = if (link.startsWith("http://") || link.startsWith("https://")) {
            link
        } else {
            "https://$link"
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(normalized))
        try {
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(requireContext(), R.string.error_obrir_ubicacio, Toast.LENGTH_SHORT).show()
        }
    }

    private fun persistirPermisosUris(uris: List<Uri>) {
        uris.forEach { uri ->
            try {
                requireContext().contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) {
            }
        }
    }
}


