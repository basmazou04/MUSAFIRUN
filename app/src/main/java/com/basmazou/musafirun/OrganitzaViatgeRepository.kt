package com.basmazou.musafirun

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import org.json.JSONArray
import org.json.JSONObject

data class LlistaItem(
    val id: Int = 0,
    var text: String = "",
    var completat: Boolean = false
)

data class LlistaViatge(
    val id: Int = 0,
    var nom: String = "",
    val items: MutableList<LlistaItem> = mutableListOf()
)

data class ReservaViatge(
    val id: Int = 0,
    var tipus: String = "",
    var codi: String = "",
    var diaReserva: String = "",
    var dataInici: String = "",
    var dataFi: String = "",
    var preu: String = "",
    var pdfUrl: String? = null,
    var qrUrl: String? = null,
    var imatgeUrl: String? = null
)

data class GastoViatge(
    val id: Int = 0,
    var tipus: String = "",
    var importGasto: String = ""
)

data class LlocViatge(
    val id: Int = 0,
    var nom: String = "",
    var ubicacio: String = ""
)

data class OrganitzacioViatge(
    val id: Int = 0,
    var pais: String = "",
    var duracio: String = "",
    var persones: String = "",
    var pressupost: String = "",
    val albumFotos: MutableList<String> = mutableListOf(),
    val llistes: MutableList<LlistaViatge> = mutableListOf(),
    val reserves: MutableList<ReservaViatge> = mutableListOf(),
    val gastos: MutableList<GastoViatge> = mutableListOf(),
    val llocs: MutableList<LlocViatge> = mutableListOf()
)

object OrganitzaViatgeRepository {
    private const val CAMP_ORGANITZACIONS_JSON = "organitzacions_json"
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val organitzacions = mutableListOf<OrganitzacioViatge>()
    private val llistesEsborrany = mutableListOf<LlistaViatge>()
    private val reservesEsborrany = mutableListOf<ReservaViatge>()
    private val gastosEsborrany = mutableListOf<GastoViatge>()
    private val llocsEsborrany = mutableListOf<LlocViatge>()
    private val albumFotosEsborrany = mutableListOf<String>()
    private var nextOrganitzacioId = 1
    private var nextLlistaId = 1
    private var nextItemId = 1
    private var nextReservaId = 1
    private var nextGastoId = 1
    private var nextLlocId = 1

    fun iniciarNouEsborrany() {
        // Reinicia l'estat temporal del formulari quan es comença una organització nova.
        llistesEsborrany.clear()
        reservesEsborrany.clear()
        gastosEsborrany.clear()
        llocsEsborrany.clear()
        albumFotosEsborrany.clear()
    }

    fun obtenirOrganitzacions(): List<OrganitzacioViatge> = organitzacions.toList()

    fun obtenirOrganitzacio(organitzacioId: Int): OrganitzacioViatge? {
        return organitzacions.find { it.id == organitzacioId }
    }

    fun carregarOrganitzacions(
        context: Context,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        // Carrega una única estructura JSON del document d'usuari i la transforma a models locals.
        assegurarDocumentUsuari(
            context = context,
            onSuccess = {
                obtenirUserDocumentReference(context)
                    .get()
                    .addOnSuccessListener { document ->
                        organitzacions.clear()
                        val dades = document.data.orEmpty()
                        val organitzacionsJson = dades[CAMP_ORGANITZACIONS_JSON] as? String
                        organitzacions.addAll(jsonToOrganitzacions(organitzacionsJson))
                        sincronitzarComptadors()
                        onSuccess()
                    }
                    .addOnFailureListener(onError)
            },
            onError = onError
        )
    }

