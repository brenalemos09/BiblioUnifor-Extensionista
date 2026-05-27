package com.example.bibliounifornew.login

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bibliounifornew.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TelaRF02Intermediaria : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf02_intermediaria)

        // Carregamento seguro da logo
        val imageLogo = findViewById<ImageView>(R.id.imageIcon2)
        carregarLogoSegura(imageLogo)

        val btnEstudante = findViewById<Button>(R.id.btnEstudante)
        val btnAdmin = findViewById<Button>(R.id.btnAdmin)

        btnEstudante.setOnClickListener {
            val intent = Intent(this, TelaRF03LoginAluno::class.java)
            startActivity(intent)
        }

        btnAdmin.setOnClickListener {
            // Navegação para a Tela de Login do Administrador (RF23)
            val intent = Intent(this, TelaRF23LoginADM::class.java)
            startActivity(intent)
        }
    }

    private fun carregarLogoSegura(imageView: ImageView) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val options = BitmapFactory.Options().apply {
                    inSampleSize = 4
                    inJustDecodeBounds = false
                }
                val bitmap = BitmapFactory.decodeResource(resources, R.drawable.unifor_marca, options)
                withContext(Dispatchers.Main) {
                    imageView.setImageBitmap(bitmap)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
