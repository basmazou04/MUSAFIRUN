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

class DetallLlistaFragment : Fragment() {

    private lateinit var itemsAdapter: ItemsLlistaAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        estatInstanciaGuardat: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_detall_llista_viatge, container, false)
    }

    override fun onViewCreated(view: View, estatInstanciaGuardat: Bundle?) {
        super.onViewCreated(view, estatInstanciaGuardat)

        val organitzacioId = arguments?.getInt(ARG_ORGANITZACIO_ID)?.takeIf { it != NO_ID }
        val llistaId = requireArguments().getInt(ARG_LLISTA_ID, -1)
        val llista = OrganitzaViatgeRepository.obtenirLlista(organitzacioId, llistaId)
        if (llista == null) {
            parentFragmentManager.popBackStack()
            return
        }

        val textTitol = view.findViewById<TextView>(R.id.tvDetallNomLlista)
        val textBuit = view.findViewById<TextView>(R.id.tvItemsBuitsDetall)
        val nouItemEditText = view.findViewById<EditText>(R.id.etNouItemDetall)
        val vistaRecicladora = view.findViewById<RecyclerView>(R.id.rvItemsDetallLlista)

        textTitol.text = llista.nom

        itemsAdapter = ItemsLlistaAdapter(
            mostrarCheckbox = true,
            onCheckedChange = { item, isChecked ->
                item.completat = isChecked
                OrganitzaViatgeRepository.actualitzarOrdre(organitzacioId, llistaId)
                actualitzarItems(organitzacioId, llistaId, textBuit, itemsAdapter, textTitol)
            },
            enEliminarClick = { item ->
                mostrarConfirmacioEliminarItem(item) {
                    OrganitzaViatgeRepository.eliminarItem(organitzacioId, llistaId, item.id)
                    actualitzarItems(organitzacioId, llistaId, textBuit, itemsAdapter, textTitol)
                }
            }
        )

        vistaRecicladora.layoutManager = LinearLayoutManager(requireContext())
        vistaRecicladora.adapter = itemsAdapter

        view.findViewById<View>(R.id.btnAfegirItemDetall).setOnClickListener {
            val text = nouItemEditText.text.toString().trim()
            if (text.isEmpty()) {
                Toast.makeText(requireContext(), R.string.error_item_buit, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            OrganitzaViatgeRepository.afegirItem(organitzacioId, llistaId, text)
            nouItemEditText.text?.clear()
            actualitzarItems(organitzacioId, llistaId, textBuit, itemsAdapter, textTitol)
        }

        view.findViewById<View>(R.id.btnGuardarCanvisLlista).setOnClickListener {
            if (organitzacioId == null) {
                Toast.makeText(requireContext(), R.string.llista_guardada_ok, Toast.LENGTH_SHORT)
                    .show()
                parentFragmentManager.popBackStack()
            } else {
                OrganitzaViatgeRepository.persistirOrganitzacio(
                    context = requireContext(),
                    organitzacioId = organitzacioId,
                    onSuccess = {
                        Toast.makeText(
                            requireContext(),
                            R.string.llista_guardada_ok,
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
        }

        actualitzarItems(organitzacioId, llistaId, textBuit, itemsAdapter, textTitol)
    }

    private fun actualitzarItems(
        organitzacioId: Int?,
        llistaId: Int,
        textBuit: TextView,
        adapter: ItemsLlistaAdapter,
        textTitol: TextView
    ) {
        val llista = OrganitzaViatgeRepository.obtenirLlista(organitzacioId, llistaId) ?: return
        textTitol.text = llista.nom
        adapter.actualitzarLlista(llista.items.toList())
        textBuit.visibility = if (llista.items.isEmpty()) View.VISIBLE else View.GONE
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
        private const val ARG_LLISTA_ID = "llista_id"
        private const val ARG_ORGANITZACIO_ID = "organitzacio_id"
        private const val NO_ID = -1

        fun newInstance(llistaId: Int, organitzacioId: Int?): DetallLlistaFragment {
            return DetallLlistaFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_LLISTA_ID, llistaId)
                    putInt(ARG_ORGANITZACIO_ID, organitzacioId ?: NO_ID)
                }
            }
        }
    }
}