    fun guardarOrganitzacio(
        context: Context,
        pais: String,
        duracio: String,
        persones: String,
        pressupost: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        // Converteix l'esborrany actual en una organització immutable per desar-la al núvol.
        val organitzacio = OrganitzacioViatge(
            id = nextOrganitzacioId++,
            pais = pais,
            duracio = duracio,
            persones = persones,
            pressupost = pressupost,
            albumFotos = albumFotosEsborrany.toMutableList(),
            llistes = llistesEsborrany.map { it.deepCopy() }.toMutableList(),
            reserves = reservesEsborrany.map { it.deepCopy() }.toMutableList(),
            gastos = gastosEsborrany.map { it.deepCopy() }.toMutableList(),
            llocs = llocsEsborrany.map { it.deepCopy() }.toMutableList()
        )

        assegurarDocumentUsuari(
            context = context,
            onSuccess = {
                organitzacions.removeAll { it.id == organitzacio.id }
                organitzacions.add(organitzacio)
                persistirTotesLesOrganitzacions(
                    context = context,
                    onSuccess = {
                        iniciarNouEsborrany()
                        sincronitzarComptadors()
                        onSuccess()
                    },
                    onError = onError
                )
            },
            onError = onError
        )
    }

    fun persistirOrganitzacio(
        context: Context,
        organitzacioId: Int,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val organitzacio = obtenirOrganitzacio(organitzacioId)
        if (organitzacio == null) {
            onSuccess()
            return
        }

        persistirTotesLesOrganitzacions(
            context = context,
            onSuccess = onSuccess,
            onError = onError
        )
    }

    fun eliminarOrganitzacio(
        context: Context,
        organitzacioId: Int,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        assegurarDocumentUsuari(
            context = context,
            onSuccess = {
                organitzacions.removeAll { it.id == organitzacioId }
                persistirTotesLesOrganitzacions(
                    context = context,
                    onSuccess = onSuccess,
                    onError = onError
                )
            },
            onError = onError
        )
    }

    fun obtenirLlistes(organitzacioId: Int?): List<LlistaViatge> {
        return obtenirLlistesMutables(organitzacioId)?.toList().orEmpty()
    }

    fun obtenirLlista(organitzacioId: Int?, llistaId: Int): LlistaViatge? {
        return obtenirLlistesMutables(organitzacioId)?.find { it.id == llistaId }
    }

    fun crearLlista(organitzacioId: Int?, nom: String, items: List<String>) {
        val llista = LlistaViatge(
            id = nextLlistaId++,
            nom = nom,
            items = items.map { text ->
                LlistaItem(
                    id = nextItemId++,
                    text = text
                )
            }.toMutableList()
        )
        obtenirLlistesMutables(organitzacioId)?.add(llista)
    }

    fun eliminarLlista(organitzacioId: Int?, llistaId: Int) {
        obtenirLlistesMutables(organitzacioId)?.removeAll { it.id == llistaId }
    }

    fun afegirItem(organitzacioId: Int?, llistaId: Int, text: String) {
        obtenirLlista(organitzacioId, llistaId)?.items?.add(
            LlistaItem(
                id = nextItemId++,
                text = text
            )
        )
    }

    fun eliminarItem(organitzacioId: Int?, llistaId: Int, itemId: Int) {
        obtenirLlista(organitzacioId, llistaId)?.items?.removeAll { it.id == itemId }
    }

    fun actualitzarOrdre(organitzacioId: Int?, llistaId: Int) {
        val llista = obtenirLlista(organitzacioId, llistaId) ?: return
        val pendents = llista.items.filter { !it.completat }
        val completats = llista.items.filter { it.completat }
        llista.items.clear()
        llista.items.addAll(pendents + completats)
    }

    fun obtenirReserves(organitzacioId: Int?): List<ReservaViatge> {
        return obtenirReservesMutables(organitzacioId)?.toList().orEmpty()
    }

    fun obtenirReserva(organitzacioId: Int?, reservaId: Int): ReservaViatge? {
        return obtenirReservesMutables(organitzacioId)?.find { it.id == reservaId }
    }

