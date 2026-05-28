package com.example.bibliounifornew.features.usuario.solicitacao

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.CheckBox
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R
import com.example.bibliounifornew.features.usuario.livro.TelaRF12TelaDoLivro

class TelaRF19SolicitacoesTermosCondicoes : AppCompatActivity() {

    private var tituloLivro: String = ""
    private var autorLivro: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf19_solicitacoes_termos_condicoes)

        // ─── EXTRAS RECEBIDOS DE TelaRF19Solicitacoes ────────────────────────
        val tipoMidia = intent.getStringExtra("TIPO_MIDIA") ?: ""
        val livroId   = intent.getStringExtra("LIVRO_ID")   ?: ""
        tituloLivro   = intent.getStringExtra("TITULO")     ?: ""
        autorLivro    = intent.getStringExtra("AUTOR")      ?: ""

        // ─── VIEWS ────────────────────────────────────────────────────────────
        val scrollView   = findViewById<ScrollView>(R.id.scrollTermos)
        val checkBox     = findViewById<CheckBox>(R.id.checkTelaAceitarTermos)
        val btnConfirmar = findViewById<Button>(R.id.buttonConfirmarTermosTela)
        val textAviso    = findViewById<TextView>(R.id.textAvisoScroll)

        // Bloqueados até o usuário rolar o scroll até o fim dos termos
        checkBox.isEnabled     = false
        btnConfirmar.isEnabled = false
        btnConfirmar.alpha     = 0.5f

        // ─── LÓGICA DE SCROLL → HABILITA CHECKBOX ────────────────────────────
        scrollView.viewTreeObserver.addOnScrollChangedListener {
            val child = scrollView.getChildAt(scrollView.childCount - 1)
            val diff  = child.bottom - (scrollView.height + scrollView.scrollY)
            if (diff <= 0) {
                checkBox.isEnabled = true
                textAviso.visibility = android.view.View.GONE
            }
        }

        // ─── CHECKBOX → HABILITA BOTÃO ────────────────────────────────────────
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            btnConfirmar.isEnabled = isChecked
            btnConfirmar.alpha     = if (isChecked) 1.0f else 0.5f
        }

        // ─── CONFIRMAR → PERSISTE NO FIRESTORE (MOCKADO) ────────────────────
        btnConfirmar.setOnClickListener {
            showPopupSucesso(tipoMidia, livroId)
        }
    }

    // ─── POPUP DE SUCESSO ─────────────────────────────────────────────────────

    private fun showPopupSucesso(tipoMidia: String, livroId: String) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.telarf19_solicitacoes_voltar_biblioteca)
        dialog.window?.setBackgroundDrawable(
            android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT)
        )
        dialog.setCancelable(false)

        val txtMensagem = dialog.findViewById<TextView>(R.id.textPopupSolicitacao)
        
        val mensagem = when(tipoMidia) {
            "PDF"       -> "Solicitação de PDF enviada com sucesso"
            "Braille"   -> "Solicitação Braille enviada com sucesso"
            "Audiobook" -> "Solicitação de Audiobook enviada com sucesso"
            "Reserva"   -> "Livro reservado com sucesso"
            "Aluguel"   -> "Livro alugado com sucesso"
            else        -> "Operação realizada com sucesso"
        }
        
        txtMensagem?.text = mensagem

        dialog.findViewById<Button>(R.id.buttonPopupOkSolicitacao)
            ?.setOnClickListener {
                dialog.dismiss()
                voltarParaLivro(livroId)
            }

        dialog.show()
    }

    private fun voltarParaLivro(livroId: String) {
        val intent = Intent(this, TelaRF12TelaDoLivro::class.java)
            .putExtra("LIVRO_ID", livroId)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
        finish()
    }
}
