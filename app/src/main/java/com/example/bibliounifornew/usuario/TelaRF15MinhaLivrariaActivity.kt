package com.example.bibliounifornew.usuario

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.AuthRepository
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException

class TelaRF15MinhaLivrariaActivity : AppCompatActivity() {

    private val authRepository = AuthRepository()
    private val db             = FirebaseFirestore.getInstance()

    private lateinit var adapter   : LivrariaAdapter
    private val listaLivraria      = mutableListOf<ItemLivraria>()
    private var usuarioId          : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf15_minha_livraria)

        // ─── AUTENTICAÇÃO ─────────────────────────────────────────────────────
        val usuarioAtual = authRepository.getUsuarioAtual()
        if (usuarioAtual != null) {
            usuarioId = usuarioAtual.uid
            findViewById<android.widget.TextView>(R.id.textEmailLivraria)?.text = usuarioAtual.email
        } else {
            startActivity(Intent(this, com.example.bibliounifornew.login.TelaRF03LoginAluno::class.java))
            finish()
            return
        }

        // ─── FILTRO ────────────────────────────────────────────────────────────
        findViewById<android.widget.ImageView>(R.id.imgFiltroStatus)?.setOnClickListener {
            abrirPopupFiltro()
        }

        // ─── RECYCLERVIEW ─────────────────────────────────────────────────────
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewLivraria)
        adapter = LivrariaAdapter(listaLivraria) { item, position ->
            removerLivro(item, position)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // ─── CONSULTA FIRESTORE ───────────────────────────────────────────────
        carregarLivraria()
    }

    /**
     * Busca os livros do usuário na coleção biblioteca_usuarios.
     * Documentos têm ID padrão {uid}_{livroId} e campos: usuarioId, livroId, titulo,
     * autor, statusLeitura, adicionadoEm.
     */
    private fun carregarLivraria() {
        db.collection("biblioteca_usuarios")
            .whereEqualTo("usuarioId", usuarioId)
            .get()
            .addOnSuccessListener { result ->
                listaLivraria.clear()
                for (doc in result) {
                    val livroId = doc.getString("livroId")
                        ?: doc.id.substringAfter("${usuarioId}_")
                    val item = ItemLivraria(
                        livroId       = livroId,
                        titulo        = doc.getString("titulo")        ?: "",
                        autor         = doc.getString("autor")         ?: "",
                        statusLeitura = doc.getString("statusLeitura") ?: "Não Lido"
                    )
                    listaLivraria.add(item)
                }
                adapter.notifyDataSetChanged()
                if (listaLivraria.isEmpty()) {
                    Toast.makeText(this, "Sua livraria está vazia. Adicione livros pela tela do livro.", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { e ->
                val msg = if (e is FirebaseFirestoreException)
                    "Erro ao carregar livraria. Tente novamente."
                else
                    "Sem conexão ou permissão negada."
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Remove o livro de biblioteca_usuarios e atualiza o RecyclerView localmente.
     */
    private fun removerLivro(item: ItemLivraria, position: Int) {
        val docId = "${usuarioId}_${item.livroId}"
        db.collection("biblioteca_usuarios").document(docId)
            .delete()
            .addOnSuccessListener {
                adapter.removerItem(position)
                Toast.makeText(this, "\"${item.titulo.ifEmpty { item.livroId }}\" removido da sua livraria.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Falha ao remover. Tente novamente.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun abrirPopupFiltro() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.popup_filtrar_midia)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialog.findViewById<MaterialButton>(R.id.btnLimparFiltro)?.setOnClickListener { dialog.dismiss() }
        dialog.findViewById<MaterialButton>(R.id.btnSalvarFiltro)?.setOnClickListener {
            Toast.makeText(this, "Filtro aplicado.", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }
}