    fun crearReserva(
        organitzacioId: Int?,
        tipus: String,
        codi: String,
        diaReserva: String,
        dataInici: String,
        dataFi: String,
        preu: String,
        pdfUrl: String?,
        qrUrl: String?,
        imatgeUrl: String?
    ) {
        val reserva = ReservaViatge(
            id = nextReservaId++,
            tipus = tipus,
            codi = codi,
            diaReserva = diaReserva,
            dataInici = dataInici,
            dataFi = dataFi,
            preu = preu,
            pdfUrl = pdfUrl,
            qrUrl = qrUrl,
            imatgeUrl = imatgeUrl
        )
        obtenirReservesMutables(organitzacioId)?.add(reserva)
    }

    fun actualitzarReserva(
        organitzacioId: Int?,
        reservaId: Int,
        tipus: String,
        codi: String,
        diaReserva: String,
        dataInici: String,
        dataFi: String,
        preu: String,
        pdfUrl: String?,
        qrUrl: String?,
        imatgeUrl: String?
    ) {
        val reserva = obtenirReserva(organitzacioId, reservaId) ?: return
        reserva.tipus = tipus
        reserva.codi = codi
        reserva.diaReserva = diaReserva
        reserva.dataInici = dataInici
        reserva.dataFi = dataFi
        reserva.preu = preu
        reserva.pdfUrl = pdfUrl
        reserva.qrUrl = qrUrl
        reserva.imatgeUrl = imatgeUrl
    }

    fun eliminarReserva(organitzacioId: Int?, reservaId: Int) {
        obtenirReservesMutables(organitzacioId)?.removeAll { it.id == reservaId }
    }

    fun obtenirGastos(organitzacioId: Int?): List<GastoViatge> {
        return obtenirGastosMutables(organitzacioId)?.toList().orEmpty()
    }

    fun obtenirGasto(organitzacioId: Int?, gastoId: Int): GastoViatge? {
        return obtenirGastosMutables(organitzacioId)?.find { it.id == gastoId }
    }

    fun crearGasto(
        organitzacioId: Int?,
        tipus: String,
        importGasto: String
    ) {
        val gasto = GastoViatge(
            id = nextGastoId++,
            tipus = tipus,
            importGasto = importGasto
        )
        obtenirGastosMutables(organitzacioId)?.add(gasto)
    }

    fun actualitzarGasto(
        organitzacioId: Int?,
        gastoId: Int,
        tipus: String,
        importGasto: String
    ) {
        val gasto = obtenirGasto(organitzacioId, gastoId) ?: return
        gasto.tipus = tipus
        gasto.importGasto = importGasto
    }

    fun eliminarGasto(organitzacioId: Int?, gastoId: Int) {
        obtenirGastosMutables(organitzacioId)?.removeAll { it.id == gastoId }
    }

    fun obtenirLlocs(organitzacioId: Int?): List<LlocViatge> {
        return obtenirLlocsMutables(organitzacioId)?.toList().orEmpty()
    }

    fun obtenirLloc(organitzacioId: Int?, llocId: Int): LlocViatge? {
        return obtenirLlocsMutables(organitzacioId)?.find { it.id == llocId }
    }

    fun crearLloc(
        organitzacioId: Int?,
        nom: String,
        ubicacio: String
    ) {
        val lloc = LlocViatge(
            id = nextLlocId++,
            nom = nom,
            ubicacio = ubicacio
        )
        obtenirLlocsMutables(organitzacioId)?.add(lloc)
    }

    fun actualitzarLloc(
        organitzacioId: Int?,
        llocId: Int,
        nom: String,
        ubicacio: String
    ) {
        val lloc = obtenirLloc(organitzacioId, llocId) ?: return
        lloc.nom = nom
        lloc.ubicacio = ubicacio
    }

    fun eliminarLloc(organitzacioId: Int?, llocId: Int) {
        obtenirLlocsMutables(organitzacioId)?.removeAll { it.id == llocId }
    }

    fun obtenirAlbumFotos(organitzacioId: Int?): List<String> {
        return obtenirAlbumFotosMutables(organitzacioId)?.toList().orEmpty()
    }

