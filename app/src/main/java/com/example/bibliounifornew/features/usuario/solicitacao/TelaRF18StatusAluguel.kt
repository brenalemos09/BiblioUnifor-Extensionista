package com.example.bibliounifornew.features.usuario.solicitacao

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.ImageView
import coil.load
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.AuthRepository
import com.example.bibliounifornew.data.UsuarioRepository
import com.example.bibliounifornew.features.usuario.perfil.NavigationHelper
import com.google.firebase.firestore.FirebaseFirestore

class TelaRF18StatusAluguel : AppCompatActivity() {

    private val authRepository    = AuthRepository()
    private val usuarioRepository = UsuarioRepository()
    private val db                = FirebaseFirestore.getInstance()

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
        val imagePerfil = findViewById<ImageView?>(R.id.imagePerfilAlugados)
        val usuarioAtual = authRepository.getUsuarioAtual()

        if (usuarioAtual != null) {
            textNome?.text = "Carregando..."

            usuarioRepository.buscarPerfilUsuario(usuarioAtual.uid) { sucesso, dados, erro ->
                if (sucesso && dados != null) {
                    textNome?.text = dados["nome"] as? String ?: "Usuário"
                    val fotoUrl = dados["fotoUrl"] as? String ?: ""
                    if (fotoUrl.isNotEmpty()) {
                        imagePerfil?.load(fotoUrl) {
                            placeholder(R.drawable.user_placeholder)
                            error(R.drawable.user_placeholder)
                        }
                    }
                } else {
                    textNome?.text = "Usuário"
                    Toast.makeText(this, "Erro ao carregar perfil: $erro", Toast.LENGTH_SHORT).show()
                }
            }

            carregarAlugueis(usuarioAtual.uid)
        } else {
            startActivity(Intent(this, com.example.bibliounifornew.login.TelaRF03LoginAluno::class.java))
            finish()
            return
        }

        NavigationHelper.configurarBarraNavegacao(this)
    }

    // ─── CARREGAR ALUGUÉIS DO FIRESTORE ──────────────────────────────────────

    private fun carregarAlugueis(uid: String) {
        db.collection("solicitacoes_emprestimo")
            .whereEqualTo("uidAluno", uid)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    adapter.atualizarLista(emptyList())
                    return@addOnSuccessListener
                }

                val total       = result.size()
                var processados = 0
                val listaTemp   = mutableListOf<ItemAluguel>()

                for (doc in result) {
                    val docId           = doc.id
                    val idLivro         = doc.getString("idLivro") ?: doc.getString("livroId") ?: ""
                    val status          = doc.getString("status")           ?: "pendente"
                    val dataDevolucao   = doc.getLong("dataDevolucao")      ?: 0L
                    val dataSolicitacao = doc.getLong("dataSolicitacao")    ?: 0L

                    if (idLivro.isEmpty()) {
                        listaTemp.add(ItemAluguel(docId = docId, status = status,
                            dataDevolucao = dataDevolucao, dataSolicitacao = dataSolicitacao))
                        processados++
                        if (processados == total) adapter.atualizarLista(listaTemp)
                        continue
                    }

                    // Join com coleção livros para título e capa
                    db.collection("livros").document(idLivro).get()
                        .addOnSuccessListener { livroDoc ->
                            val titulo   = livroDoc.getString("title")    ?: livroDoc.getString("titulo") ?: idLivro
                            val coverUrl = livroDoc.getString("coverUrl") ?: ""
                            listaTemp.add(
                                ItemAluguel(
                                    docId           = docId,
                                    livroId         = idLivro,
                                    titulo          = titulo,
                                    coverUrl        = coverUrl,
                                    status          = status,
                                    dataDevolucao   = dataDevolucao,
                                    dataSolicitacao = dataSolicitacao
                                )
                            )
                            processados++
                            if (processados == total) adapter.atualizarLista(listaTemp)
                        }
                        .addOnFailureListener {
                            listaTemp.add(ItemAluguel(docId = docId, livroId = idLivro, status = status,
                                dataDevolucao = dataDevolucao, dataSolicitacao = dataSolicitacao))
                            processados++
                            if (processados == total) adapter.atualizarLista(listaTemp)
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao carregar aluguéis: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
