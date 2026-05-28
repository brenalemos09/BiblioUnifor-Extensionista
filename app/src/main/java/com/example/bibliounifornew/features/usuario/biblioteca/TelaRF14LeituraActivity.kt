package com.example.bibliounifornew.features.usuario.biblioteca

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.MockData
import com.example.bibliounifornew.features.usuario.livro.TelaRF11TelaDePesquisa
import com.example.bibliounifornew.features.usuario.solicitacao.TelaRF18StatusAluguel

class TelaRF14LeituraActivity : AppCompatActivity() {

    private var livroIdAtual   : String = ""
    private var linkPdfAtual   : String = "https://www.google.com"
    private var linkAudioAtual : String = "https://www.google.com"
    private var tituloAtual    : String = "O Senhor dos Anéis"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf14_leitura)

        livroIdAtual = intent.getStringExtra("LIVRO_ID") ?: "1"

        // Carregar Mock baseado no ID
        val livro = MockData.livros.find { it.id == livroIdAtual } ?: MockData.livros[0]
        tituloAtual = livro.title

        findViewById<TextView>(R.id.textTituloLivroAcoes)?.text    = tituloAtual
        findViewById<TextView>(R.id.textAutorLivroAcoes)?.text     = livro.author
        findViewById<TextView>(R.id.textCategoriaLivroAcoes)?.text = livro.category
        findViewById<ImageView>(R.id.imageLivroAcoes)?.setImageResource(R.drawable.osda)

        // ─── BOTÕES DE FLUXO DIRETO PARA TERMOS ─────────────────────────────
        fun irParaTermos(tipo: String) {
            val intent = Intent(this, com.example.bibliounifornew.features.usuario.solicitacao.TelaRF19SolicitacoesTermosCondicoes::class.java)
            intent.putExtra("TIPO_MIDIA", tipo)
            intent.putExtra("LIVRO_ID", livroIdAtual)
            intent.putExtra("TITULO", tituloAtual)
            startActivity(intent)
        }

        findViewById<Button>(R.id.buttonReservarLivro).setOnClickListener { irParaTermos("Reserva") }
        findViewById<Button>(R.id.buttonSolicitarPdf).setOnClickListener { irParaTermos("PDF") }
        findViewById<Button>(R.id.buttonSolicitarBraille).setOnClickListener { irParaTermos("Braille") }
        findViewById<Button>(R.id.buttonSolicitarAudiobook).setOnClickListener { irParaTermos("Audiobook") }

        // ─── BOTÃO ALUGAR (POPUP PRIMEIRO) ────────────────────────────────────
        findViewById<Button>(R.id.buttonAlugarLivro).setOnClickListener {
            showPopupAlugar()
        }

        // ─── BOTÃO PROCURAR ───────────────────────────────────────────────────
        findViewById<Button>(R.id.buttonProcurarLivro).setOnClickListener {
            startActivity(Intent(this, TelaRF11TelaDePesquisa::class.java))
        }

        // ─── BOTÃO ABRIR PDF ──────────────────────────────────────────────────
        findViewById<Button>(R.id.buttonAbrirPdfLivro).setOnClickListener {
            Toast.makeText(this, "Abrindo PDF", Toast.LENGTH_SHORT).show()
        }

        // ─── BOTÃO ABRIR AUDIOBOOK ────────────────────────────────────────────
        findViewById<Button>(R.id.buttonAbrirAudioLivro).setOnClickListener {
            Toast.makeText(this, "Abrindo Audiobook", Toast.LENGTH_SHORT).show()
        }

        // ─── BOTÃO SETOR LOCALIZADO ──────────────────────────────────────────
        findViewById<Button>(R.id.buttonSetorLivro).setOnClickListener {
            showPopupSetor()
        }
    }

    private fun abrirMidia(url: String, tipo: String) {
        try {
            val uri    = Uri.parse(url)
            val mime   = if (tipo == "pdf") "application/pdf" else "audio/*"
            val titulo = if (tipo == "pdf") "Abrir PDF com..." else "Ouvir Audiobook com..."
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mime)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(Intent.createChooser(intent, titulo))
        } catch (e: Exception) {
            Toast.makeText(this, "Não foi possível abrir o arquivo.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showPopupAlugar() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_alugar_livro)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.85).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        dialog.findViewById<TextView>(R.id.textTituloPopupAlugar)?.text =
            "Você deseja alugar o livro\n\"$tituloAtual\"?"

        val btnConfirmar = dialog.findViewById<Button>(R.id.buttonAdicionarLivro)
        val btnCancelar  = dialog.findViewById<TextView>(R.id.textCancelarPopup)

        btnConfirmar.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this, com.example.bibliounifornew.features.usuario.solicitacao.TelaRF19SolicitacoesTermosCondicoes::class.java)
            intent.putExtra("TIPO_MIDIA", "Aluguel")
            intent.putExtra("LIVRO_ID", livroIdAtual)
            intent.putExtra("TITULO", tituloAtual)
            startActivity(intent)
        }
        btnCancelar.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun showPopupSucesso() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_livro_adicionado)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.85).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        dialog.findViewById<Button>(R.id.buttonVerMeusLivros).setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(this, TelaRF18StatusAluguel::class.java))
        }

        dialog.show()
    }

    private fun showPopupSetor() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_setor_localizado)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialog.findViewById<TextView>(R.id.textLivroSetor)?.text = "Livro: $tituloAtual"

        dialog.findViewById<Button>(R.id.buttonVoltarSetor).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
}
