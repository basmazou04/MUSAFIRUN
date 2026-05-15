package com.basmazou.musafirun

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import com.google.android.material.materialswitch.MaterialSwitch

class AjustesPerfil : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        estatInstanciaGuardat: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_ajustes_perfil, container, false)
    }

    override fun onViewCreated(view: View, estatInstanciaGuardat: Bundle?) {
        super.onViewCreated(view, estatInstanciaGuardat)

        val userNameText = view.findViewById<TextView>(R.id.tvNomUsuariPerfil)
        val ajustesTitle = view.findViewById<TextView>(R.id.tvAjustesTitle)
        val perfilSectionTitle = view.findViewById<TextView>(R.id.tvPerfilTitle)
        val languageTitle = view.findViewById<TextView>(R.id.tvLanguageTitle)
        val themeTitle = view.findViewById<TextView>(R.id.tvThemeTitle)
        val themeSubtitle = view.findViewById<TextView>(R.id.tvThemeSubtitle)
        val languageSubtitle = view.findViewById<TextView>(R.id.tvLanguageSubtitle)
        val switchDarkMode = view.findViewById<MaterialSwitch>(R.id.switchDarkMode)
        val languageGroup = view.findViewById<RadioGroup>(R.id.radioGroupLanguage)
        val rbCatalan = view.findViewById<RadioButton>(R.id.rbCatalan)
        val rbSpanish = view.findViewById<RadioButton>(R.id.rbSpanish)
        val rbEnglish = view.findViewById<RadioButton>(R.id.rbEnglish)

        val currentUserId = UserSessionManager.getCurrentUserId(requireContext())
        userNameText.text = if (currentUserId.startsWith("guest_")) {
            getString(R.string.perfil_guest_user)
        } else {
            currentUserId
        }

        switchDarkMode.isChecked = AppSettingsManager.isDarkModeEnabled(requireContext())
        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            AppSettingsManager.setDarkModeEnabled(requireContext(), isChecked)
        }

        when (AppSettingsManager.getCurrentLanguage(requireContext())) {
            "es" -> languageGroup.check(R.id.rbSpanish)
            "en" -> languageGroup.check(R.id.rbEnglish)
            else -> languageGroup.check(R.id.rbCatalan)
        }

        languageGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedLanguage = when (checkedId) {
                R.id.rbSpanish -> "es"
                R.id.rbEnglish -> "en"
                else -> "ca"
            }

            if (selectedLanguage != AppSettingsManager.getCurrentLanguage(requireContext())) {
                AppSettingsManager.setCurrentLanguage(requireContext(), selectedLanguage)
                ajustesTitle.text = getString(R.string.ajustes_perfil_titol)
                perfilSectionTitle.text = getString(R.string.perfil_titol)
                themeTitle.text = getString(R.string.ajust_theme_title)
                themeSubtitle.text = getString(R.string.ajust_theme_subtitle)
                switchDarkMode.text = getString(R.string.ajust_theme_switch)
                languageTitle.text = getString(R.string.ajust_language_title)
                languageSubtitle.text = getString(R.string.ajust_language_subtitle)
                rbCatalan.text = getString(R.string.language_catalan)
                rbSpanish.text = getString(R.string.language_spanish)
                rbEnglish.text = getString(R.string.language_english)
                userNameText.text = if (currentUserId.startsWith("guest_")) {
                    getString(R.string.perfil_guest_user)
                } else {
                    currentUserId
                }
            }
        }
    }
}


