package com.basmazou.musafirun

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CrearLlistaFragment : Fragment() {

    private lateinit var itemsAdapter: ItemsLlistaAdapter
    private val itemsPendents = mutableListOf<LlistaItem>()
    private var nextTempId = 1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        estatInstanciaGuardat: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_crear_llista_viatge, container, false)
    }

    override fun onViewCreated(view: View, estatInstanciaGuardat: Bundle?) {
        super.onViewCreated(view, estatInstanciaGuardat)

        val organitzacioId = arguments?.getInt(ARG_ORGANITZACIO_ID)?.takeIf { it != NO_ID }
        val nomLlistaEditText = view.findViewById<EditText>(R.id.etNomLlista)
        val nouItemEditText = view.findViewById<EditText>(R.id.etNouItemLlista)
        val textBuit = view.findViewById<TextView>(R.id.tvItemsBuitsCrear)
        val vistaRecicladora = view.findViewById<RecyclerView>(R.id.rvItemsCrearLlista)

        itemsAdapter = ItemsLlistaAdapter(
            mostrarCheckbox = false,
            enEliminarClick = { item ->
                mostrarConfirmacioEliminarItem(item) {
                    itemsPendents.removeAll { it.id == item.id }
                    actualitzarItems(textBuit)
                }
            }
        )

        vistaRecicladora.layoutManager = LinearLayoutManager(requireContext())
        vistaRecicladora.adapter = itemsAdapter

        view.findViewById<View>(R.id.btnAfegirItemLlista).setOnClickListener {
            val text = nouItemEditText.text.toString().trim()
            if (text.isEmpty()) {
                Toast.makeText(requireContext(), R.string.error_item_buit, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            itemsPendents.add(LlistaItem(id = nextTempId++, text = text))
            nouItemEditText.text?.clear()
            actualitzarItems(textBuit)
        }

        view.findViewById<View>(R.id.btnGuardarLlista).setOnClickListener {
            val nomLlista = nomLlistaEditText.text.toString().trim()
            if (nomLlista.isEmpty()) {
                Toast.makeText(requireContext(), R.string.error_nom_llista_buit, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (itemsPendents.isEmpty()) {
                Toast.makeText(requireContext(), R.string.error_llista_sense_items, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            OrganitzaViatgeRepository.crearLlista(
                organitzacioId = organitzacioId,
                nom = nomLlista,
                items = itemsPendents.map { it.text }
            )

            if (organitzacioId == null) {
                parentFragmentManager.popBackStack()
            } else {
                OrganitzaViatgeRepository.persistirOrganitzacio(
                    context = requireContext(),
                    organitzacioId = organitzacioId,
                    onSuccess = { parentFragmentManager.popBackStack() },
                    onError = {
                        Toast.makeText(
                            requireContext(),
                            R.string.error_sincronitzacio_viatge,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            }
        }

        actualitzarItems(textBuit)
    }

    private fun actualitzarItems(textBuit: TextView) {
        itemsAdapter.actualitzarLlista(itemsPendents.toList())
        textBuit.visibility = if (itemsPendents.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun mostrarConfirmacioEliminarItem(item: LlistaItem, onConfirm: () -> Unit) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.confirmacio_eliminar_item_titol)
            .setMessage(getString(R.string.confirmacio_eliminar_item_missatge, item.text))
            .setPositiveButton(R.string.confirmar_accio) { _, _ -> onConfirm() }
            .setNegativeButton(R.string.cancelar_accio, null)
            .show()
    }

    companion object {
        private const val ARG_ORGANITZACIO_ID = "organitzacio_id"
        private const val NO_ID = -1

        fun newInstance(organitzacioId: Int?): CrearLlistaFragment {
            return CrearLlistaFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_ORGANITZACIO_ID, organitzacioId ?: NO_ID)
                }
            }
        }
    }
}


