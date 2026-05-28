package com.example.bibliounifornew.features.usuario.notificacao

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.Notificacao
import com.example.bibliounifornew.features.usuario.livro.TelaRF12TelaDoLivro

class TelaRF20Notificacoes : AppCompatActivity() {

    private lateinit var adapter  : NotificacaoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf20_notificacoes)

        // ─── CABEÇALHO ────────────────────────────────────────────────────────
        configurarCabecalho()

        // ─── RECYCLERVIEW ─────────────────────────────────────────────────────
        configurarRecyclerView()
    }

    private fun configurarCabecalho() {
        val textNome    = findViewById<TextView>(R.id.textNomeNotif)
        val imagePerfil = findViewById<ImageView>(R.id.imagePerfilNotif)

        textNome?.text = "João Silva"
        imagePerfil?.setImageResource(R.drawable.user_placeholder)
    }

    private fun configurarRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewNotificacoes)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = NotificacaoAdapter(
            lista        = mutableListOf(),
            onNotifClick = { notif ->
                if (notif.livroId.isNotEmpty()) {
                    val intent = Intent(this, TelaRF12TelaDoLivro::class.java)
                        .putExtra("LIVRO_ID", notif.livroId)
                    startActivity(intent)
                }
            }
        )
        recyclerView.adapter = adapter

        carregarNotificacoesMock()

        // ── Swipe-to-dismiss ──────────────────────────────────────────────────
        val swipeHandler = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                rv         : RecyclerView,
                viewHolder : RecyclerView.ViewHolder,
                target     : RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                adapter.removerItem(position)
                Toast.makeText(this@TelaRF20Notificacoes, "Notificação descartada", Toast.LENGTH_SHORT).show()
            }
        }
        ItemTouchHelper(swipeHandler).attachToRecyclerView(recyclerView)
    }

    private fun carregarNotificacoesMock() {
        val mockData = listOf(
            Notificacao(
                id = "1",
                titulo = "Livro Disponível!",
                descricao = "O livro '1984' que você desejava está disponível para aluguel.",
                timestamp = System.currentTimeMillis(),
                lida = false,
                livroId = "9788535914061",
                autor = "George Orwell",
                coverUrl = "https://m.media-amazon.com/images/I/91SZS6B7-CL.jpg"
            ),
            Notificacao(
                id = "2",
                titulo = "Devolução Próxima",
                descricao = "Lembre-se de devolver 'O Pequeno Príncipe' em 2 dias.",
                timestamp = System.currentTimeMillis() - 3600000,
                lida = true,
                livroId = "9788533302273",
                autor = "Antoine de Saint-Exupéry",
                coverUrl = "https://m.media-amazon.com/images/I/8179u87mZ+L.jpg"
            ),
            Notificacao(
                id = "3",
                titulo = "Novo Livro Adicionado",
                descricao = "Confira a nova obra de Martin Fowler em nosso acervo.",
                timestamp = System.currentTimeMillis() - 86400000,
                lida = false,
                livroId = "9788575227244",
                autor = "Martin Fowler",
                coverUrl = "https://m.media-amazon.com/images/I/41-S6T6A6vL.jpg"
            )
        )
        adapter.atualizarLista(mockData)
    }
}
