package com.basmazou.musafirun

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {
    override fun onCreate(estatInstanciaGuardat: Bundle?) {
        AppSettingsManager.applySavedSettings(this)
        super.onCreate(estatInstanciaGuardat)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val db = FirebaseFirestore.getInstance()

        val usuari = findViewById<EditText>(R.id.usuari)
        val gmail = findViewById<EditText>(R.id.gmail)
        val contrasenya = findViewById<EditText>(R.id.contrasenya)
        val botoRegistre = findViewById<Button>(R.id.botoRegistre)

        botoRegistre.setOnClickListener {
            val nomUsuari = usuari.text.toString().trim()
            val correu = gmail.text.toString().trim()
            val pwd = contrasenya.text.toString().trim()

            if (nomUsuari.isEmpty() || correu.isEmpty() || pwd.isEmpty()) {
                Toast.makeText(this, R.string.error_registre_camps_buits, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(correu).matches()) {
                Toast.makeText(this, R.string.error_registre_correu_invalid, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            db.collection("usuaris")
                .document(nomUsuari)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        Toast.makeText(
                            this,
                            R.string.error_registre_usuari_existent,
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        val dadesUsuari = hashMapOf(
                            "usuari" to nomUsuari,
                            "gmail" to correu,
                            "contrasenya" to pwd
                        )

                        db.collection("usuaris")
                            .document(nomUsuari)
                            .set(dadesUsuari)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this,
                                    R.string.registre_ok,
                                    Toast.LENGTH_SHORT
                                ).show()

                                val intent = Intent(this, IniciSessio::class.java)
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(
                                    this,
                                    R.string.error_registre_guardar,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(
                        this,
                        R.string.error_registre_comprovar,
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }

        val botoRediregirPantallaIniciSessio =
            findViewById<Button>(R.id.botoRediregirPantallaIniciSessio)
        botoRediregirPantallaIniciSessio.setOnClickListener {
            val intent = Intent(this, IniciSessio::class.java)
            startActivity(intent)
        }

        val continuarInvitat = findViewById<TextView>(R.id.continuarInvitat)
        continuarInvitat.setOnClickListener {
            UserSessionManager.startGuestSession(this)
            val intent = Intent(this, PantallaPrincipal::class.java)
            startActivity(intent)
            finish()
        }
    }
}


