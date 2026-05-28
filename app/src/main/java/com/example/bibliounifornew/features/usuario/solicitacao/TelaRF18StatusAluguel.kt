package com.example.bibliounifornew.features.usuario.solicitacao

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.ImageView
import com.example.bibliounifornew.R
import com.example.bibliounifornew.features.usuario.perfil.NavigationHelper

class TelaRF18StatusAluguel : AppCompatActivity() {

    private lateinit var adapter  : StatusAluguelAdapter
    private val listaAlugueis     = mutableListOf<ItemAluguel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf18_status_aluguel)

        // ─── RECYCLER VIEW ────────────────────────────────────────────────────
        val recycler = findViewById<RecyclerView>(R.id.recyclerStatusAluguel)
        adapter = StatusAluguelAdapter(
            lista     = listaAlugueis,
            onRenovar = { item ->
                startActivity(
                    Intent(this, TelaRF18CalendarioRenovacao::class.java)
                        .putExtra("LIVRO_ID", item.livroId)
                        .putExtra("TITULO",   item.titulo)
                )
            }
        )
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        // ─── CABEÇALHO ────────────────────────────────────────────────────────
        val textNome    = findViewById<TextView>(R.id.textNomeUsuarioAlugados)
        textNome?.text = "Aluno Unifor"

        carregarAlugueisMock()

        NavigationHelper.configurarBarraNavegacao(this)
    }

    private fun carregarAlugueisMock() {
        listaAlugueis.clear()
        listaAlugueis.add(ItemAluguel(
            docId = "a1",
            livroId = "1",
            titulo = "O Senhor dos Anéis",
            status = "ativo",
            dataDevolucao = System.currentTimeMillis() + 604800000L, // +7 dias
            dataSolicitacao = System.currentTimeMillis() - 86400000L // -1 dia
        ))
        listaAlugueis.add(ItemAluguel(
            docId = "a2",
            livroId = "2",
            titulo = "1984",
            status = "pendente",
            dataDevolucao = 0L,
            dataSolicitacao = System.currentTimeMillis()
        ))
        adapter.notifyDataSetChanged()
    }
}
