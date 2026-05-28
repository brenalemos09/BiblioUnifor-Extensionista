package com.example.bibliounifornew.features.adm.gerenciamento

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.example.bibliounifornew.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class TelaRF37InfoLivroADM : AppCompatActivity() {

    private var livroId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf37_info_livro_adm)

        livroId = intent.getStringExtra("LIVRO_ID") ?: "1"

        carregarDadosLivroMock()

        // Configurar cliques dos lápis para editar campos
        configurarEdicao(R.id.btnEditTitulo,    R.id.editTituloLivro)
        configurarEdicao(R.id.btnEditAutor,     R.id.editAutorLivro)
        configurarEdicao(R.id.btnEditDescricao, R.id.editDescricaoLivro)
        configurarEdicao(R.id.btnEditLingua,    R.id.editLingua)
        configurarEdicao(R.id.btnEditEditora,   R.id.editEditora)
        configurarEdicao(R.id.btnEditDimensao,  R.id.editDimensao)
        configurarEdicao(R.id.btnEditISBN10,    R.id.editISBN10)
        configurarEdicao(R.id.btnEditISBN13,    R.id.editISBN13)
        configurarEdicao(R.id.btnEditData,      R.id.editData)
        configurarEdicao(R.id.btnEditPaginas,   R.id.editPaginas)

        // Controle de Exemplares
        findViewById<com.google.android.material.card.MaterialCardView>(R.id.btnDiminuirExemplares)
            ?.setOnClickListener { atualizarExemplares(-1) }
        findViewById<com.google.android.material.card.MaterialCardView>(R.id.btnAumentarExemplares)
            ?.setOnClickListener { atualizarExemplares(1) }

        // Botão Apagar Mídia
        findViewById<Button>(R.id.btnApagarMidia)?.setOnClickListener {
            abrirPopupApagar()
        }
    }

    // ─── CARREGAR DADOS MOCK ──────────────────────────────────────────────────

    private fun carregarDadosLivroMock() {
        // Dados fixos para o protótipo
        findViewById<EditText>(R.id.editTituloLivro)?.setText("Código Limpo")
        findViewById<EditText>(R.id.editAutorLivro)?.setText("Robert C. Martin")
        findViewById<EditText>(R.id.editDescricaoLivro)?.setText("Mesmo um código ruim pode funcionar. Mas se ele não for limpo, pode acabar com uma empresa de desenvolvimento.")
        findViewById<EditText>(R.id.editLingua)?.setText("Português")
        findViewById<EditText>(R.id.editEditora)?.setText("Alta Books")
        findViewById<EditText>(R.id.editDimensao)?.setText("23 x 16 x 2.8 cm")
        findViewById<EditText>(R.id.editISBN10)?.setText("8576082675")
        findViewById<EditText>(R.id.editISBN13)?.setText("978-8576082675")
        findViewById<EditText>(R.id.editData)?.setText("2009")
        findViewById<EditText>(R.id.editPaginas)?.setText("456")
        findViewById<TextView>(R.id.textExemplaresTotal)?.text = "5"

        findViewById<ImageView>(R.id.imgCapaLivroDetalhe)?.setImageResource(R.drawable.osda)

        carregarAvaliacoesMock()
    }

    private fun carregarAvaliacoesMock() {
        val container = findViewById<LinearLayout>(R.id.containerAvaliacoes)
        val textSemAvaliacoes = findViewById<TextView>(R.id.textSemAvaliacoes)

        container?.let {
            for (i in it.childCount - 1 downTo 0) {
                val child = it.getChildAt(i)
                if (child.id != R.id.textSemAvaliacoes) it.removeView(child)
            }
        }
        textSemAvaliacoes?.visibility = View.VISIBLE
    }

    // ─── EDIÇÃO DE CAMPOS ─────────────────────────────────────────────────────

    private fun configurarEdicao(botaoId: Int, campoId: Int) {
        val botao = findViewById<ImageView>(botaoId)
        val campo = findViewById<EditText>(campoId)

        botao?.setOnClickListener {
            if (campo?.isEnabled == false) {
                campo.isEnabled = true
                campo.requestFocus()
                if (campo.text != null) {
                    campo.setSelection(campo.text.length)
                }
                botao.setImageResource(android.R.drawable.ic_menu_save)
                Toast.makeText(this, "Editando campo...", Toast.LENGTH_SHORT).show()
            } else {
                campo?.isEnabled = false
                botao.setImageResource(R.drawable.ic_edit_pencil)
                Toast.makeText(this, "Campo atualizado com sucesso!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ─── CONTROLE DE EXEMPLARES ───────────────────────────────────────────────

    private fun atualizarExemplares(delta: Int) {
        val tvTotal = findViewById<TextView>(R.id.textExemplaresTotal)
        val atual   = tvTotal.text.toString().toIntOrNull() ?: 0
        val novo    = (atual + delta).coerceAtLeast(0)

        tvTotal.text = novo.toString()
        Toast.makeText(this, "Quantidade de exemplares atualizada", Toast.LENGTH_SHORT).show()
    }

    // ─── POPUP APAGAR ─────────────────────────────────────────────────────────

    private fun abrirPopupApagar() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_apagar_midia)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val btnConfirmar = dialog.findViewById<MaterialButton>(R.id.btnConfirmarApagar)
        val btnCancelar  = dialog.findViewById<MaterialButton>(R.id.btnCancelarApagar)

        btnConfirmar?.setOnClickListener {
            Toast.makeText(this, "Mídia removida com sucesso!", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
            finish()
        }

        btnCancelar?.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
}
