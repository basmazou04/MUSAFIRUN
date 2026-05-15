package com.basmazou.musafirun

import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.os.Build
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.OpenableColumns
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import java.io.File
import java.io.FileOutputStream

class EditarReservaFragment : Fragment() {

    private var pdfUri: Uri? = null
    private var qrUri: Uri? = null
    private var imatgeUri: Uri? = null
    private var pdfUrl: String? = null
    private var qrUrl: String? = null
    private var imatgeUrl: String? = null

    private val pdfSelector =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                pdfUri = uri
                persistirPermisLectura(uri)
                actualitzarPreviewPdf()
            }
        }

    private val qrSelector =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                qrUri = uri
                persistirPermisLectura(uri)
                actualitzarPreviewQr()
            }
        }

    private val imatgeSelector =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                imatgeUri = uri
                persistirPermisLectura(uri)
                actualitzarPreviewImatge()
            }
        }

    private lateinit var cardPdfPreview: View
    private lateinit var tvPdfPreviewNom: TextView
    private lateinit var btnDescarregarPdf: View
    private lateinit var cardQrPreview: View
    private lateinit var ivQrPreview: ImageView
    private lateinit var cardImatgePreview: View
    private lateinit var ivImatgePreview: ImageView
    private lateinit var guardarButton: View
    private var descarregaEnCurs = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        estatInstanciaGuardat: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_editar_reserva_viatge, container, false)
    }

    override fun onViewCreated(view: View, estatInstanciaGuardat: Bundle?) {
        super.onViewCreated(view, estatInstanciaGuardat)

        val organitzacioId = arguments?.getInt(ARG_ORGANITZACIO_ID)?.takeIf { it != NO_ID }
        val reservaId = arguments?.getInt(ARG_RESERVA_ID)?.takeIf { it != NO_ID }
        val reserva = reservaId?.let { OrganitzaViatgeRepository.obtenirReserva(organitzacioId, it) }

        val textTitol = view.findViewById<TextView>(R.id.tvEditarReservaTitle)
        val subtitleText = view.findViewById<TextView>(R.id.tvEditarReservaSubtitle)
        guardarButton = view.findViewById(R.id.btnGuardarReserva)

        val tipusEditText = view.findViewById<EditText>(R.id.etTipusReserva)
        val codiEditText = view.findViewById<EditText>(R.id.etCodiReserva)
        val diaEditText = view.findViewById<EditText>(R.id.etDiaReserva)
        val iniciEditText = view.findViewById<EditText>(R.id.etDataIniciReserva)
        val fiEditText = view.findViewById<EditText>(R.id.etDataFiReserva)
        val preuEditText = view.findViewById<EditText>(R.id.etPreuReserva)

        val btnSeleccionarPdf = view.findViewById<View>(R.id.btnSeleccionarPdf)
        val btnSeleccionarQr = view.findViewById<View>(R.id.btnSeleccionarQr)
        val btnSeleccionarImatge = view.findViewById<View>(R.id.btnSeleccionarImatge)

        cardPdfPreview = view.findViewById(R.id.cardPdfPreview)
        tvPdfPreviewNom = view.findViewById(R.id.tvPdfPreviewNom)
        btnDescarregarPdf = view.findViewById(R.id.btnDescarregarPdf)
        cardQrPreview = view.findViewById(R.id.cardQrPreview)
        ivQrPreview = view.findViewById(R.id.ivQrPreview)
        cardImatgePreview = view.findViewById(R.id.cardImatgePreview)
        ivImatgePreview = view.findViewById(R.id.ivImatgePreview)

        btnSeleccionarPdf.setOnClickListener { pdfSelector.launch(arrayOf("application/pdf")) }
        btnSeleccionarQr.setOnClickListener { qrSelector.launch(arrayOf("image/*")) }
        btnSeleccionarImatge.setOnClickListener { imatgeSelector.launch(arrayOf("image/*")) }

        btnDescarregarPdf.setOnClickListener { descarregarPdf() }
        cardQrPreview.setOnClickListener { visualitzarImatge(qrUri?.toString() ?: qrUrl) }
        cardImatgePreview.setOnClickListener { visualitzarImatge(imatgeUri?.toString() ?: imatgeUrl) }

        if (reserva != null) {
            textTitol.text = getString(R.string.editar_reserva_titol)
            subtitleText.text = getString(R.string.editar_reserva_subtitol)

            tipusEditText.setText(reserva.tipus)
            codiEditText.setText(reserva.codi)
            diaEditText.setText(reserva.diaReserva)
            iniciEditText.setText(reserva.dataInici)
            fiEditText.setText(reserva.dataFi)
            preuEditText.setText(reserva.preu)
            pdfUrl = reserva.pdfUrl
            qrUrl = reserva.qrUrl
            imatgeUrl = reserva.imatgeUrl
        }

        actualitzarPreviewPdf()
        actualitzarPreviewQr()
        actualitzarPreviewImatge()

        guardarButton.setOnClickListener {
            val tipus = tipusEditText.text.toString().trim()
            val codi = codiEditText.text.toString().trim()
            val dia = diaEditText.text.toString().trim()
            val inici = iniciEditText.text.toString().trim()
            val fi = fiEditText.text.toString().trim()
            val preu = preuEditText.text.toString().trim()

            if (tipus.isEmpty() || codi.isEmpty() || dia.isEmpty() || inici.isEmpty() || fi.isEmpty() || preu.isEmpty()) {
                Toast.makeText(requireContext(), R.string.error_camps_reserva_obligatoris, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            guardarButton.isEnabled = false
            pujarAdjuntsSiCal(
                onSuccess = { pdfFinal, qrFinal, imatgeFinal ->
                    guardarReserva(
                        pdfUrl = pdfFinal,
                        qrUrl = qrFinal,
                        imatgeUrl = imatgeFinal,
                        tipus = tipus,
                        codi = codi,
                        dia = dia,
                        inici = inici,
                        fi = fi,
                        preu = preu
                    )
                },
                onError = { exception ->
                    guardarButton.isEnabled = true
                    Log.e("EditarReserva", "Error pujant adjunts", exception)
                    Toast.makeText(
                        requireContext(),
                        exception.localizedMessage ?: getString(R.string.error_pujar_fitxer_reserva),
                        Toast.LENGTH_LONG
                    ).show()
                }
            )
        }
    }

    private fun pujarAdjuntsSiCal(
        onSuccess: (String?, String?, String?) -> Unit,
        onError: (Exception) -> Unit
    ) {
        pujarPdfSiCal(
            onSuccess = { pdfFinal ->
                pujarQrSiCal(
                    onSuccess = { qrFinal ->
                        pujarImatgeSiCal(
                            onSuccess = { imatgeFinal ->
                                onSuccess(pdfFinal, qrFinal, imatgeFinal)
                            },
                            onError = onError
                        )
                    },
                    onError = onError
                )
            },
            onError = onError
        )
    }

    private fun pujarPdfSiCal(onSuccess: (String?) -> Unit, onError: (Exception) -> Unit) {
        val uri = pdfUri ?: run {
            onSuccess(pdfUrl)
            return
        }
        pujarFitxer(
            uri = uri,
            path = "reserves/pdfs/${System.currentTimeMillis()}.pdf",
            contentType = "application/pdf",
            onSuccess = {
                pdfUrl = it
                onSuccess(it)
            },
            onError = onError
        )
    }

    private fun pujarQrSiCal(onSuccess: (String?) -> Unit, onError: (Exception) -> Unit) {
        val uri = qrUri ?: run {
            onSuccess(qrUrl)
            return
        }
        pujarFitxer(
            uri = uri,
            path = "reserves/qrs/${System.currentTimeMillis()}.jpg",
            contentType = "image/jpeg",
            onSuccess = {
                qrUrl = it
                onSuccess(it)
            },
            onError = onError
        )
    }

    private fun pujarImatgeSiCal(onSuccess: (String?) -> Unit, onError: (Exception) -> Unit) {
        val uri = imatgeUri ?: run {
            onSuccess(imatgeUrl)
            return
        }
        pujarFitxer(
            uri = uri,
            path = "reserves/imatges/${System.currentTimeMillis()}.jpg",
            contentType = "image/jpeg",
            onSuccess = {
                imatgeUrl = it
                onSuccess(it)
            },
            onError = onError
        )
    }

    private fun pujarFitxer(
        uri: Uri,
        path: String,
        contentType: String,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val storageRef = FirebaseStorage.getInstance().reference.child(path)
        val metadata = StorageMetadata.Builder()
            .setContentType(contentType)
            .build()

        storageRef.putFile(uri, metadata)
            .continueWithTask { task ->
                if (!task.isSuccessful) {
                    throw task.exception ?: IllegalStateException(getString(R.string.error_pujar_fitxer_reserva))
                }
                storageRef.downloadUrl
            }
            .addOnSuccessListener { downloadUrl -> onSuccess(downloadUrl.toString()) }
            .addOnFailureListener(onError)
    }

    private fun descarregarPdf() {
        // Evita dobles tocs mentre s'està processant una descàrrega.
        if (descarregaEnCurs) return
        descarregaEnCurs = true
        btnDescarregarPdf.isEnabled = false

        when {
            pdfUri != null -> {
                try {
                    val sourceUri = pdfUri!!
                    val inputStream = requireContext().contentResolver.openInputStream(sourceUri) ?: run {
                        Toast.makeText(requireContext(), R.string.error_obrir_pdf, Toast.LENGTH_SHORT).show()
                        resetEstatDescarrega()
                        return
                    }
                    val fallbackName = "reserva_${System.currentTimeMillis()}.pdf"
                    val fileName = obtenirNomFitxer(sourceUri) ?: fallbackName
                    val targetFile = File(requireContext().cacheDir, fileName)
                    inputStream.use { input ->
                        FileOutputStream(targetFile).use { output -> input.copyTo(output) }
                    }
                    guardarPdfEnDescargas(targetFile, fileName)
                } catch (exception: Exception) {
                    Log.e("EditarReserva", "Error processant PDF local", exception)
                    Toast.makeText(
                        requireContext(),
                        exception.localizedMessage ?: getString(R.string.error_no_descarregar_pdf),
                        Toast.LENGTH_LONG
                    ).show()
                } finally {
                    resetEstatDescarrega()
                }
            }

            !pdfUrl.isNullOrBlank() -> {
                try {
                    FirebaseStorage.getInstance()
                        .getReferenceFromUrl(pdfUrl!!)
                        .getStream()
                        .addOnSuccessListener { taskSnapshot ->
                            val targetFile =
                                File(requireContext().cacheDir, "reserva_${System.currentTimeMillis()}.pdf")
                            taskSnapshot.stream.use { input ->
                                FileOutputStream(targetFile).use { output -> input.copyTo(output) }
                            }
                            guardarPdfEnDescargas(targetFile, targetFile.name)
                            resetEstatDescarrega()
                        }
                        .addOnFailureListener { exception ->
                            Log.e("EditarReserva", "Error descarregant PDF", exception)
                            Toast.makeText(
                                requireContext(),
                                exception.localizedMessage ?: getString(R.string.error_descarregar_pdf),
                                Toast.LENGTH_LONG
                            ).show()
                            resetEstatDescarrega()
                        }
                } catch (exception: IllegalArgumentException) {
                    Log.e("EditarReserva", "URL de PDF invÃ lida: $pdfUrl", exception)
                    Toast.makeText(requireContext(), R.string.error_no_descarregar_pdf, Toast.LENGTH_SHORT).show()
                    resetEstatDescarrega()
                }
            }

            else -> {
                Toast.makeText(requireContext(), R.string.error_sense_pdf_visualitzar, Toast.LENGTH_SHORT).show()
                resetEstatDescarrega()
            }
        }
    }

    private fun visualitzarImatge(urlOrUri: String?) {
        if (urlOrUri.isNullOrBlank()) return
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_image_preview)
        val vistaImatge = dialog.findViewById<ImageView>(R.id.ivPreviewImage)
        Glide.with(this)
            .load(urlOrUri)
            .into(vistaImatge)
        dialog.show()
    }

    private fun actualitzarPreviewPdf() {
        val hasPdf = pdfUri != null || !pdfUrl.isNullOrBlank()
        cardPdfPreview.visibility = if (hasPdf) View.VISIBLE else View.GONE
        btnDescarregarPdf.visibility = if (hasPdf) View.VISIBLE else View.GONE
        if (hasPdf) {
            val nom = pdfUri?.lastPathSegment ?: getString(R.string.pdf_guardat_preview)
            tvPdfPreviewNom.text = nom
        }
    }

    private fun actualitzarPreviewQr() {
        val src = qrUri?.toString() ?: qrUrl
        val hasQr = !src.isNullOrBlank()
        cardQrPreview.visibility = if (hasQr) View.VISIBLE else View.GONE
        if (hasQr) {
            Glide.with(this).load(src).centerCrop().into(ivQrPreview)
        }
    }

    private fun actualitzarPreviewImatge() {
        val src = imatgeUri?.toString() ?: imatgeUrl
        val hasImatge = !src.isNullOrBlank()
        cardImatgePreview.visibility = if (hasImatge) View.VISIBLE else View.GONE
        if (hasImatge) {
            Glide.with(this).load(src).centerCrop().into(ivImatgePreview)
        }
    }

    private fun persistirPermisLectura(uri: Uri) {
        try {
            requireContext().contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: SecurityException) {
        }
    }

    private fun guardarReserva(
        pdfUrl: String?,
        qrUrl: String?,
        imatgeUrl: String?,
        tipus: String,
        codi: String,
        dia: String,
        inici: String,
        fi: String,
        preu: String
    ) {
        val organitzacioId = arguments?.getInt(ARG_ORGANITZACIO_ID)?.takeIf { it != NO_ID }
        val reservaId = arguments?.getInt(ARG_RESERVA_ID)?.takeIf { it != NO_ID }

        if (reservaId == null) {
            OrganitzaViatgeRepository.crearReserva(
                organitzacioId = organitzacioId,
                tipus = tipus,
                codi = codi,
                diaReserva = dia,
                dataInici = inici,
                dataFi = fi,
                preu = preu,
                pdfUrl = pdfUrl,
                qrUrl = qrUrl,
                imatgeUrl = imatgeUrl
            )
        } else {
            OrganitzaViatgeRepository.actualitzarReserva(
                organitzacioId = organitzacioId,
                reservaId = reservaId,
                tipus = tipus,
                codi = codi,
                diaReserva = dia,
                dataInici = inici,
                dataFi = fi,
                preu = preu,
                pdfUrl = pdfUrl,
                qrUrl = qrUrl,
                imatgeUrl = imatgeUrl
            )
        }

        if (organitzacioId == null) {
            Toast.makeText(requireContext(), R.string.reserva_guardada_ok, Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        } else {
            OrganitzaViatgeRepository.persistirOrganitzacio(
                context = requireContext(),
                organitzacioId = organitzacioId,
                onSuccess = {
                    Toast.makeText(requireContext(), R.string.reserva_guardada_ok, Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                },
                onError = {
                    guardarButton.isEnabled = true
                    Toast.makeText(
                        requireContext(),
                        R.string.error_sincronitzacio_viatge,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }
    }

    private fun obtenirNomFitxer(uri: Uri): String? {
        return requireContext().contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameColumn = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameColumn != -1 && cursor.moveToFirst()) {
                cursor.getString(nameColumn)
            } else {
                null
            }
        }
    }

    private fun guardarPdfEnDescargas(file: File, fileName: String) {
        // Copia el PDF al directori de descàrregues perquè sigui visible fora de l'app.
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            } else {
                @Suppress("DEPRECATION")
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                put(MediaStore.MediaColumns.DATA, File(downloadsDir, fileName).absolutePath)
            }
        }
        val collection = MediaStore.Files.getContentUri("external")
        val uri = requireContext().contentResolver.insert(collection, values)
        if (uri == null) {
            Toast.makeText(requireContext(), R.string.error_no_descarregar_pdf, Toast.LENGTH_SHORT).show()
            return
        }
        requireContext().contentResolver.openOutputStream(uri)?.use { output ->
            file.inputStream().use { input -> input.copyTo(output) }
        } ?: run {
            Toast.makeText(requireContext(), R.string.error_no_descarregar_pdf, Toast.LENGTH_SHORT).show()
            return
        }
        Toast.makeText(requireContext(), R.string.pdf_descarregat_ok, Toast.LENGTH_SHORT).show()
    }

    private fun resetEstatDescarrega() {
        descarregaEnCurs = false
        btnDescarregarPdf.isEnabled = true
    }

    companion object {
        private const val ARG_ORGANITZACIO_ID = "organitzacio_id"
        private const val ARG_RESERVA_ID = "reserva_id"
        private const val NO_ID = -1

        fun newInstance(organitzacioId: Int?, reservaId: Int?): EditarReservaFragment {
            return EditarReservaFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_ORGANITZACIO_ID, organitzacioId ?: NO_ID)
                    putInt(ARG_RESERVA_ID, reservaId ?: NO_ID)
                }
            }
        }
    }
}