    fun afegirFotosAlbum(organitzacioId: Int?, fotos: List<String>) {
        if (fotos.isEmpty()) return
        val album = obtenirAlbumFotosMutables(organitzacioId) ?: return
        fotos.forEach { foto ->
            if (foto.isNotBlank() && !album.contains(foto)) {
                album.add(foto)
            }
        }
    }

    fun eliminarFotoAlbum(organitzacioId: Int?, fotoUri: String) {
        obtenirAlbumFotosMutables(organitzacioId)?.removeAll { it == fotoUri }
    }

    private fun obtenirUserDocumentReference(context: Context) =
        firestore.collection("usuaris")
            .document(UserSessionManager.getCurrentUserId(context))

    private fun assegurarDocumentUsuari(
        context: Context,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val userId = UserSessionManager.getCurrentUserId(context)
        obtenirUserDocumentReference(context)
            .set(
                hashMapOf(
                    "usuari" to userId
                ),
                SetOptions.merge()
            )
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener(onError)
    }

    private fun persistirTotesLesOrganitzacions(
        context: Context,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        obtenirUserDocumentReference(context)
            .set(
                hashMapOf(
                    CAMP_ORGANITZACIONS_JSON to organitzacionsToJson()
                ),
                SetOptions.merge()
            )
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener(onError)
    }

    private fun obtenirLlistesMutables(organitzacioId: Int?): MutableList<LlistaViatge>? {
        return if (organitzacioId == null) {
            llistesEsborrany
        } else {
            obtenirOrganitzacio(organitzacioId)?.llistes
        }
    }

    private fun obtenirReservesMutables(organitzacioId: Int?): MutableList<ReservaViatge>? {
        return if (organitzacioId == null) {
            reservesEsborrany
        } else {
            obtenirOrganitzacio(organitzacioId)?.reserves
        }
    }

    private fun obtenirGastosMutables(organitzacioId: Int?): MutableList<GastoViatge>? {
        return if (organitzacioId == null) {
            gastosEsborrany
        } else {
            obtenirOrganitzacio(organitzacioId)?.gastos
        }
    }

    private fun obtenirLlocsMutables(organitzacioId: Int?): MutableList<LlocViatge>? {
        return if (organitzacioId == null) {
            llocsEsborrany
        } else {
            obtenirOrganitzacio(organitzacioId)?.llocs
        }
    }

    private fun obtenirAlbumFotosMutables(organitzacioId: Int?): MutableList<String>? {
        return if (organitzacioId == null) {
            albumFotosEsborrany
        } else {
            obtenirOrganitzacio(organitzacioId)?.albumFotos
        }
    }

    private fun sincronitzarComptadors() {
        nextOrganitzacioId = (organitzacions.maxOfOrNull { it.id } ?: 0) + 1
        val totesLesLlistes = organitzacions.flatMap { it.llistes } + llistesEsborrany
        nextLlistaId = (totesLesLlistes.maxOfOrNull { it.id } ?: 0) + 1
        val totsElsItems = totesLesLlistes.flatMap { it.items }
        nextItemId = (totsElsItems.maxOfOrNull { it.id } ?: 0) + 1
        val totesLesReserves = organitzacions.flatMap { it.reserves } + reservesEsborrany
        nextReservaId = (totesLesReserves.maxOfOrNull { it.id } ?: 0) + 1
        val totsElsGastos = organitzacions.flatMap { it.gastos } + gastosEsborrany
        nextGastoId = (totsElsGastos.maxOfOrNull { it.id } ?: 0) + 1
        val totsElsLlocs = organitzacions.flatMap { it.llocs } + llocsEsborrany
        nextLlocId = (totsElsLlocs.maxOfOrNull { it.id } ?: 0) + 1
    }

