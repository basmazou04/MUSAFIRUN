package com.basmazou.musafirun

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout

class Organizacion : Fragment() {

    private lateinit var organitzacionsAdapter: OrganitzacionsAdapter
    private var isGuestUser: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        estatInstanciaGuardat: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_organizacion, container, false)
    }

    override fun onViewCreated(view: View, estatInstanciaGuardat: Bundle?) {
        super.onViewCreated(view, estatInstanciaGuardat)

        val textBuit = view.findViewById<TextView>(R.id.tvOrganitzacionsBuides)
        val vistaRecicladora = view.findViewById<RecyclerView>(R.id.rvOrganitzacions)
        val tabs = view.findViewById<TabLayout>(R.id.tabsOrganitzacions)
        val premiumCard = view.findViewById<View>(R.id.cardPremiumCompartides)
        val createButton = view.findViewById<View>(R.id.btn_organitza_viatge)
        isGuestUser = UserSessionManager.getCurrentUserId(requireContext()).startsWith("guest_")

        organitzacionsAdapter = OrganitzacionsAdapter(
            onClick = { organitzacio ->
                parentFragmentManager.beginTransaction()
                    .replace(
                        R.id.fragment_container,
                        DetallOrganitzacioFragment.newInstance(organitzacio.id)
                    )
                    .addToBackStack(null)
                    .commit()
            },
            enEliminarClick = { organitzacio ->
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.confirmacio_eliminar_organitzacio_titol)
                    .setMessage(
                        getString(
                            R.string.confirmacio_eliminar_organitzacio_missatge,
                            organitzacio.pais
                        )
                    )
                    .setPositiveButton(R.string.confirmar_accio) { _, _ ->
                        OrganitzaViatgeRepository.eliminarOrganitzacio(
                            context = requireContext(),
                            organitzacioId = organitzacio.id,
                            onSuccess = { actualitzarOrganitzacions(textBuit) },
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
            },
            onShareClick = {
                Toast.makeText(
                    requireContext(),
                    R.string.premium_feature_soon,
                    Toast.LENGTH_LONG
                ).show()
            }
        )

        vistaRecicladora.layoutManager = LinearLayoutManager(requireContext())
        vistaRecicladora.adapter = organitzacionsAdapter
        tabs.addTab(tabs.newTab().setText(getString(R.string.organitzacions_tab_propies)))
        tabs.addTab(tabs.newTab().setText(getString(R.string.organitzacions_tab_compartides)))
        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val compartides = tab.position == 1
                vistaRecicladora.visibility = if (compartides) View.GONE else View.VISIBLE
                premiumCard.visibility = if (compartides) View.VISIBLE else View.GONE
                textBuit.visibility = if (compartides) View.GONE else textBuit.visibility
            }
            override fun onTabUnselected(tab: TabLayout.Tab) = Unit
            override fun onTabReselected(tab: TabLayout.Tab) = Unit
        })

        parentFragmentManager.setFragmentResultListener(
            REQUEST_GUARDAR_ORGANITZACIO,
            viewLifecycleOwner
        ) { _, bundle ->
            guardarOrganitzacioDesDeResultat(bundle, textBuit)
        }

        createButton.setOnClickListener {
            if (isGuestUser) {
                Toast.makeText(
                    requireContext(),
                    R.string.organitzacio_guest_login_required,
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
            OrganitzaViatgeRepository.iniciarNouEsborrany()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, PantallaOrganitzaViatgeFragment())
                .addToBackStack(null)
                .commit()
        }

        if (isGuestUser) {
            organitzacionsAdapter.actualitzarLlista(emptyList())
            vistaRecicladora.visibility = View.GONE
            textBuit.visibility = View.VISIBLE
            textBuit.text = getString(R.string.organitzacions_guest_info)
            createButton.alpha = 0.6f
        } else {
            vistaRecicladora.visibility = View.VISIBLE
            createButton.alpha = 1f
            recarregarOrganitzacions()
        }
    }

    override fun onResume() {
        super.onResume()
        if (isGuestUser) return
        view?.findViewById<TextView>(R.id.tvOrganitzacionsBuides)?.let {
            actualitzarOrganitzacions(it)
        }
    }

    private fun actualitzarOrganitzacions(textBuit: TextView) {
        val organitzacions = OrganitzaViatgeRepository.obtenirOrganitzacions()
        organitzacionsAdapter.actualitzarLlista(organitzacions)
        textBuit.visibility = if (organitzacions.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun recarregarOrganitzacions() {
        val textBuit = view?.findViewById<TextView>(R.id.tvOrganitzacionsBuides) ?: return
        OrganitzaViatgeRepository.carregarOrganitzacions(
            context = requireContext(),
            onSuccess = { actualitzarOrganitzacions(textBuit) },
            onError = {
                actualitzarOrganitzacions(textBuit)
                Toast.makeText(
                    requireContext(),
                    R.string.error_carregar_organitzacions,
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }

    private fun guardarOrganitzacioDesDeResultat(bundle: Bundle, textBuit: TextView) {
        if (isGuestUser) {
            Toast.makeText(
                requireContext(),
                R.string.organitzacio_guest_login_required,
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val pais = bundle.getString(KEY_PAIS).orEmpty()
        val duracio = bundle.getString(KEY_DURACIO).orEmpty()
        val persones = bundle.getString(KEY_PERSONES).orEmpty()
        val pressupost = bundle.getString(KEY_PRESSUPOST).orEmpty()

        OrganitzaViatgeRepository.guardarOrganitzacio(
            context = requireContext(),
            pais = pais,
            duracio = duracio,
            persones = persones,
            pressupost = pressupost,
            onSuccess = {
                actualitzarOrganitzacions(textBuit)
                Toast.makeText(requireContext(), R.string.viatge_guardat_ok, Toast.LENGTH_SHORT)
                    .show()
            },
            onError = {
                actualitzarOrganitzacions(textBuit)
                Toast.makeText(
                    requireContext(),
                    "${getString(R.string.error_sincronitzacio_viatge)} ${it.message.orEmpty()}",
                    Toast.LENGTH_LONG
                ).show()
            }
        )
    }

    companion object {
        const val REQUEST_GUARDAR_ORGANITZACIO = "request_guardar_organitzacio"
        const val KEY_PAIS = "key_pais"
        const val KEY_DURACIO = "key_duracio"
        const val KEY_PERSONES = "key_persones"
        const val KEY_PRESSUPOST = "key_pressupost"
    }
}


