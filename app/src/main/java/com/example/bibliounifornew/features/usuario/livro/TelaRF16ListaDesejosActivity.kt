package com.example.bibliounifornew.features.usuario.livro

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.ImageView
import android.graphics.drawable.ColorDrawable
import android.graphics.Color
import coil.load
import com.example.bibliounifornew.R
import com.example.bibliounifornew.features.usuario.perfil.NavigationHelper

class TelaRF16ListaDesejosActivity : AppCompatActivity() {

    private lateinit var adapter  : ListaDesejosAdapter
    private val listaDesejos      = mutableListOf<ItemListaDesejos>()
    private var usuarioId         : String = "mock_user_123"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf16_lista_desejos)

        // ─── RECYCLER VIEW ────────────────────────────────────────────────────
        val recycler = findViewById<RecyclerView>(R.id.recyclerListaDesejos)
        adapter = ListaDesejosAdapter(
            lista      = listaDesejos,
            onLivraria = { item -> adicionarNaLivraria(item) },
            onAlugar   = { item -> 
                if (item.disponivel) {
                    showPopupAlugar(item)
                } else {
                    Toast.makeText(this, "Sinto muito, \"${item.titulo}\" está indisponível no momento.", Toast.LENGTH_LONG).show()
                }
            },
            onExcluir  = { item, pos -> excluirDaLista(item, pos) }
        )
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        // ─── CABEÇALHO ────────────────────────────────────────────────────────
        val textNome    = findViewById<TextView>(R.id.textNomeUsuarioDesejos)
        val imagePerfil = findViewById<ImageView?>(R.id.imageUsuarioDesejos)
        
        // Mock User Profile
        textNome?.text = "João Silva"
        imagePerfil?.setImageResource(R.drawable.user_placeholder)

        carregarListaDesejos()

        NavigationHelper.configurarBarraNavegacao(this)
    }

    // ─── CARREGAR MOCK DATA ──────────────────────────────────────────────────

    private fun carregarListaDesejos() {
        val mockData = listOf(
            ItemListaDesejos(
                docId = "1",
                livroId = "9788535914061",
                titulo = "1984",
                autor = "George Orwell",
                coverUrl = "https://m.media-amazon.com/images/I/91SZS6B7-CL.jpg",
                disponivel = true
            ),
            ItemListaDesejos(
                docId = "2",
                livroId = "9788533302273",
                titulo = "O Pequeno Príncipe",
                autor = "Antoine de Saint-Exupéry",
                coverUrl = "https://m.media-amazon.com/images/I/8179u87mZ+L.jpg",
                disponivel = false
            ),
            ItemListaDesejos(
                docId = "3",
                livroId = "9788535914849",
                titulo = "A Revolução dos Bichos",
                autor = "George Orwell",
                coverUrl = "https://m.media-amazon.com/images/I/91BsAdSBFML.jpg",
                disponivel = true
            )
        )
        adapter.atualizarLista(mockData)
    }

    // ─── AÇÕES ────────────────────────────────────────────────────────────────

    private fun adicionarNaLivraria(item: ItemListaDesejos) {
        Toast.makeText(this, "\"${item.titulo}\" adicionado à sua Livraria!", Toast.LENGTH_SHORT).show()
    }

    private fun excluirDaLista(item: ItemListaDesejos, position: Int) {
        adapter.removerItem(position)
        Toast.makeText(this, "\"${item.titulo}\" removido da lista de desejos.", Toast.LENGTH_SHORT).show()
    }

    // ─── POPUPS ───────────────────────────────────────────────────────────────

    private fun showPopupAlugar(item: ItemListaDesejos) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_alugar_livro)
        
        // Garante fundo transparente e centralização
        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(
                (resources.displayMetrics.widthPixels * 0.90).toInt(),
                WindowManager.LayoutParams.WRAP_CONTENT
            )
        }

        val txtTitulo   = dialog.findViewById<TextView>(R.id.textTituloPopupAlugar)
        val btnAlugar   = dialog.findViewById<com.google.android.material.button.MaterialButton>(R.id.buttonAdicionarLivro)
        val btnCancelar = dialog.findViewById<TextView>(R.id.textCancelarPopup)

        // Título formatado com aspas
        txtTitulo?.text = "Você deseja alugar o livro\n\"${item.titulo}\"?"

        btnAlugar?.setOnClickListener {
            dialog.dismiss()
            showPopupLivroAdicionado()
        }
        
        btnCancelar?.setOnClickListener { 
            dialog.dismiss() 
        }
        
        dialog.show()
    }

    private fun showPopupLivroAdicionado() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_livro_adicionado)
        
        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(
                (resources.displayMetrics.widthPixels * 0.90).toInt(),
                WindowManager.LayoutParams.WRAP_CONTENT
            )
        }

        val btnVerMeusLivros = dialog.findViewById<com.google.android.material.button.MaterialButton>(R.id.buttonVerMeusLivros)
        btnVerMeusLivros?.setOnClickListener {
            dialog.dismiss()
            // Direciona para Status de Aluguel (RF18) como na imagem
            val intent = Intent(this, com.example.bibliounifornew.features.usuario.solicitacao.TelaRF18StatusAluguel::class.java)
            startActivity(intent)
            finish()
        }
        dialog.show()
    }
}
