package com.basmazou.musafirun

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout

class GuardadesCompartides : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        estatInstanciaGuardat: Bundle?
    ): View = inflater.inflate(R.layout.fragment_guardades_compartides, container, false)

    override fun onViewCreated(view: View, estatInstanciaGuardat: Bundle?) {
        super.onViewCreated(view, estatInstanciaGuardat)
        val tabs = view.findViewById<TabLayout>(R.id.tabsGuardades)
        tabs.addTab(tabs.newTab().setText(getString(R.string.guardades_tab_rebudes)))
        tabs.addTab(tabs.newTab().setText(getString(R.string.guardades_tab_enviades)))
        tabs.addTab(tabs.newTab().setText(getString(R.string.guardades_tab_favorites)))
    }
}


