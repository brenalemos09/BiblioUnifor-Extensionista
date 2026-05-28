package com.example.bibliounifornew.features.usuario.livro

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.EntidadeLivro
import com.example.bibliounifornew.features.usuario.biblioteca.TelaRF14LeituraActivity
import com.google.android.material.button.MaterialButton

class TelaLivroActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf12_teladolivro)

        val livroId = intent.getStringExtra("LIVRO_ID") ?: ""
        carregarLivro(livroId)
    }

    private fun carregarLivro(livroId: String) {
        // Mock data loading
        val mockLivro = EntidadeLivro(
            id = livroId,
            title = "1984",
            author = "George Orwell",
            description = "Uma obra-prima da literatura distópica que explora os perigos do totalitarismo e da vigilância governamental.",
            category = "Ficção Científica",
            coverUrl = "https://m.media-amazon.com/images/I/91SZS6B7-CL.jpg",
            stockQuantity = 5,
            isAvailable = true
        )
        popularUI(mockLivro)
    }

    private fun popularUI(livro: EntidadeLivro) {
        findViewById<TextView>(R.id.textTituloLivro)?.text = livro.title
        findViewById<TextView>(R.id.textAutorLivro)?.text  = livro.author
        findViewById<TextView>(R.id.textSobreLivro)?.text  = livro.description

        val imgCapa = findViewById<ImageView>(R.id.imageLivroDetalhes)
        if (livro.coverUrl.isNotEmpty()) {
            imgCapa?.load(livro.coverUrl) {
                placeholder(R.drawable.osda)
                error(R.drawable.osda)
            }
        } else {
            imgCapa?.setImageResource(R.drawable.osda)
        }

        if (livro.category.isNotEmpty()) {
            findViewById<MaterialButton>(R.id.buttonGenero)?.text = livro.category
        }

        val txtDisp    = findViewById<TextView>(R.id.textDisponivel)
        val txtEstoque = findViewById<TextView>(R.id.textEstoque)
        val indicador  = findViewById<View>(R.id.statusIndicator)

        if (livro.isAvailable && livro.stockQuantity > 0) {
            txtDisp?.text = "Disponível para aluguel"
            txtDisp?.setTextColor(Color.parseColor("#2E7D32"))
            txtEstoque?.text = "${livro.stockQuantity} unidades em estoque"
            indicador?.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#4CAF50"))
        } else {
            txtDisp?.text = "Indisponível no momento"
            txtDisp?.setTextColor(Color.parseColor("#C62828"))
            txtEstoque?.text = "Sem estoque"
            indicador?.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#C62828"))
        }

        findViewById<MaterialButton>(R.id.buttonVerMais)?.setOnClickListener {
            startActivity(Intent(this, TelaRF13VerMaisLivro::class.java).putExtra("LIVRO_ID", livro.id))
        }

        findViewById<MaterialButton>(R.id.buttonSolicitar)?.setOnClickListener {
            startActivity(Intent(this, TelaRF14LeituraActivity::class.java).putExtra("LIVRO_ID", livro.id))
        }

        findViewById<MaterialButton>(R.id.buttonLer)?.setOnClickListener {
            startActivity(Intent(this, TelaRF14LeituraActivity::class.java).putExtra("LIVRO_ID", livro.id))
        }
    }
}
