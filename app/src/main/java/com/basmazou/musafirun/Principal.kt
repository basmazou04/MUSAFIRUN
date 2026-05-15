package com.basmazou.musafirun

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.google.android.material.chip.ChipGroup
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors

class Principal : Fragment() {

    data class Noticia(
        val titol: String,
        val enllac: String,
        val urlImatge: String?,
        val font: String?,
        val snippet: String?,
        val pubDateMillis: Long?
    )

    private val executorXarxa = Executors.newSingleThreadExecutor()
    private val gestorPrincipal = Handler(Looper.getMainLooper())

    private var llistaNoticies: List<Noticia> = emptyList()
    private var indexCarrusel = 0
    private var carruselActiu = false
    private var carruselPausatPerUsuari = false
    private var idiomaActual = "ca"
    private var favoritsCache: MutableSet<String> = mutableSetOf()

    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var chipGroupIdiomes: ChipGroup
    private lateinit var textTitolNoticia: TextView
    private lateinit var textEstatNoticies: TextView
    private lateinit var textPistaNoticies: TextView
    private lateinit var textMetaNoticia: TextView
    private lateinit var textSnippetNoticia: TextView
    private lateinit var imatgePreviaNoticia: ImageView
    private lateinit var botoPauseResume: ImageView
    private lateinit var botoCompartir: ImageView
    private lateinit var botoFavorit: ImageView
    private lateinit var badgeOffline: TextView
    private lateinit var textGreeting: TextView

    private val runnableCarrusel = object : Runnable {
        override fun run() {
            if (!isAdded || llistaNoticies.isEmpty()) return
            indexCarrusel = (indexCarrusel + 1) % llistaNoticies.size
            mostrarNoticiaActual()
            gestorPrincipal.postDelayed(this, 4000)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        estatInstanciaGuardat: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_principal, container, false)
    }

    override fun onViewCreated(view: View, estatInstanciaGuardat: Bundle?) {
        super.onViewCreated(view, estatInstanciaGuardat)

        swipeRefresh = view.findViewById(R.id.swipeRefreshPrincipal)
        chipGroupIdiomes = view.findViewById(R.id.chipsNewsLanguage)
        textTitolNoticia = view.findViewById(R.id.tvNewsTitle)
        textEstatNoticies = view.findViewById(R.id.tvNewsStatus)
        textPistaNoticies = view.findViewById(R.id.tvNewsHint)
        textMetaNoticia = view.findViewById(R.id.tvNewsMeta)
        textSnippetNoticia = view.findViewById(R.id.tvNewsSnippet)
        imatgePreviaNoticia = view.findViewById(R.id.ivNewsPreview)
        botoPauseResume = view.findViewById(R.id.btnPauseResume)
        botoCompartir = view.findViewById(R.id.btnNewsShare)
        botoFavorit = view.findViewById(R.id.btnNewsFavorite)
        badgeOffline = view.findViewById(R.id.tvOfflineBadge)
        textGreeting = view.findViewById(R.id.tvGreeting)

        configurarSaludo()
        configurarPaisosDestacats(view)
        configurarChipsIdioma()
        configurarSwipeRefresh()
        configurarBotoPausa()
        configurarAccionsNoticia()
        configurarEnllacosAgencies(view)

        carregarNoticies(idiomaActual)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        aturarCarrusel()
    }

    private fun configurarSaludo() {
        val ctx = requireContext()
        val rawId = UserSessionManager.getCurrentUserId(ctx)
        val nomVisible = if (rawId.startsWith("guest_")) {
            getString(R.string.greeting_guest_name)
        } else {
            rawId
        }
        textGreeting.text = getString(R.string.greeting_morning_user, nomVisible)
    }

