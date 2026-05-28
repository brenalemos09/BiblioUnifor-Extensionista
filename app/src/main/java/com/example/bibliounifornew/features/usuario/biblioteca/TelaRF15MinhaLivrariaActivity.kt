package com.example.bibliounifornew.features.usuario.biblioteca

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.R
import com.example.bibliounifornew.features.usuario.perfil.NavigationHelper

class TelaRF15MinhaLivrariaActivity : AppCompatActivity() {

    private lateinit var adapter: LivrariaAdapter
    private val listaLivraria = mutableListOf<ItemLivraria>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf15_minha_livraria)

        // ─── DADOS MOCKADOS ──────────────────────────────────────────────────
        findViewById<TextView>(R.id.textEmailLivraria)?.text = "aluno@unifor.br"

        // ─── BARRA DE NAVEGAÇÃO FIXA ──────────────────────────────────────────
        try {
            NavigationHelper.configurarBarraNavegacao(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // ─── RECYCLERVIEW DINÂMICO ─────────────────────────────────────────────
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewLivraria)
        adapter = LivrariaAdapter(listaLivraria) { item, position ->
            removerLivroMock(item, position)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // ─── CARREGAR MOCK ───────────────────────────────────────────────────
        carregarLivrariaMock()
    }

    private fun carregarLivrariaMock() {
        listaLivraria.clear()
        listaLivraria.add(ItemLivraria("1", "O Senhor dos Anéis", "J.R.R. Tolkien", "Lendo"))
        listaLivraria.add(ItemLivraria("2", "1984", "George Orwell", "Lido"))
        listaLivraria.add(ItemLivraria("3", "O Pequeno Príncipe", "Antoine de Saint-Exupéry", "Não Lido"))
        adapter.notifyDataSetChanged()
    }

    private fun removerLivroMock(item: ItemLivraria, position: Int) {
        adapter.removerItem(position)
        Toast.makeText(this, "\"${item.titulo}\" removido (Local).", Toast.LENGTH_SHORT).show()
    }
}
