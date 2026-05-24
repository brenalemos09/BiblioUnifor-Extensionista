package com.example.bibliounifornew.usuario

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.AuthRepository
import com.example.bibliounifornew.data.Notificacao
import com.example.bibliounifornew.data.UsuarioRepository
import com.google.firebase.firestore.ListenerRegistration

class TelaRF20Notificacoes : AppCompatActivity() {

    private val authRepository    = AuthRepository()
    private val usuarioRepository = UsuarioRepository()
    private lateinit var adapter  : NotificacaoAdapter

    // SnapshotListener — cancelado em onDestroy para evitar memory leak
    private var snapshotListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf20_notificacoes)

        // ─── CABEÇALHO COM ATUALIZAÇÃO EM TEMPO REAL ──────────────────────────
        val textNomeUsuario = findViewById<TextView>(R.id.textNomeNotif)
        val usuarioAtual    = authRepository.getUsuarioAtual()

        if (usuarioAtual != null) {
            textNomeUsuario?.text = "Carregando..."
            snapshotListener = usuarioRepository.observarPerfilUsuario(usuarioAtual.uid) { dados ->
                textNomeUsuario?.text = dados?.get("nome") as? String ?: "Usuário"
            }
        } else {
            startActivity(Intent(this, com.example.bibliounifornew.login.TelaRF03LoginAluno::class.java))
            finish()
            return
        }

        // ─── DADOS DE NOTIFICAÇÃO ─────────────────────────────────────────────
        // Por enquanto populados localmente; o padrão já está pronto para
        // ser substituído por uma query em "notificacoes_usuarios/{uid}" no Firestore.
        val notificacoes: MutableList<Notificacao> = mutableListOf(
            Notificacao(
                id        = "notif_001",
                titulo    = "O Ceifador",
                autor     = "Neal Shusterman",
                descricao = "Seu livro alugado está disponível para retirada. Prazo: 3 dias úteis.",
                tempo     = "Hoje",
                lida      = true,
                capaResId = R.drawable.o_alienista_capa
            ),
            Notificacao(
                id        = "notif_002",
                titulo    = "O Senhor dos Anéis",
                autor     = "J.R.R. Tolkien",
                descricao = "Seu empréstimo vence em 2 dias. Renove agora para evitar multas.",
                tempo     = "Ontem",
                lida      = false,
                capaResId = R.drawable.osda
            )
        )

        // ─── RECYCLERVIEW ─────────────────────────────────────────────────────
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewNotificacoes)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = NotificacaoAdapter(notificacoes)
        recyclerView.adapter = adapter

        // ─── SWIPE-TO-DISMISS COM ANIMAÇÃO ────────────────────────────────────
        val swipeHandler = object : ItemTouchHelper.SimpleCallback(
            0, // Sem drag (reordenação desativada)
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT // Swipe em ambas as direções
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false // Não usamos drag-and-drop

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                if (position != RecyclerView.NO_ID.toInt()) {
                    adapter.removerItem(position)
                    Toast.makeText(
                        this@TelaRF20Notificacoes,
                        "Notificação descartada",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        ItemTouchHelper(swipeHandler).attachToRecyclerView(recyclerView)
    }

    override fun onDestroy() {
        super.onDestroy()
        snapshotListener?.remove()
    }
}
