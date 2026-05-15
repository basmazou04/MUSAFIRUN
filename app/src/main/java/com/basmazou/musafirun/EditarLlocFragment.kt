package com.basmazou.musafirun

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment

class EditarLlocFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        estatInstanciaGuardat: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_editar_lloc_viatge, container, false)
    }

    override fun onViewCreated(view: View, estatInstanciaGuardat: Bundle?) {
        super.onViewCreated(view, estatInstanciaGuardat)

        val organitzacioId = arguments?.getInt(ARG_ORGANITZACIO_ID)?.takeIf { it != NO_ID }
        val llocId = arguments?.getInt(ARG_LLOC_ID)?.takeIf { it != NO_ID }
        val lloc = llocId?.let { OrganitzaViatgeRepository.obtenirLloc(organitzacioId, it) }

        val textTitol = view.findViewById<TextView>(R.id.tvEditarLlocTitle)
        val nomEditText = view.findViewById<EditText>(R.id.etNomLloc)
        val ubicacioEditText = view.findViewById<EditText>(R.id.etUbicacioLloc)
        val guardarButton = view.findViewById<View>(R.id.btnGuardarLloc)

        if (lloc != null) {
            textTitol.text = getString(R.string.editar_lloc_titol)
            nomEditText.setText(lloc.nom)
            ubicacioEditText.setText(lloc.ubicacio)
        }

        guardarButton.setOnClickListener {
            val nom = nomEditText.text.toString().trim()
            val ubicacio = ubicacioEditText.text.toString().trim()

            if (nom.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    R.string.error_camps_lloc_obligatoris,
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (llocId == null) {
                OrganitzaViatgeRepository.crearLloc(
                    organitzacioId = organitzacioId,
                    nom = nom,
                    ubicacio = ubicacio
                )
            } else {
                OrganitzaViatgeRepository.actualitzarLloc(
                    organitzacioId = organitzacioId,
                    llocId = llocId,
                    nom = nom,
                    ubicacio = ubicacio
                )
            }

            if (organitzacioId == null) {
                Toast.makeText(requireContext(), R.string.lloc_guardat_ok, Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            } else {
                OrganitzaViatgeRepository.persistirOrganitzacio(
                    context = requireContext(),
                    organitzacioId = organitzacioId,
                    onSuccess = {
                        Toast.makeText(requireContext(), R.string.lloc_guardat_ok, Toast.LENGTH_SHORT)
                            .show()
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
    }

    companion object {
        private const val ARG_ORGANITZACIO_ID = "organitzacio_id"
        private const val ARG_LLOC_ID = "lloc_id"
        private const val NO_ID = -1

        fun newInstance(organitzacioId: Int?, llocId: Int?): EditarLlocFragment {
            return EditarLlocFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_ORGANITZACIO_ID, organitzacioId ?: NO_ID)
                    putInt(ARG_LLOC_ID, llocId ?: NO_ID)
                }
            }
        }
    }
}


