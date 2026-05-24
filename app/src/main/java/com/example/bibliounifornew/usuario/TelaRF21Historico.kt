package com.example.bibliounifornew.usuario

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.AuthRepository
import com.example.bibliounifornew.data.UsuarioRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query

class TelaRF21Historico : AppCompatActivity() {

    private val authRepository    = AuthRepository()
    private val usuarioRepository = UsuarioRepository()
    private val db                = FirebaseFirestore.getInstance()
    private lateinit var adapter  : HistoricoAdapter
    private val listaHistorico    = mutableListOf<ItemHistorico>()
    private var usuarioId         : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf21_historico)

        // ─── AUTENTICAÇÃO ─────────────────────────────────────────────────────
        val textCabecalho = findViewById<TextView>(R.id.textEmailHistorico)
        val usuarioAtual  = authRepository.getUsuarioAtual()

        if (usuarioAtual != null) {
            usuarioId          = usuarioAtual.uid
            textCabecalho?.text = usuarioAtual.email
        } else {
            startActivity(Intent(this, com.example.bibliounifornew.login.TelaRF03LoginAluno::class.java))
            finish()
            return
        }

        // ─── RECYCLERVIEW ─────────────────────────────────────────────────────
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewHistorico)
        adapter = HistoricoAdapter(listaHistorico) { item, position ->
            showPopupRemover(item.titulo) {
                usuarioRepository.removerDoHistorico(usuarioId, item.livroId) { sucesso ->
                    if (sucesso) {
                        adapter.removerItem(position)
                    } else {
                        Toast.makeText(this, "Falha ao remover. Tente novamente.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // ─── CONSULTA FIRESTORE ───────────────────────────────────────────────
        carregarHistorico()
    }

    private fun carregarHistorico() {
        db.collection("historico_usuarios")
            .whereEqualTo("usuarioId", usuarioId)
            .orderBy("adicionadoEm", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                listaHistorico.clear()
                for (document in result) {
                    val livroId = document.getString("livroId")
                        ?: document.id.substringAfter("${usuarioId}_")
                    val item = ItemHistorico(
                        livroId  = livroId,
                        titulo   = document.getString("titulo") ?: "",
                        autor    = document.getString("autor")  ?: "",
                        dataLido = document.getLong("adicionadoEm") ?: 0L
                    )
                    listaHistorico.add(item)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                val mensagem = if (e is FirebaseFirestoreException) {
                    "Histórico indisponível. Configure o índice no Firestore Console."
                } else {
                    "Erro ao carregar histórico."
                }
                Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show()
            }
    }

    private fun showPopupRemover(nomeLivro: String, onConfirm: () -> Unit) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_remover_historico)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val textMensagem = dialog.findViewById<TextView>(R.id.textMensagemPopupRemoverHistorico)
        val btnConfirmar = dialog.findViewById<Button>(R.id.buttonPopupRemoverHistorico)
        val btnCancelar  = dialog.findViewById<TextView>(R.id.textCancelarPopupRemoverHistorico)

        textMensagem?.text = "Tem certeza que deseja remover \"$nomeLivro\" do seu histórico?"

        btnConfirmar?.setOnClickListener {
            onConfirm()
            dialog.dismiss()
        }

        btnCancelar?.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }
}
