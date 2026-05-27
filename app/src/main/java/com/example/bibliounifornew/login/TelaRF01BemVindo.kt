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

/**
 * Tela inicial de Boas-Vindas (RF01)
 */
class TelaRF01BemVindo : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf01_bemvindo)

        // Carrega a imagem de forma segura para evitar crash de memória
        val imageLogo = findViewById<ImageView>(R.id.imageIcon2)
        carregarLogoSegura(imageLogo)

        val btnEntrar = findViewById<Button>(R.id.buttonComecar)
        btnEntrar.setOnClickListener {
            val intent = Intent(this@TelaRF01BemVindo, TelaRF02Intermediaria::class.java)
            startActivity(intent)
        }
    }

    // Decode em Dispatchers.IO — libera a Main Thread e elimina o ANR de frame skip.
    // lifecycleScope cancela automaticamente se a Activity for destruída antes de concluir,
    // portanto não há risco de atualizar uma View morta.
    private fun carregarLogoSegura(imageView: ImageView) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val options = BitmapFactory.Options().apply {
                    inSampleSize = 4          // 16× menos memória que a imagem original
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