    private fun organitzacionsToJson(): String {
        val organitzacionsArray = JSONArray()
        organitzacions.forEach { organitzacio ->
            val organitzacioObject = JSONObject().apply {
                put("id", organitzacio.id)
                put("pais", organitzacio.pais)
                put("duracio", organitzacio.duracio)
                put("persones", organitzacio.persones)
                put("pressupost", organitzacio.pressupost)
                put("albumFotos", JSONArray(organitzacio.albumFotos))

                val llistesArray = JSONArray()
                organitzacio.llistes.forEach { llista ->
                    val llistaObject = JSONObject().apply {
                        put("id", llista.id)
                        put("nom", llista.nom)

                        val itemsArray = JSONArray()
                        llista.items.forEach { item ->
                            itemsArray.put(
                                JSONObject().apply {
                                    put("id", item.id)
                                    put("text", item.text)
                                    put("completat", item.completat)
                                }
                            )
                        }
                        put("items", itemsArray)
                    }
                    llistesArray.put(llistaObject)
                }
                put("llistes", llistesArray)

                val reservesArray = JSONArray()
                organitzacio.reserves.forEach { reserva ->
                    reservesArray.put(
                        JSONObject().apply {
                            put("id", reserva.id)
                            put("tipus", reserva.tipus)
                            put("codi", reserva.codi)
                            put("diaReserva", reserva.diaReserva)
                            put("dataInici", reserva.dataInici)
                            put("dataFi", reserva.dataFi)
                            put("preu", reserva.preu)
                            put("pdfUrl", reserva.pdfUrl)
                            put("qrUrl", reserva.qrUrl)
                            put("imatgeUrl", reserva.imatgeUrl)
                        }
                    )
                }
                put("reserves", reservesArray)

                val gastosArray = JSONArray()
                organitzacio.gastos.forEach { gasto ->
                    gastosArray.put(
                        JSONObject().apply {
                            put("id", gasto.id)
                            put("tipus", gasto.tipus)
                            put("import", gasto.importGasto)
                        }
                    )
                }
                put("gastos", gastosArray)

                val llocsArray = JSONArray()
                organitzacio.llocs.forEach { lloc ->
                    llocsArray.put(
                        JSONObject().apply {
                            put("id", lloc.id)
                            put("nom", lloc.nom)
                            put("ubicacio", lloc.ubicacio)
                        }
                    )
                }
                put("llocs", llocsArray)
            }
            organitzacionsArray.put(organitzacioObject)
        }
        return organitzacionsArray.toString()
    }

