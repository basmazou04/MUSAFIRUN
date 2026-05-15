package com.basmazou.musafirun

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment

class EditarGastoFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        estatInstanciaGuardat: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_editar_gasto_viatge, container, false)
    }

    override fun onViewCreated(view: View, estatInstanciaGuardat: Bundle?) {
        super.onViewCreated(view, estatInstanciaGuardat)

        val organitzacioId = arguments?.getInt(ARG_ORGANITZACIO_ID)?.takeIf { it != NO_ID }
        val gastoId = arguments?.getInt(ARG_GASTO_ID)?.takeIf { it != NO_ID }
        val gasto = gastoId?.let { OrganitzaViatgeRepository.obtenirGasto(organitzacioId, it) }

        val textTitol = view.findViewById<TextView>(R.id.tvEditarGastoTitle)
        val tipusEditText = view.findViewById<EditText>(R.id.etTipusGasto)
        val importEditText = view.findViewById<EditText>(R.id.etImportGasto)
        val guardarButton = view.findViewById<View>(R.id.btnGuardarGasto)

        if (gasto != null) {
            textTitol.text = getString(R.string.editar_gasto_titol)
            tipusEditText.setText(gasto.tipus)
            importEditText.setText(gasto.importGasto)
        }

        guardarButton.setOnClickListener {
            val tipus = tipusEditText.text.toString().trim()
            val importGasto = importEditText.text.toString().trim()

            if (tipus.isEmpty() || importGasto.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    R.string.error_camps_gasto_obligatoris,
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (gastoId == null) {
                OrganitzaViatgeRepository.crearGasto(
                    organitzacioId = organitzacioId,
                    tipus = tipus,
                    importGasto = importGasto
                )
            } else {
                OrganitzaViatgeRepository.actualitzarGasto(
                    organitzacioId = organitzacioId,
                    gastoId = gastoId,
                    tipus = tipus,
                    importGasto = importGasto
                )
            }

            if (organitzacioId == null) {
                Toast.makeText(requireContext(), R.string.gasto_guardat_ok, Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            } else {
                OrganitzaViatgeRepository.persistirOrganitzacio(
                    context = requireContext(),
                    organitzacioId = organitzacioId,
                    onSuccess = {
                        Toast.makeText(
                            requireContext(),
                            R.string.gasto_guardat_ok,
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
    }

    companion object {
        private const val ARG_ORGANITZACIO_ID = "organitzacio_id"
        private const val ARG_GASTO_ID = "gasto_id"
        private const val NO_ID = -1

        fun newInstance(organitzacioId: Int?, gastoId: Int?): EditarGastoFragment {
            return EditarGastoFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_ORGANITZACIO_ID, organitzacioId ?: NO_ID)
                    putInt(ARG_GASTO_ID, gastoId ?: NO_ID)
                }
            }
        }
    }
}


