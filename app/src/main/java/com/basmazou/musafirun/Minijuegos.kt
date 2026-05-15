package com.basmazou.musafirun

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
class Minijuegos : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        estatInstanciaGuardat: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_minijuegos, container, false)
    }

    override fun onViewCreated(view: View, estatInstanciaGuardat: Bundle?) {
        super.onViewCreated(view, estatInstanciaGuardat)

        view.findViewById<View>(R.id.cardAdivinarBandera).setOnClickListener {
            obrirJoc(MinijocBanderaFragment())
        }
        view.findViewById<View>(R.id.cardCapitales).setOnClickListener {
            obrirJoc(MinijocCapitalesFragment())
        }
        view.findViewById<View>(R.id.cardCulturaGeneral).setOnClickListener {
            obrirJoc(MinijocCulturaGeneralFragment())
        }
    }

    private fun obrirJoc(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}


