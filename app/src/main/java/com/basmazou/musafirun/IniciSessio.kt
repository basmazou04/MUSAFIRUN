package com.basmazou.musafirun

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class IniciSessio : AppCompatActivity() {
    override fun onCreate(estatInstanciaGuardat: Bundle?) {
        AppSettingsManager.applySavedSettings(this)
        super.onCreate(estatInstanciaGuardat)
        enableEdgeToEdge()
        setContentView(R.layout.activity_inici_sessio)

        val recordar = getSharedPreferences("loginPrefs", MODE_PRIVATE)

        if (recordar.getBoolean("recorda", false)) {
            startActivity(Intent(this, PantallaPrincipal::class.java))
            finish()
            return
        }

        val db = FirebaseFirestore.getInstance()

        val botoIniciarSesio = findViewById<Button>(R.id.botoIniciarSesio)
        val nomUsuariEditText = findViewById<EditText>(R.id.nomusuari)
        val contrasenyaEditText = findViewById<EditText>(R.id.contrasenya)
        val recordam = findViewById<CheckBox>(R.id.recordam)

        botoIniciarSesio.setOnClickListener {
            val nomUsuari = nomUsuariEditText.text.toString().trim()
            val contrasenya = contrasenyaEditText.text.toString().trim()

            if (nomUsuari.isEmpty() || contrasenya.isEmpty()) {
                Toast.makeText(this, R.string.error_login_camps_buits, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            db.collection("usuaris")
                .document(nomUsuari)
                .get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        val contrasenyaBD = doc.getString("contrasenya")

                        if (contrasenyaBD == contrasenya) {
                            UserSessionManager.startRegisteredSession(
                                context = this,
                                userId = nomUsuari,
                                remember = recordam.isChecked
                            )

                            Log.d("IniciSesio", "Usuari guardat: $nomUsuari")
                            Toast.makeText(this, R.string.login_iniciat_ok, Toast.LENGTH_SHORT).show()

                            val intent = Intent(this, PantallaPrincipal::class.java)
                            intent.putExtra("usuari", nomUsuari)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this, R.string.error_login_contrasenya, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, R.string.error_login_firestore, Toast.LENGTH_SHORT).show()
                }
        }

        val botoNoTincCompte = findViewById<Button>(R.id.botoNoTincCompte)
        botoNoTincCompte.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}


