package com.basmazou.musafirun

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.ViewFlipper
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CountryDetailFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        estatInstanciaGuardat: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_country_detail, container, false)
    }

    override fun onViewCreated(view: View, estatInstanciaGuardat: Bundle?) {
        super.onViewCreated(view, estatInstanciaGuardat)
        val countryCode = requireArguments().getString(ARG_COUNTRY_CODE).orEmpty()
        val content = obtenirContingut(countryCode) ?: run {
            parentFragmentManager.popBackStack()
            return
        }

        view.findViewById<TextView>(R.id.tvCountryTitle).text = getString(content.titleRes)
        view.findViewById<TextView>(R.id.tvCountrySubtitle).text = getString(content.subtitleRes)
        view.findViewById<TextView>(R.id.tvCountryCapitalValue).text = getString(content.capitalRes)
        view.findViewById<TextView>(R.id.tvCountryPopulationValue).text = getString(content.populationRes)
        view.findViewById<TextView>(R.id.tvCountryReligionValue).text = getString(content.religionRes)

        val carrusel = view.findViewById<ViewFlipper>(R.id.vfCountryPhotos)
        configurarCarrusel(carrusel, content)

        val llocsRecycler = view.findViewById<RecyclerView>(R.id.rvCountryPlaces)
        llocsRecycler.layoutManager = LinearLayoutManager(requireContext())
        llocsRecycler.adapter = LlocsTuristicsAdapter(
            content.places.map { (title, description) -> LlocTuristic(title, description) }
        )

        view.findViewById<View>(R.id.btnBackCountryDetail).setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun configurarCarrusel(viewFlipper: ViewFlipper, content: CountryContent) {
        viewFlipper.removeAllViews()
        val slides = listOf(
            content.image1Res to content.caption1Res,
            content.image2Res to content.caption2Res,
            content.image3Res to content.caption3Res
        )
        slides.forEach { (imageRes, captionRes) ->
            val slide = layoutInflater.inflate(R.layout.item_country_carousel_slide, viewFlipper, false)
            slide.findViewById<ImageView>(R.id.ivCountrySlide).setImageResource(imageRes)
            slide.findViewById<TextView>(R.id.tvCountrySlideCaption).text = getString(captionRes)
            viewFlipper.addView(slide)
        }
        viewFlipper.flipInterval = 3000
        viewFlipper.isAutoStart = true
        viewFlipper.startFlipping()
    }

    private fun obtenirContingut(code: String): CountryContent? {
        return when (code) {
            COUNTRY_ESPANYA -> CountryContent(
                titleRes = R.string.country_title_espanya,
                subtitleRes = R.string.country_subtitle_espanya,
                image1Res = R.drawable.espanya_1,
                image2Res = R.drawable.espanya_2,
                image3Res = R.drawable.espanya_3,
                capitalRes = R.string.capital_madrid,
                populationRes = R.string.country_population_espanya,
                religionRes = R.string.country_religion_espanya,
                caption1Res = R.string.country_caption_espanya_1,
                caption2Res = R.string.country_caption_espanya_2,
                caption3Res = R.string.country_caption_espanya_3,
                places = listOf(
                    R.string.country_place_espanya_1_title to R.string.country_place_espanya_1_desc,
                    R.string.country_place_espanya_2_title to R.string.country_place_espanya_2_desc,
                    R.string.country_place_espanya_3_title to R.string.country_place_espanya_3_desc
                )
            )

            COUNTRY_FRANCA -> CountryContent(
                titleRes = R.string.country_title_franca,
                subtitleRes = R.string.country_subtitle_franca,
                image1Res = R.drawable.francia_1,
                image2Res = R.drawable.francia_2,
                image3Res = R.drawable.francia_3,
                capitalRes = R.string.capital_paris,
                populationRes = R.string.country_population_franca,
                religionRes = R.string.country_religion_franca,
                caption1Res = R.string.country_caption_franca_1,
                caption2Res = R.string.country_caption_franca_2,
                caption3Res = R.string.country_caption_franca_3,
                places = listOf(
                    R.string.country_place_franca_1_title to R.string.country_place_franca_1_desc,
                    R.string.country_place_franca_2_title to R.string.country_place_franca_2_desc,
                    R.string.country_place_franca_3_title to R.string.country_place_franca_3_desc
                )
            )

            COUNTRY_BELGICA -> CountryContent(
                titleRes = R.string.country_title_belgica,
                subtitleRes = R.string.country_subtitle_belgica,
                image1Res = R.drawable.belgica_1,
                image2Res = R.drawable.belgica_2,
                image3Res = R.drawable.belgica_3,
                capitalRes = R.string.capital_brusselles,
                populationRes = R.string.country_population_belgica,
                religionRes = R.string.country_religion_belgica,
                caption1Res = R.string.country_caption_belgica_1,
                caption2Res = R.string.country_caption_belgica_2,
                caption3Res = R.string.country_caption_belgica_3,
                places = listOf(
                    R.string.country_place_belgica_1_title to R.string.country_place_belgica_1_desc,
                    R.string.country_place_belgica_2_title to R.string.country_place_belgica_2_desc,
                    R.string.country_place_belgica_3_title to R.string.country_place_belgica_3_desc
                )
            )

            COUNTRY_MARROC -> CountryContent(
                titleRes = R.string.country_title_marroc,
                subtitleRes = R.string.country_subtitle_marroc,
                image1Res = R.drawable.marroc_1,
                image2Res = R.drawable.marroc_2,
                image3Res = R.drawable.marroc_3,
                capitalRes = R.string.capital_rabat,
                populationRes = R.string.country_population_marroc,
                religionRes = R.string.country_religion_marroc,
                caption1Res = R.string.country_caption_marroc_1,
                caption2Res = R.string.country_caption_marroc_2,
                caption3Res = R.string.country_caption_marroc_3,
                places = listOf(
                    R.string.country_place_marroc_1_title to R.string.country_place_marroc_1_desc,
                    R.string.country_place_marroc_2_title to R.string.country_place_marroc_2_desc,
                    R.string.country_place_marroc_3_title to R.string.country_place_marroc_3_desc
                )
            )

            COUNTRY_TURQUIA -> CountryContent(
                titleRes = R.string.country_title_turquia,
                subtitleRes = R.string.country_subtitle_turquia,
                image1Res = R.drawable.turkiye_1,
                image2Res = R.drawable.turkiye_2,
                image3Res = R.drawable.turkiye_3,
                capitalRes = R.string.capital_ankara,
                populationRes = R.string.country_population_turquia,
                religionRes = R.string.country_religion_turquia,
                caption1Res = R.string.country_caption_turquia_1,
                caption2Res = R.string.country_caption_turquia_2,
                caption3Res = R.string.country_caption_turquia_3,
                places = listOf(
                    R.string.country_place_turquia_1_title to R.string.country_place_turquia_1_desc,
                    R.string.country_place_turquia_2_title to R.string.country_place_turquia_2_desc,
                    R.string.country_place_turquia_3_title to R.string.country_place_turquia_3_desc
                )
            )

            else -> null
        }
    }

    data class LlocTuristic(
        val titleRes: Int,
        val descRes: Int
    )

    private data class CountryContent(
        val titleRes: Int,
        val subtitleRes: Int,
        val image1Res: Int,
        val image2Res: Int,
        val image3Res: Int,
        val capitalRes: Int,
        val populationRes: Int,
        val religionRes: Int,
        val caption1Res: Int,
        val caption2Res: Int,
        val caption3Res: Int,
        val places: List<Pair<Int, Int>>
    )

    companion object {
        const val COUNTRY_ESPANYA = "espanya"
        const val COUNTRY_FRANCA = "franca"
        const val COUNTRY_BELGICA = "belgica"
        const val COUNTRY_MARROC = "marroc"
        const val COUNTRY_TURQUIA = "turquia"

        private const val ARG_COUNTRY_CODE = "arg_country_code"

        fun newInstance(countryCode: String): CountryDetailFragment {
            return CountryDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_COUNTRY_CODE, countryCode)
                }
            }
        }
    }
}