    private fun jsonToOrganitzacions(json: String?): List<OrganitzacioViatge> {
        if (json.isNullOrBlank()) return emptyList()

        val resultat = mutableListOf<OrganitzacioViatge>()
        val organitzacionsArray = JSONArray(json)

        for (i in 0 until organitzacionsArray.length()) {
            val organitzacioObject = organitzacionsArray.getJSONObject(i)
            val llistesArray = organitzacioObject.optJSONArray("llistes") ?: JSONArray()
            val reservesArray = organitzacioObject.optJSONArray("reserves") ?: JSONArray()
            val gastosArray = organitzacioObject.optJSONArray("gastos") ?: JSONArray()
            val llocsArray = organitzacioObject.optJSONArray("llocs") ?: JSONArray()
            val albumFotosArray = organitzacioObject.optJSONArray("albumFotos") ?: JSONArray()
            val llistes = mutableListOf<LlistaViatge>()
            val reserves = mutableListOf<ReservaViatge>()
            val gastos = mutableListOf<GastoViatge>()
            val llocs = mutableListOf<LlocViatge>()
            val albumFotos = mutableListOf<String>()

            for (j in 0 until llistesArray.length()) {
                val llistaObject = llistesArray.getJSONObject(j)
                val itemsArray = llistaObject.optJSONArray("items") ?: JSONArray()
                val items = mutableListOf<LlistaItem>()

                for (k in 0 until itemsArray.length()) {
                    val itemObject = itemsArray.getJSONObject(k)
                    items.add(
                        LlistaItem(
                            id = itemObject.optInt("id"),
                            text = itemObject.optString("text"),
                            completat = itemObject.optBoolean("completat")
                        )
                    )
                }

                llistes.add(
                    LlistaViatge(
                        id = llistaObject.optInt("id"),
                        nom = llistaObject.optString("nom"),
                        items = items
                    )
                )
            }

            for (j in 0 until reservesArray.length()) {
                val reservaObject = reservesArray.getJSONObject(j)
                val pdfUrlRaw = reservaObject.optString("pdfUrl", "")
                val pdfUrlNormalitzada = pdfUrlRaw
                    .takeIf { it.isNotBlank() && it != "null" }
                val qrUrlRaw = reservaObject.optString("qrUrl", "")
                val qrUrlNormalitzada = qrUrlRaw
                    .takeIf { it.isNotBlank() && it != "null" }
                val imatgeUrlRaw = reservaObject.optString("imatgeUrl", "")
                val imatgeUrlNormalitzada = imatgeUrlRaw
                    .takeIf { it.isNotBlank() && it != "null" }
                reserves.add(
                    ReservaViatge(
                        id = reservaObject.optInt("id"),
                        tipus = reservaObject.optString("tipus"),
                        codi = reservaObject.optString("codi"),
                        diaReserva = reservaObject.optString("diaReserva"),
                        dataInici = reservaObject.optString("dataInici"),
                        dataFi = reservaObject.optString("dataFi"),
                        preu = reservaObject.optString("preu"),
                        pdfUrl = pdfUrlNormalitzada,
                        qrUrl = qrUrlNormalitzada,
                        imatgeUrl = imatgeUrlNormalitzada
                    )
                )
            }

            for (j in 0 until gastosArray.length()) {
                val gastoObject = gastosArray.getJSONObject(j)
                gastos.add(
                    GastoViatge(
                        id = gastoObject.optInt("id"),
                        tipus = gastoObject.optString("tipus"),
                        importGasto = gastoObject.optString("import")
                    )
                )
            }

            for (j in 0 until llocsArray.length()) {
                val llocObject = llocsArray.getJSONObject(j)
                llocs.add(
                    LlocViatge(
                        id = llocObject.optInt("id"),
                        nom = llocObject.optString("nom"),
                        ubicacio = llocObject.optString("ubicacio")
                    )
                )
            }

            for (j in 0 until albumFotosArray.length()) {
                albumFotos.add(albumFotosArray.optString(j))
            }

            resultat.add(
                OrganitzacioViatge(
                    id = organitzacioObject.optInt("id"),
                    pais = organitzacioObject.optString("pais"),
                    duracio = organitzacioObject.optString("duracio"),
                    persones = organitzacioObject.optString("persones"),
                    pressupost = organitzacioObject.optString("pressupost"),
                    albumFotos = albumFotos,
                    llistes = llistes,
                    reserves = reserves,
                    gastos = gastos,
                    llocs = llocs
                )
            )
        }

        return resultat
    }

    private fun LlistaViatge.deepCopy(): LlistaViatge {
        return LlistaViatge(
            id = id,
            nom = nom,
            items = items.map { item ->
                LlistaItem(
                    id = item.id,
                    text = item.text,
                    completat = item.completat
                )
            }.toMutableList()
        )
    }

    private fun ReservaViatge.deepCopy(): ReservaViatge {
        return ReservaViatge(
            id = id,
            tipus = tipus,
            codi = codi,
            diaReserva = diaReserva,
            dataInici = dataInici,
            dataFi = dataFi,
            preu = preu,
            pdfUrl = pdfUrl,
            qrUrl = qrUrl,
            imatgeUrl = imatgeUrl
        )
    }

    private fun GastoViatge.deepCopy(): GastoViatge {
        return GastoViatge(
            id = id,
            tipus = tipus,
            importGasto = importGasto
        )
    }

    private fun LlocViatge.deepCopy(): LlocViatge {
        return LlocViatge(
            id = id,
            nom = nom,
            ubicacio = ubicacio
        )
    }
}


