package com.basmazou.musafirun

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class DetallOrganitzacioFragment : Fragment() {

    private lateinit var llistesAdapter: LlistesViatgeAdapter
    private lateinit var reservesAdapter: ReservesViatgeAdapter
    private lateinit var gastosAdapter: GastosViatgeAdapter
    private lateinit var llocsAdapter: LlocsViatgeAdapter
    private lateinit var albumFotosAdapter: AlbumFotosEditableAdapter
    private var currentOrganitzacioId: Int = -1

    private val seleccionarFotosLauncher =
        registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
            // Desa permisos persistents per poder tornar a mostrar les fotos en futures sessions.
            if (uris.isNullOrEmpty() || currentOrganitzacioId == -1) return@registerForActivityResult
            persistirPermisosUris(uris)
            OrganitzaViatgeRepository.afegirFotosAlbum(
                organitzacioId = currentOrganitzacioId,
                fotos = uris.map(Uri::toString)
            )
            view?.findViewById<TextView>(R.id.tvDetallAlbumBuit)?.let {
                actualitzarAlbum(currentOrganitzacioId, it)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        estatInstanciaGuardat: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_detall_organitzacio_viatge, container, false)
    }

    override fun onViewCreated(view: View, estatInstanciaGuardat: Bundle?) {
        super.onViewCreated(view, estatInstanciaGuardat)

        val organitzacioId = requireArguments().getInt(ARG_ORGANITZACIO_ID, -1)
        currentOrganitzacioId = organitzacioId
        val organitzacio = OrganitzaViatgeRepository.obtenirOrganitzacio(organitzacioId)
        if (organitzacio == null) {
            parentFragmentManager.popBackStack()
            return
        }

        view.findViewById<TextView>(R.id.tvDetallPaisValor).text = organitzacio.pais
        view.findViewById<TextView>(R.id.tvDetallDuracioValor).text = organitzacio.duracio
        view.findViewById<TextView>(R.id.tvDetallPersonesValor).text = organitzacio.persones
        view.findViewById<TextView>(R.id.tvDetallPressupostValor).text = organitzacio.pressupost

        val emptyLlistesText = view.findViewById<TextView>(R.id.tvDetallLlistesBuides)
        val llistesRecyclerView = view.findViewById<RecyclerView>(R.id.rvDetallLlistes)
        val emptyReservesText = view.findViewById<TextView>(R.id.tvDetallReservesBuides)
        val reservesRecyclerView = view.findViewById<RecyclerView>(R.id.rvDetallReserves)
        val emptyGastosText = view.findViewById<TextView>(R.id.tvDetallGastosBuides)
        val gastosRecyclerView = view.findViewById<RecyclerView>(R.id.rvDetallGastos)
        val resumPressupostText = view.findViewById<TextView>(R.id.tvDetallResumPressupostGastos)
        val emptyLlocsText = view.findViewById<TextView>(R.id.tvDetallLlocsBuides)
        val llocsRecyclerView = view.findViewById<RecyclerView>(R.id.rvDetallLlocs)
        val emptyAlbumText = view.findViewById<TextView>(R.id.tvDetallAlbumBuit)
        val albumRecyclerView = view.findViewById<RecyclerView>(R.id.rvDetallAlbumFotos)

        llistesAdapter = LlistesViatgeAdapter(
            onLlistaClick = { llista ->
                parentFragmentManager.beginTransaction()
                    .replace(
                        R.id.fragment_container,
                        DetallLlistaFragment.newInstance(llista.id, organitzacioId)
                    )
                    .addToBackStack(null)
                    .commit()
            },
            enEliminarClick = { llista ->
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.confirmacio_eliminar_llista_titol)
                    .setMessage(getString(R.string.confirmacio_eliminar_llista_missatge, llista.nom))
                    .setPositiveButton(R.string.confirmar_accio) { _, _ ->
                        OrganitzaViatgeRepository.eliminarLlista(organitzacioId, llista.id)
                        OrganitzaViatgeRepository.persistirOrganitzacio(
                            context = requireContext(),
                            organitzacioId = organitzacioId,
                            onSuccess = { actualitzarLlistes(organitzacioId, emptyLlistesText) },
                            onError = {
                                Toast.makeText(
                                    requireContext(),
                                    R.string.error_sincronitzacio_viatge,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
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
                        EditarReservaFragment.newInstance(organitzacioId, reserva.id)
                    )
                    .addToBackStack(null)
                    .commit()
            },
            enEliminarClick = { reserva ->
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.confirmacio_eliminar_reserva_titol)
                    .setMessage(getString(R.string.confirmacio_eliminar_reserva_missatge, reserva.tipus))
                    .setPositiveButton(R.string.confirmar_accio) { _, _ ->
                        OrganitzaViatgeRepository.eliminarReserva(organitzacioId, reserva.id)
                        OrganitzaViatgeRepository.persistirOrganitzacio(
                            context = requireContext(),
                            organitzacioId = organitzacioId,
                            onSuccess = { actualitzarReserves(organitzacioId, emptyReservesText) },
                            onError = {
                                Toast.makeText(
                                    requireContext(),
                                    R.string.error_sincronitzacio_viatge,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
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
                        EditarGastoFragment.newInstance(organitzacioId, gasto.id)
                    )
                    .addToBackStack(null)
                    .commit()
            },
            enEliminarClick = { gasto ->
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.confirmacio_eliminar_gasto_titol)
                    .setMessage(getString(R.string.confirmacio_eliminar_gasto_missatge, gasto.tipus))
                    .setPositiveButton(R.string.confirmar_accio) { _, _ ->
                        OrganitzaViatgeRepository.eliminarGasto(organitzacioId, gasto.id)
                        OrganitzaViatgeRepository.persistirOrganitzacio(
                            context = requireContext(),
                            organitzacioId = organitzacioId,
                            onSuccess = {
                                actualitzarGastos(organitzacioId, emptyGastosText, resumPressupostText)
                            },
                            onError = {
                                Toast.makeText(
                                    requireContext(),
                                    R.string.error_sincronitzacio_viatge,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
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
                        EditarLlocFragment.newInstance(organitzacioId, lloc.id)
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
                        OrganitzaViatgeRepository.eliminarLloc(organitzacioId, lloc.id)
                        OrganitzaViatgeRepository.persistirOrganitzacio(
                            context = requireContext(),
                            organitzacioId = organitzacioId,
                            onSuccess = { actualitzarLlocs(organitzacioId, emptyLlocsText) },
                            onError = {
                                Toast.makeText(
                                    requireContext(),
                                    R.string.error_sincronitzacio_viatge,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                    }
                    .setNegativeButton(R.string.cancelar_accio, null)
                    .show()
            }
        )

        llocsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        llocsRecyclerView.adapter = llocsAdapter

        albumFotosAdapter = AlbumFotosEditableAdapter(
            enEliminarClick = { fotoUri ->
                OrganitzaViatgeRepository.eliminarFotoAlbum(organitzacioId, fotoUri)
                actualitzarAlbum(organitzacioId, emptyAlbumText)
            }
        )
        albumRecyclerView.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )
        albumRecyclerView.adapter = albumFotosAdapter

        view.findViewById<View>(R.id.btnAfegirLlistaDetallOrganitzacio).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(
                    R.id.fragment_container,
                    CrearLlistaFragment.newInstance(organitzacioId)
                )
                .addToBackStack(null)
                .commit()
        }

        view.findViewById<View>(R.id.btnAfegirReservaDetallOrganitzacio).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(
                    R.id.fragment_container,
                    EditarReservaFragment.newInstance(organitzacioId, null)
                )
                .addToBackStack(null)
                .commit()
        }

        view.findViewById<View>(R.id.btnAfegirGastoDetallOrganitzacio).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(
                    R.id.fragment_container,
                    EditarGastoFragment.newInstance(organitzacioId, null)
                )
                .addToBackStack(null)
                .commit()
        }

        view.findViewById<View>(R.id.btnAfegirLlocDetallOrganitzacio).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(
                    R.id.fragment_container,
                    EditarLlocFragment.newInstance(organitzacioId, null)
                )
                .addToBackStack(null)
                .commit()
        }

        view.findViewById<View>(R.id.btnAfegirFotoAlbumDetallOrganitzacio).setOnClickListener {
            seleccionarFotosLauncher.launch(arrayOf("image/*"))
        }

        view.findViewById<View>(R.id.btnGuardarCanvisOrganitzacio).setOnClickListener {
            OrganitzaViatgeRepository.persistirOrganitzacio(
                context = requireContext(),
                organitzacioId = organitzacioId,
                onSuccess = {
                    Toast.makeText(
                        requireContext(),
                        R.string.organitzacio_guardada_ok,
                        Toast.LENGTH_SHORT
                    ).show()
                    parentFragmentManager.popBackStack()
                },
                onError = {
                    Toast.makeText(
                        requireContext(),
                        R.string.error_sincronitzacio_viatge,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }

        actualitzarLlistes(organitzacioId, emptyLlistesText)
        actualitzarReserves(organitzacioId, emptyReservesText)
        actualitzarGastos(organitzacioId, emptyGastosText, resumPressupostText)
        actualitzarLlocs(organitzacioId, emptyLlocsText)
        actualitzarAlbum(organitzacioId, emptyAlbumText)
    }

    override fun onResume() {
        super.onResume()
        val organitzacioId = arguments?.getInt(ARG_ORGANITZACIO_ID, -1) ?: -1
        view?.findViewById<TextView>(R.id.tvDetallLlistesBuides)?.let {
            actualitzarLlistes(organitzacioId, it)
        }
        view?.findViewById<TextView>(R.id.tvDetallReservesBuides)?.let {
            actualitzarReserves(organitzacioId, it)
        }
        val emptyGastosText = view?.findViewById<TextView>(R.id.tvDetallGastosBuides)
        val resumPressupostText = view?.findViewById<TextView>(R.id.tvDetallResumPressupostGastos)
        if (emptyGastosText != null && resumPressupostText != null) {
            actualitzarGastos(organitzacioId, emptyGastosText, resumPressupostText)
        }
        view?.findViewById<TextView>(R.id.tvDetallLlocsBuides)?.let {
            actualitzarLlocs(organitzacioId, it)
        }
        view?.findViewById<TextView>(R.id.tvDetallAlbumBuit)?.let {
            actualitzarAlbum(organitzacioId, it)
        }
    }

    private fun actualitzarLlistes(organitzacioId: Int, textBuit: TextView) {
        val llistes = OrganitzaViatgeRepository.obtenirLlistes(organitzacioId)
        llistesAdapter.actualitzarLlista(llistes)
        textBuit.visibility = if (llistes.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun actualitzarReserves(organitzacioId: Int, textBuit: TextView) {
        val reserves = OrganitzaViatgeRepository.obtenirReserves(organitzacioId)
        reservesAdapter.actualitzarLlista(reserves)
        textBuit.visibility = if (reserves.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun actualitzarGastos(
        organitzacioId: Int,
        textBuit: TextView,
        resumPressupostText: TextView
    ) {
        val gastos = OrganitzaViatgeRepository.obtenirGastos(organitzacioId)
        gastosAdapter.actualitzarLlista(gastos)
        textBuit.visibility = if (gastos.isEmpty()) View.VISIBLE else View.GONE

        val organitzacio = OrganitzaViatgeRepository.obtenirOrganitzacio(organitzacioId)
        val pressupost = parseEuros(organitzacio?.pressupost.orEmpty())
        val totalGastos = gastos.sumOf { parseEuros(it.importGasto) }

        // Mostra pressupost i despesa acumulada per visualitzar ràpidament si el viatge s'ajusta al límit.
        resumPressupostText.text = getString(
            R.string.resum_pressupost_gastos,
            formatEuros(pressupost),
            formatEuros(totalGastos)
        )
    }

    private fun actualitzarLlocs(organitzacioId: Int, textBuit: TextView) {
        val llocs = OrganitzaViatgeRepository.obtenirLlocs(organitzacioId)
        llocsAdapter.actualitzarLlista(llocs)
        textBuit.visibility = if (llocs.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun actualitzarAlbum(organitzacioId: Int, textBuit: TextView) {
        val fotos = OrganitzaViatgeRepository.obtenirAlbumFotos(organitzacioId)
        albumFotosAdapter.actualitzarLlista(fotos)
        textBuit.visibility = if (fotos.isEmpty()) View.VISIBLE else View.GONE
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
        // Normalitza enllaços sense protocol perquè Android els pugui resoldre correctament.
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

    companion object {
        private const val ARG_ORGANITZACIO_ID = "organitzacio_id"

        fun newInstance(organitzacioId: Int): DetallOrganitzacioFragment {
            return DetallOrganitzacioFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_ORGANITZACIO_ID, organitzacioId)
                }
            }
        }
    }
}


