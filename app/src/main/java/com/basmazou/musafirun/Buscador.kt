package com.basmazou.musafirun

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class Buscador : Fragment(R.layout.fragment_buscador) {

    override fun onViewCreated(view: View, estatInstanciaGuardat: Bundle?) {
        super.onViewCreated(view, estatInstanciaGuardat)

        val vistaCercador = view.findViewById<SearchView>(R.id.searchView)

        val textCercador = vistaCercador.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        textCercador.hint = getString(R.string.buscador_hint)
        textCercador.setHintTextColor(Color.WHITE)
        vistaCercador.setOnCloseListener {
            textCercador.hint = getString(R.string.buscador_hint)
            false
        }

        val recycler = view.findViewById<RecyclerView>(R.id.recyclerPaises)
        recycler.layoutManager = LinearLayoutManager(requireContext())

        // Llista inicial de països de prova; es pot escalar fàcilment afegint nous elements.
        val listaPaises = listOf(
            Pais(CountryDetailFragment.COUNTRY_ESPANYA, getString(R.string.pais_espanya), getString(R.string.capital_madrid), R.drawable.espanya),
            Pais(CountryDetailFragment.COUNTRY_FRANCA, getString(R.string.pais_franca), getString(R.string.capital_paris), R.drawable.francia),
            Pais(CountryDetailFragment.COUNTRY_BELGICA, getString(R.string.pais_belgica), getString(R.string.capital_brusselles), R.drawable.belgica),
            Pais(CountryDetailFragment.COUNTRY_MARROC, getString(R.string.pais_marroc), getString(R.string.capital_rabat), R.drawable.marruecos),
            Pais(CountryDetailFragment.COUNTRY_TURQUIA, getString(R.string.pais_turquia), getString(R.string.capital_ankara), R.drawable.turquia)
        )

        val adapter = PaisAdapter(
            listaOriginal = listaPaises,
            onPaisClick = { pais ->
                parentFragmentManager.beginTransaction()
                    .replace(
                        R.id.fragment_container,
                        CountryDetailFragment.newInstance(pais.code)
                    )
                    .addToBackStack(null)
                    .commit()
            }
        )
        recycler.adapter = adapter

        vistaCercador.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                // Aplica filtre també en prémer "buscar" des del teclat.
                adapter.filtrar(query.orEmpty())
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filtrar(newText.orEmpty())
                if (newText.isNullOrEmpty()) {
                    textCercador.hint = getString(R.string.buscador_hint)
                }
                return true
            }
        })
    }
}


