package com.example.bibliounifornew.features.adm.solicitacoes

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.R
import com.example.bibliounifornew.features.adm.gerenciamento.NavigationHelperADM
import com.example.bibliounifornew.features.adm.gerenciamento.TelaRF30UsuariosParaADM
import com.example.bibliounifornew.features.adm.gerenciamento.TelaRF37InfoLivroADM

class TelaRF36ListaAlugueisADM : AppCompatActivity() {

    private lateinit var adapter: AlugueisAdapter
    private val listaAlugueis = mutableListOf<ItemAluguel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf36_lista_alugueis_adm)

        // ─── RECYCLERVIEW ─────────────────────────────────────────────────────
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewAlugueis)
        adapter = AlugueisAdapter(
            listaAlugueis,
            onVerLivro    = { item ->
                val intent = Intent(this, TelaRF37InfoLivroADM::class.java)
                intent.putExtra("LIVRO_ID", item.idLivro)
                startActivity(intent)
            },
            onVerUsuario  = { item ->
                val intent = Intent(this, TelaRF30UsuariosParaADM::class.java)
                intent.putExtra("USUARIO_ID", item.uidAluno)
                startActivity(intent)
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        carregarAlugueisMock()

        NavigationHelperADM.configurarBarraNavegacao(this)
    }

    /**
     * Carrega aluguéis mockados para o protótipo.
     */
    private fun carregarAlugueisMock() {
        val mockData = listOf(
            ItemAluguel("1", "u1", "l1", System.currentTimeMillis() - 86400000 * 2, "ativo", "João Silva", "O Hobbit", "J.R.R. Tolkien"),
            ItemAluguel("2", "u2", "l2", System.currentTimeMillis() - 86400000 * 5, "atrasado", "Maria Oliveira", "1984", "George Orwell"),
            ItemAluguel("3", "u3", "l3", System.currentTimeMillis() - 86400000 * 1, "pendente", "Carlos Santos", "Dom Casmurro", "Machado de Assis")
        )
        adapter.atualizarLista(mockData)
    }

    private fun carregarAlugueis() {
        carregarAlugueisMock()
    }
}