    private fun configurarPaisosDestacats(view: View) {
        val rv = view.findViewById<RecyclerView>(R.id.rvPaisosDestacats)
        rv.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        val paisos = listOf(
            Pais(CountryDetailFragment.COUNTRY_ESPANYA, getString(R.string.pais_espanya), getString(R.string.capital_madrid), R.drawable.espanya),
            Pais(CountryDetailFragment.COUNTRY_FRANCA, getString(R.string.pais_franca), getString(R.string.capital_paris), R.drawable.francia),
            Pais(CountryDetailFragment.COUNTRY_BELGICA, getString(R.string.pais_belgica), getString(R.string.capital_brusselles), R.drawable.belgica),
            Pais(CountryDetailFragment.COUNTRY_MARROC, getString(R.string.pais_marroc), getString(R.string.capital_rabat), R.drawable.marruecos),
            Pais(CountryDetailFragment.COUNTRY_TURQUIA, getString(R.string.pais_turquia), getString(R.string.capital_ankara), R.drawable.turquia)
        )

        rv.adapter = PaisDestacatAdapter(paisos) { pais ->
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, CountryDetailFragment.newInstance(pais.code))
                .addToBackStack(null)
                .commit()
        }
    }

    private fun configurarChipsIdioma() {
        chipGroupIdiomes.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener
            val nouIdioma = when (checkedIds.first()) {
                R.id.chipLangEs -> "es"
                R.id.chipLangEn -> "en"
                else -> "ca"
            }
            if (nouIdioma != idiomaActual) {
                idiomaActual = nouIdioma
                carregarNoticies(idiomaActual)
            }
        }
    }
    
    private fun configurarSwipeRefresh() {
        swipeRefresh.setOnRefreshListener {
            carregarNoticies(idiomaActual)
        }
    }

    private fun configurarBotoPausa() {
        botoPauseResume.setOnClickListener {
            if (carruselActiu) {
                aturarCarrusel()
                carruselPausatPerUsuari = true
                botoPauseResume.setImageResource(R.drawable.ic_play)
                botoPauseResume.contentDescription = getString(R.string.news_play_desc)
            } else if (llistaNoticies.isNotEmpty()) {
                carruselPausatPerUsuari = false
                iniciarCarrusel()
                botoPauseResume.setImageResource(R.drawable.ic_pause)
                botoPauseResume.contentDescription = getString(R.string.news_pause_desc)
            }
        }
    }

    private fun configurarAccionsNoticia() {
        botoCompartir.setOnClickListener {
            mostrarMissatgePremium()
        }
        botoFavorit.setOnClickListener {
            mostrarMissatgePremium()
        }
    }

    private fun actualitzarIconaFavorit(esFavorit: Boolean) {
        botoFavorit.setImageResource(
            if (esFavorit) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline
        )
        botoFavorit.contentDescription = getString(
            if (esFavorit) R.string.news_unfavorite_desc else R.string.news_favorite_desc
        )
    }

    private fun mostrarMissatgePremium() {
        Toast.makeText(requireContext(), R.string.premium_feature_soon, Toast.LENGTH_LONG).show()
    }

    private fun carregarNoticies(codiIdioma: String) {
        // Reinicia l'estat visual abans d'una nova càrrega per evitar dades antigues a pantalla.
        aturarCarrusel()
        textTitolNoticia.text = ""
        textMetaNoticia.text = ""
        textSnippetNoticia.text = ""
        textEstatNoticies.text = getString(R.string.news_loading)
        textPistaNoticies.visibility = View.GONE
        imatgePreviaNoticia.visibility = View.INVISIBLE
        swipeRefresh.isRefreshing = true

        val ctx = requireContext()
        val hiHaXarxa = hiHaConnexio(ctx)

        if (!hiHaXarxa) {
            // Sense connexió, es mostra directament l'última còpia local disponible.
            val cached = NewsCacheStore.carregar(ctx, codiIdioma)
            gestorPrincipal.post { aplicarResultat(cached, codiIdioma, desDeMemoria = true, foraLinia = true) }
            return
        }

        executorXarxa.execute {
            val noticiesCarregades = obtenirRssGoogleNews(codiIdioma)
            gestorPrincipal.post {
                if (!isAdded) return@post
                if (noticiesCarregades.isEmpty()) {
                    // Si la xarxa respon buit o falla el parseig, fem fallback a memòria cau.
                    val cached = NewsCacheStore.carregar(ctx, codiIdioma)
                    aplicarResultat(cached, codiIdioma, desDeMemoria = true, foraLinia = false)
                } else {
                    NewsCacheStore.desar(ctx, codiIdioma, noticiesCarregades)
                    aplicarResultat(noticiesCarregades, codiIdioma, desDeMemoria = false, foraLinia = false)
                }
            }
        }
    }

    private fun aplicarResultat(items: List<Noticia>, codiIdioma: String, desDeMemoria: Boolean, foraLinia: Boolean) {
        swipeRefresh.isRefreshing = false
        llistaNoticies = items
        indexCarrusel = 0
        badgeOffline.visibility = if (foraLinia && items.isNotEmpty()) View.VISIBLE else View.GONE

        if (items.isEmpty()) {
            textEstatNoticies.text =
                if (foraLinia) getString(R.string.news_no_internet)
                else getString(R.string.news_empty)
            textTitolNoticia.text = ""
            textMetaNoticia.text = ""
            textSnippetNoticia.text = ""
            textPistaNoticies.visibility = View.GONE
            imatgePreviaNoticia.visibility = View.INVISIBLE
            Glide.with(this).clear(imatgePreviaNoticia)
            return
        }

        textEstatNoticies.text =
            if (foraLinia) getString(R.string.news_offline_hint) else ""
        mostrarNoticiaActual()
        textPistaNoticies.visibility = View.VISIBLE
        if (!carruselPausatPerUsuari) iniciarCarrusel()
    }

    private fun hiHaConnexio(ctx: Context): Boolean {
        val cm = ContextCompat.getSystemService(ctx, ConnectivityManager::class.java) ?: return false
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    private fun obtenirRssGoogleNews(codiIdioma: String): List<Noticia> {
        val codiPais = when (codiIdioma) {
            "es" -> "ES"
            "en" -> "US"
            else -> "ES"
        }
        val consulta = Uri.encode("travel OR visas OR countries")
        val urlRss =
            "https://news.google.com/rss/search?q=$consulta&hl=$codiIdioma&gl=$codiPais&ceid=$codiPais:$codiIdioma"

        val connexio = (URL(urlRss).openConnection() as HttpURLConnection).apply {
            connectTimeout = 12000
            readTimeout = 12000
            requestMethod = "GET"
        }

        return try {
            connexio.inputStream.use { fluxEntrada ->
                val parserXml = XmlPullParserFactory.newInstance().newPullParser()
                parserXml.setInput(fluxEntrada, "UTF-8")
                parsejarRss(parserXml).take(12)
            }
        } catch (_: Exception) {
            emptyList()
        } finally {
            connexio.disconnect()
        }
    }

    private fun parsejarRss(parserXml: XmlPullParser): List<Noticia> {
        // Parseig manual del RSS per extreure només els camps útils per al carrusel.
        val noticies = mutableListOf<Noticia>()
        var tipusEsdeveniment = parserXml.eventType
        var dinsItem = false
        var titolActual: String? = null
        var enllacActual: String? = null
        var descripcioActual: String? = null
        var urlImatgeActual: String? = null
        var fontActual: String? = null
        var urlFontActual: String? = null
        var pubDateActual: String? = null

        while (tipusEsdeveniment != XmlPullParser.END_DOCUMENT) {
            when (tipusEsdeveniment) {
                XmlPullParser.START_TAG -> {
                    val nomEtiqueta = parserXml.name.lowercase()
                    val prefixEtiqueta = parserXml.prefix?.lowercase()

                    when (parserXml.name) {
                        "item" -> dinsItem = true
                        "title" -> if (dinsItem) titolActual = parserXml.nextText()
                        "link" -> if (dinsItem) enllacActual = parserXml.nextText()
                        "description" -> if (dinsItem) descripcioActual = parserXml.nextText()
                        "source" -> if (dinsItem) {
                            urlFontActual = extreureAtribut(parserXml, "url")
                            fontActual = parserXml.nextText()
                        }
                        "pubDate" -> if (dinsItem) pubDateActual = parserXml.nextText()
                    }

                    if (dinsItem && urlImatgeActual.isNullOrBlank()) {
                        val etiquetaMediaImatge = prefixEtiqueta == "media" &&
                            (nomEtiqueta == "content" || nomEtiqueta == "thumbnail")
                        val enclosureImatge = nomEtiqueta == "enclosure" &&
                            (parserXml.getAttributeValue(null, "type")?.startsWith("image") == true)
                        if (etiquetaMediaImatge || enclosureImatge) {
                            urlImatgeActual = normalitzarUrlImatge(extreureAtribut(parserXml, "url"))
                        }
                    }
                }

                XmlPullParser.END_TAG -> {
                    if (parserXml.name == "item") {
                        dinsItem = false
                        if (!titolActual.isNullOrBlank() && !enllacActual.isNullOrBlank()) {
                            val urlImatgeFinal = urlImatgeActual
                                ?: extreureUrlImatgeDeDescripcio(descripcioActual)
                                ?: construirFallbackImatge(urlFontActual)
                            val fontDerivada = fontActual ?: extreureFontDelTitol(titolActual!!)
                            val titolNet = netejarTitol(titolActual!!, fontDerivada)
                            val snippetNet = extreureSnippet(descripcioActual)
                            noticies.add(
                                Noticia(
                                    titol = titolNet,
                                    enllac = enllacActual!!,
                                    urlImatge = urlImatgeFinal,
                                    font = fontDerivada,
                                    snippet = snippetNet,
                                    pubDateMillis = parsejarPubDate(pubDateActual)
                                )
                            )
                        }

                        titolActual = null
                        enllacActual = null
                        descripcioActual = null
                        urlImatgeActual = null
                        fontActual = null
                        urlFontActual = null
                        pubDateActual = null
                    }
                }
            }

            tipusEsdeveniment = parserXml.next()
        }

        return noticies
    }

    private fun extreureFontDelTitol(titol: String): String? {
        val idx = titol.lastIndexOf(" - ")
        return if (idx > 0 && idx < titol.length - 3) titol.substring(idx + 3).trim() else null
    }

    private fun netejarTitol(titol: String, font: String?): String {
        if (font.isNullOrBlank()) return titol
        val sufix = " - $font"
        return if (titol.endsWith(sufix)) titol.removeSuffix(sufix).trim() else titol
    }

    private fun extreureSnippet(descripcio: String?): String? {
        if (descripcio.isNullOrBlank()) return null
        val sensHtml = descripcio
            .replace(Regex("<[^>]+>"), " ")
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace(Regex("\\s+"), " ")
            .trim()
        return sensHtml.takeIf { it.isNotBlank() }
    }

    private fun parsejarPubDate(pubDate: String?): Long? {
        if (pubDate.isNullOrBlank()) return null
        return try {
            val sdf = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH)
            sdf.parse(pubDate)?.time
        } catch (_: Exception) {
            null
        }
    }

    private fun mostrarNoticiaActual() {
        val noticiaActual = llistaNoticies.getOrNull(indexCarrusel) ?: return
        textTitolNoticia.text = noticiaActual.titol
        imatgePreviaNoticia.contentDescription = getString(R.string.news_image_for, noticiaActual.titol)

        val parts = mutableListOf<String>()
        formatarTempsRelatiu(noticiaActual.pubDateMillis)?.let { parts.add(it) }
        noticiaActual.font?.takeIf { it.isNotBlank() }?.let { parts.add(it) }
        textMetaNoticia.text = parts.joinToString(" ${getString(R.string.news_meta_separator)} ")
        textMetaNoticia.visibility = if (parts.isEmpty()) View.GONE else View.VISIBLE

        val snippet = noticiaActual.snippet
        if (snippet.isNullOrBlank()) {
            textSnippetNoticia.visibility = View.GONE
        } else {
            textSnippetNoticia.visibility = View.VISIBLE
            textSnippetNoticia.text = snippet
        }

        imatgePreviaNoticia.visibility = View.VISIBLE
        if (noticiaActual.urlImatge.isNullOrBlank()) {
            // Evita que Glide reaprofiti una imatge anterior quan aquesta notícia no en té.
            Glide.with(this).clear(imatgePreviaNoticia)
            imatgePreviaNoticia.setImageDrawable(null)
        } else {
            Glide.with(this)
                .load(noticiaActual.urlImatge)
                .centerCrop()
                .into(imatgePreviaNoticia)
        }

        val obrirEnllac = View.OnClickListener { obrirNoticia(noticiaActual.enllac) }
        imatgePreviaNoticia.setOnClickListener(obrirEnllac)
        textTitolNoticia.setOnClickListener(obrirEnllac)
        textSnippetNoticia.setOnClickListener(obrirEnllac)

        actualitzarIconaFavorit(favoritsCache.contains(noticiaActual.enllac))
    }

    private fun formatarTempsRelatiu(millis: Long?): String? {
        if (millis == null || millis <= 0L) return null
        val diff = System.currentTimeMillis() - millis
        if (diff < 0) return getString(R.string.news_relative_now)
        val mins = diff / 60_000L
        return when {
            mins < 1 -> getString(R.string.news_relative_now)
            mins < 60 -> getString(R.string.news_relative_minutes, mins.toInt())
            mins < 60 * 24 -> getString(R.string.news_relative_hours, (mins / 60).toInt())
            else -> getString(R.string.news_relative_days, (mins / (60 * 24)).toInt())
        }
    }

    private fun obrirNoticia(url: String) {
        try {
            val intent = CustomTabsIntent.Builder()
                .setShowTitle(true)
                .build()
            intent.launchUrl(requireContext(), Uri.parse(url))
        } catch (_: Exception) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    }

    private fun extreureUrlImatgeDeDescripcio(descripcio: String?): String? {
        if (descripcio.isNullOrBlank()) return null
        val patroImg = Regex("""<img[^>]+src=[\"']([^\"']+)[\"']""", RegexOption.IGNORE_CASE)
        val urlDirecta = patroImg.find(descripcio)?.groupValues?.getOrNull(1)
        if (!urlDirecta.isNullOrBlank()) return normalitzarUrlImatge(urlDirecta)
        val htmlDecodificat = descripcio
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&amp;", "&")
        val urlDecodificada = patroImg.find(htmlDecodificat)?.groupValues?.getOrNull(1)
        return normalitzarUrlImatge(urlDecodificada)
    }

    private fun normalitzarUrlImatge(url: String?): String? {
        if (url.isNullOrBlank()) return null
        val neta = url.trim()
        return when {
            neta.startsWith("//") -> "https:$neta"
            neta.startsWith("http://") || neta.startsWith("https://") -> neta
            else -> null
        }
    }

    private fun extreureAtribut(parserXml: XmlPullParser, nomAtribut: String): String? {
        for (i in 0 until parserXml.attributeCount) {
            if (parserXml.getAttributeName(i).equals(nomAtribut, ignoreCase = true)) {
                return parserXml.getAttributeValue(i)
            }
        }
        return null
    }

    private fun construirFallbackImatge(urlFont: String?): String? {
        val urlNormalitzada = normalitzarUrlImatge(urlFont) ?: return null
        return try {
            val host = URI(urlNormalitzada).host?.removePrefix("www.")?.trim().orEmpty()
            if (host.isBlank()) null else "https://www.google.com/s2/favicons?domain=$host&sz=256"
        } catch (_: Exception) {
            null
        }
    }

    private fun iniciarCarrusel() {
        if (llistaNoticies.size <= 1) return
        carruselActiu = true
        botoPauseResume.setImageResource(R.drawable.ic_pause)
        botoPauseResume.contentDescription = getString(R.string.news_pause_desc)
        gestorPrincipal.postDelayed(runnableCarrusel, 4000)
    }

    private fun aturarCarrusel() {
        carruselActiu = false
        gestorPrincipal.removeCallbacks(runnableCarrusel)
    }

    private fun configurarEnllacosAgencies(view: View) {
        view.findViewById<ImageView>(R.id.ivAgency1Web).setOnClickListener { obrirNoticia("https://radilust.com/") }
        view.findViewById<ImageView>(R.id.ivAgency1Instagram).setOnClickListener { obrirNoticia("https://www.instagram.com/radilustexperience/") }
        view.findViewById<ImageView>(R.id.ivAgency2Web).setOnClickListener { obrirNoticia("https://bintbatutatravel.com/") }
        view.findViewById<ImageView>(R.id.ivAgency2Instagram).setOnClickListener { obrirNoticia("https://www.instagram.com/bintbatutatravel/") }
        view.findViewById<ImageView>(R.id.ivAgency3Web).setOnClickListener { obrirNoticia("https://ojodenomada.com/") }
        view.findViewById<ImageView>(R.id.ivAgency3Instagram).setOnClickListener { obrirNoticia("https://www.instagram.com/ojodenomada/") }
    }
}



