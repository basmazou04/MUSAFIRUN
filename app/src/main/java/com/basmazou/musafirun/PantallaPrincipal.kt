package com.basmazou.musafirun

import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class PantallaPrincipal : AppCompatActivity() {

    override fun onCreate(estatInstanciaGuardat: Bundle?) {
        AppSettingsManager.applySavedSettings(this)
        super.onCreate(estatInstanciaGuardat)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pantalla_principal)

        val orbHome = findViewById<View>(R.id.crystal_indicator_home)
        val orbSearch = findViewById<View>(R.id.crystal_indicator_search)
        val orbAdd = findViewById<View>(R.id.crystal_indicator_add)
        val orbFav = findViewById<View>(R.id.crystal_indicator_fav)
        val orbSaved = findViewById<View>(R.id.crystal_indicator_saved)
        val navHome = findViewById<ImageView>(R.id.nav_home)
        val navSearch = findViewById<ImageView>(R.id.nav_search)
        val navAdd = findViewById<ImageView>(R.id.nav_add)
        val navFav = findViewById<ImageView>(R.id.nav_fav)
        val navSaved = findViewById<ImageView>(R.id.nav_saved)
        val btnTopProfile = findViewById<ImageView>(R.id.btnTopProfile)
        val topBar = findViewById<View>(R.id.top_bar)
        val navContainer = findViewById<View>(R.id.nav_container)

        val topBarBaseTopPadding = topBar.paddingTop
        ViewCompat.setOnApplyWindowInsetsListener(topBar) { v, insets ->
            val statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            v.setPadding(
                v.paddingLeft,
                topBarBaseTopPadding + statusBars.top,
                v.paddingRight,
                v.paddingBottom
            )
            insets
        }

        val navBaseBottomMargin =
            (navContainer.layoutParams as? ViewGroup.MarginLayoutParams)?.bottomMargin ?: 0
        ViewCompat.setOnApplyWindowInsetsListener(navContainer) { v, insets ->
            val navBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            val layoutParams = v.layoutParams as? ViewGroup.MarginLayoutParams
            if (layoutParams != null) {
                layoutParams.bottomMargin = navBaseBottomMargin + navBars.bottom
                v.layoutParams = layoutParams
            }
            insets
        }

        if (estatInstanciaGuardat == null) {
            replaceFragment(Principal())
            hideAllOrbs()
            orbHome.visibility = View.VISIBLE
        } else {
            sincronitzarOrbAmbFragmentActual(
                orbHome = orbHome,
                orbSearch = orbSearch,
                orbAdd = orbAdd,
                orbFav = orbFav,
                orbSaved = orbSaved
            )
        }

        navHome.setOnClickListener {
            replaceFragment(Principal())
            hideAllOrbs()
            orbHome.visibility = View.VISIBLE
        }

        navSearch.setOnClickListener {
            replaceFragment(Organizacion())
            hideAllOrbs()
            orbSearch.visibility = View.VISIBLE
        }

        navAdd.setOnClickListener {
            replaceFragment(Buscador())
            hideAllOrbs()
            orbAdd.visibility = View.VISIBLE
        }

        navFav.setOnClickListener {
            replaceFragment(Minijuegos())
            hideAllOrbs()
            orbFav.visibility = View.VISIBLE
        }

        navSaved.setOnClickListener {
            replaceFragment(GuardadesCompartides())
            hideAllOrbs()
            orbSaved.visibility = View.VISIBLE
        }

        btnTopProfile.setOnClickListener {
            replaceFragment(AjustesPerfil())
            hideAllOrbs()
        }

    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun hideAllOrbs() {
        findViewById<View>(R.id.crystal_indicator_home).visibility = View.GONE
        findViewById<View>(R.id.crystal_indicator_search).visibility = View.GONE
        findViewById<View>(R.id.crystal_indicator_add).visibility = View.GONE
        findViewById<View>(R.id.crystal_indicator_fav).visibility = View.GONE
        findViewById<View>(R.id.crystal_indicator_saved).visibility = View.GONE
    }

    private fun sincronitzarOrbAmbFragmentActual(
        orbHome: View,
        orbSearch: View,
        orbAdd: View,
        orbFav: View,
        orbSaved: View
    ) {
        hideAllOrbs()
        when (supportFragmentManager.findFragmentById(R.id.fragment_container)) {
            is Principal -> orbHome.visibility = View.VISIBLE
            is Organizacion -> orbSearch.visibility = View.VISIBLE
            is Buscador -> orbAdd.visibility = View.VISIBLE
            is Minijuegos -> orbFav.visibility = View.VISIBLE
            is GuardadesCompartides -> orbSaved.visibility = View.VISIBLE
            else -> orbHome.visibility = View.VISIBLE
        }
    }
}


