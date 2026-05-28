package com.example.bibliounifornew.features.usuario.solicitacao

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.MockData

class TelaRF19Solicitacoes : AppCompatActivity() {

    private var tituloAtual: String = ""
    private var autorAtual: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf19_solicitacoes)

        // ─── EXTRAS (MOCK) ───────────────────────────────────────────────────
        val livroId = intent.getStringExtra("LIVRO_ID") ?: "1"

        val imgCapa   = findViewById<ImageView>(R.id.imageLivroSolicitacao)
        val txtTitulo = findViewById<TextView>(R.id.textTituloLivroSolicitacao)
        val txtAutor  = findViewById<TextView>(R.id.textAutorLivroSolicitacao)
        val txtGenero = findViewById<TextView>(R.id.textGeneroLivroSolicitacao)

        // Dados Mockados baseados no ID
        val livro = MockData.livros.find { it.id == livroId } ?: MockData.livros[0]
        tituloAtual = livro.title
        autorAtual  = livro.author
        txtTitulo?.text = tituloAtual
        txtAutor?.text  = autorAtual
        txtGenero?.text = livro.category
        imgCapa?.setImageResource(R.drawable.osda)

        // ─── BOTÕES → TERMOS COM TIPO E LIVRO_ID ─────────────────────────────
        fun irParaTermos(tipoMidia: String) {
            startActivity(
                Intent(this, TelaRF19SolicitacoesTermosCondicoes::class.java)
                    .putExtra("TIPO_MIDIA", tipoMidia)
                    .putExtra("LIVRO_ID",   livroId)
                    .putExtra("TITULO",     tituloAtual)
                    .putExtra("AUTOR",      autorAtual)
            )
        }

        val btnPdf      = findViewById<Button>(R.id.buttonSolicitarPdf)
        val btnBraile   = findViewById<Button>(R.id.buttonSolicitarBraille)
        val btnAudio    = findViewById<Button>(R.id.buttonSolicitarAudiobook)
        val btnReservar = findViewById<Button>(R.id.buttonReservarLivro)
        val btnSetor    = findViewById<Button>(R.id.buttonSetorLocalizado)

        btnPdf?.setOnClickListener      { irParaTermos("PDF")       }
        btnBraile?.setOnClickListener   { irParaTermos("Braille")   }
        btnAudio?.setOnClickListener    { irParaTermos("Audiobook") }
        btnReservar?.setOnClickListener { irParaTermos("Reserva")   }
        btnSetor?.setOnClickListener    { showPopupSetor()           }
    }

    override fun onResume() {
        super.onResume()
        // Mantemos os botões funcionais
    }

    // ─── POPUP SETOR ─────────────────────────────────────────────────────────

    private fun showPopupSetor() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_setor_localizado)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Injeta o nome do livro atual no popup
        dialog.findViewById<TextView>(R.id.textLivroSetor)?.text = "Livro: $tituloAtual"

        dialog.findViewById<Button>(R.id.buttonVoltarSetor)
            ?.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }
}
