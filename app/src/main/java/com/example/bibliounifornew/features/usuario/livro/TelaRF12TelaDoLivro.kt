package com.example.bibliounifornew.features.usuario.livro

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.EntidadeLivro
import com.example.bibliounifornew.data.MockData
import com.example.bibliounifornew.features.usuario.biblioteca.TelaRF14LeituraActivity
import com.google.android.material.button.MaterialButton

class TelaRF12TelaDoLivro : AppCompatActivity() {

    private var livroIdAtual : String = ""
    private var tituloAtual  : String = ""
    private var autorAtual   : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf12_teladolivro)

        livroIdAtual = intent.getStringExtra("LIVRO_ID") ?: "mock_id_1"
        
        carregarDadosDoLivroMock(livroIdAtual)
        
        configurarBotoesDeStatus()
        configurarBotoesAcao()
    }

    private fun carregarDadosDoLivroMock(id: String) {
        val livro = MockData.livros.find { it.id == id } ?: MockData.livros[0]

        tituloAtual = livro.title
        autorAtual  = livro.author
        val descricao = livro.description
        val categoria = livro.category
        val estoque = 5L

        findViewById<TextView>(R.id.textTituloLivro)?.text = tituloAtual
        findViewById<TextView>(R.id.textAutorLivro)?.text  = autorAtual
        findViewById<TextView>(R.id.textSobreLivro)?.text  = descricao
        findViewById<ImageView>(R.id.imageLivroDetalhes)?.setImageResource(R.drawable.osda)
        findViewById<MaterialButton>(R.id.buttonGenero)?.text = categoria

        val txtDisp    = findViewById<TextView>(R.id.textDisponivel)
        val txtEstoque = findViewById<TextView>(R.id.textEstoque)
        val indicador  = findViewById<View>(R.id.statusIndicator)

        txtDisp?.text = "Disponível para aluguel"
        txtDisp?.setTextColor(Color.parseColor("#2E7D32"))
        txtEstoque?.text = "$estoque unidades em estoque"
        indicador?.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#4CAF50"))

        val ratingBar = findViewById<RatingBar>(R.id.ratingBarLivro)
        ratingBar?.rating = 4.5f
        ratingBar?.setOnRatingBarChangeListener { _, rating, _ ->
            Toast.makeText(this, "Você deu nota $rating!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun configurarBotoesDeStatus() {
        val btnNaoLido = findViewById<MaterialButton>(R.id.buttonNaoLido) ?: return
        val btnLendo   = findViewById<MaterialButton>(R.id.buttonLendo)   ?: return
        val btnLido    = findViewById<MaterialButton>(R.id.buttonLido)    ?: return

        definirBotaoInativo(btnNaoLido)
        definirBotaoInativo(btnLendo)
        definirBotaoInativo(btnLido)

        btnNaoLido.setOnClickListener {
            definirBotaoAtivo(btnNaoLido); definirBotaoInativo(btnLendo); definirBotaoInativo(btnLido)
            Toast.makeText(this, "Status: Não Lido", Toast.LENGTH_SHORT).show()
        }
        btnLendo.setOnClickListener {
            definirBotaoInativo(btnNaoLido); definirBotaoAtivo(btnLendo); definirBotaoInativo(btnLido)
            Toast.makeText(this, "Status: Lendo", Toast.LENGTH_SHORT).show()
        }
        btnLido.setOnClickListener {
            definirBotaoInativo(btnNaoLido); definirBotaoInativo(btnLendo); definirBotaoAtivo(btnLido)
            Toast.makeText(this, "Status: Lido", Toast.LENGTH_SHORT).show()
        }
    }

    private fun definirBotaoAtivo(btn: MaterialButton) {
        btn.backgroundTintList = getColorStateList(R.color.biblio_blue)
        btn.setTextColor(getColor(android.R.color.white))
    }

    private fun definirBotaoInativo(btn: MaterialButton) {
        btn.backgroundTintList = getColorStateList(R.color.biblio_detalhes)
        btn.setTextColor(getColor(R.color.biblio_dark))
    }

    private fun configurarBotoesAcao() {
        findViewById<MaterialButton>(R.id.buttonListaDesejos)?.setOnClickListener {
            Toast.makeText(this, "Adicionado à Lista de Desejos!", Toast.LENGTH_SHORT).show()
        }
        findViewById<MaterialButton>(R.id.buttonSuaLivraria)?.setOnClickListener {
            Toast.makeText(this, "Adicionado à sua Livraria!", Toast.LENGTH_SHORT).show()
        }
        findViewById<MaterialButton>(R.id.buttonVerMais)?.setOnClickListener {
            startActivity(Intent(this, TelaRF13VerMaisLivro::class.java).putExtra("LIVRO_ID", livroIdAtual))
        }
        findViewById<MaterialButton>(R.id.buttonSolicitar)?.setOnClickListener {
            startActivity(Intent(this, TelaRF14LeituraActivity::class.java).putExtra("LIVRO_ID", livroIdAtual))
        }
        findViewById<MaterialButton>(R.id.buttonLer)?.setOnClickListener {
            startActivity(Intent(this, TelaRF14LeituraActivity::class.java).putExtra("LIVRO_ID", livroIdAtual))
        }
    }
}
