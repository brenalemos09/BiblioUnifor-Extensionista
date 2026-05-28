package com.example.bibliounifornew.features.usuario.biblioteca

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R
import com.example.bibliounifornew.features.usuario.amigo.TelaRF17Amigos
import com.example.bibliounifornew.features.usuario.livro.TelaRF11TelaDePesquisa
import com.example.bibliounifornew.features.usuario.livro.TelaRF12TelaDoLivro
import com.example.bibliounifornew.features.usuario.livro.TelaRF16ListaDesejosActivity
import com.example.bibliounifornew.features.usuario.notificacao.TelaRF20Notificacoes
import com.example.bibliounifornew.features.usuario.perfil.NavigationHelper
import com.example.bibliounifornew.features.usuario.perfil.TelaRF09Configuracao
import com.example.bibliounifornew.features.usuario.solicitacao.TelaRF18StatusAluguel
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView

class TelaRF08DashboardUsuario : AppCompatActivity() {

    private lateinit var imagePerfil: ShapeableImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf08_dashboardusuario)

        val textNomeUsuario = findViewById<TextView>(R.id.textNomeUsuario)
        imagePerfil         = findViewById(R.id.imagePerfilUsuario)

        // PROTÓTIPO: Dados Mockados
        textNomeUsuario?.text = "João da Silva"
        imagePerfil.setImageResource(R.drawable.user_placeholder)
        carregarLivrosDescobrirMock()

        // ─── CLIQUE NA FOTO PARA TROCAR (PROTÓTIPO) ───────────────────────────
        imagePerfil.setOnClickListener {
            Toast.makeText(this, "Funcionalidade de trocar foto disponível no sistema real", Toast.LENGTH_SHORT).show()
        }

        // ─── NAVEGAÇÃO ────────────────────────────────────────────────────────
        val btnConfig         = findViewById<ImageView>(R.id.btnConfig)
        val btnNotificacao    = findViewById<ImageView>(R.id.btnNotificacao)
        val btnPesquisarLivros        = findViewById<MaterialButton>(R.id.btnPesquisarLivros)
        val btnMinhaLivrariaDashboard = findViewById<MaterialButton>(R.id.btnMinhaLivraria)
        val btnListaDesejo            = findViewById<MaterialButton>(R.id.btnListaDesejos)
        val btnAmigosDashboard        = findViewById<MaterialButton>(R.id.btnAmigos)
        val btnHistoricoDashboard     = findViewById<MaterialButton>(R.id.btnHistorico)
        val btnStatusAluguel          = findViewById<MaterialButton>(R.id.btnStatusAluguel)
        val btnSair                   = findViewById<MaterialButton>(R.id.btnSairConta)

        btnConfig.setOnClickListener         { startActivity(Intent(this, TelaRF09Configuracao::class.java)) }
        btnNotificacao.setOnClickListener    { startActivity(Intent(this, TelaRF20Notificacoes::class.java)) }
        btnPesquisarLivros.setOnClickListener        { startActivity(Intent(this, TelaRF11TelaDePesquisa::class.java)) }
        btnMinhaLivrariaDashboard.setOnClickListener { startActivity(Intent(this, TelaRF15MinhaLivrariaActivity::class.java)) }
        btnListaDesejo.setOnClickListener            { startActivity(Intent(this, TelaRF16ListaDesejosActivity::class.java)) }
        btnAmigosDashboard.setOnClickListener        { startActivity(Intent(this, TelaRF17Amigos::class.java)) }
        btnHistoricoDashboard.setOnClickListener     { startActivity(Intent(this, TelaRF21Historico::class.java)) }
        btnStatusAluguel.setOnClickListener          { startActivity(Intent(this, TelaRF18StatusAluguel::class.java)) }
        btnSair.setOnClickListener                   { showExitPopup() }

        NavigationHelper.configurarBarraNavegacao(this)
    }

    private fun carregarLivrosDescobrirMock() {
        val container = findViewById<LinearLayout>(R.id.containerDescobrir) ?: return
        container.removeAllViews()

        val livrosMock = listOf(
            Triple("Código Limpo", "Robert C. Martin", R.drawable.osda),
            Triple("Arquitetura Limpa", "Robert C. Martin", R.drawable.osda),
            Triple("Design Patterns", "Erich Gamma", R.drawable.osda),
            Triple("Kotlin em Ação", "Dmitry Jemerov", R.drawable.osda)
        )

        for (livro in livrosMock) {
            val cardView  = layoutInflater.inflate(R.layout.item_livro_descobrir, container, false)
            val imgCapa   = cardView.findViewById<ImageView>(R.id.imgCapaDescobrir)
            val txtTitulo = cardView.findViewById<TextView>(R.id.txtTituloDescobrir)
            val txtAutor  = cardView.findViewById<TextView>(R.id.txtAutorDescobrir)

            imgCapa.setImageResource(livro.third)
            txtTitulo.text = livro.first
            txtAutor.text  = livro.second

            cardView.setOnClickListener {
                startActivity(
                    Intent(this, TelaRF12TelaDoLivro::class.java)
                        .putExtra("LIVRO_ID", "mock_id")
                )
            }
            container.addView(cardView)
        }
    }

    private fun showExitPopup() {
        val dialogView = layoutInflater.inflate(R.layout.popup_sair_conta, null)
        val builder    = AlertDialog.Builder(this)
        builder.setView(dialogView)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogView.findViewById<MaterialButton>(R.id.btnConfirmarSair).setOnClickListener {
            dialog.dismiss()
            // Protótipo: Apenas volta para a tela inicial
            val intentSair = Intent(this, com.example.bibliounifornew.login.TelaRF01BemVindo::class.java)
            intentSair.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intentSair)
            finish()
        }

        dialogView.findViewById<TextView>(R.id.btnCancelarSair).setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }
}
