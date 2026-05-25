package com.example.bibliounifornew.usuario

import android.app.Dialog
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
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class TelaRF16ListaDesejosActivity : AppCompatActivity() {

    private val authRepository    = AuthRepository()
    private val usuarioRepository = UsuarioRepository()
    private val db                = FirebaseFirestore.getInstance()

    private lateinit var adapter  : ListaDesejosAdapter
    private val listaDesejos      = mutableListOf<ItemListaDesejos>()
    private var usuarioId         : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf16_lista_desejos)

        // ─── RECYCLER VIEW ────────────────────────────────────────────────────
        val recycler = findViewById<RecyclerView>(R.id.recyclerListaDesejos)
        adapter = ListaDesejosAdapter(
            lista      = listaDesejos,
            onLivraria = { item -> adicionarNaLivraria(item) },
            onAlugar   = { item -> if (item.disponivel) showPopupAlugar(item) else
                           Toast.makeText(this, "\"${item.titulo}\" está indisponível no momento.", Toast.LENGTH_SHORT).show() },
            onExcluir  = { item, pos -> excluirDaLista(item, pos) }
        )
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        // ─── CABEÇALHO ────────────────────────────────────────────────────────
        val textNome    = findViewById<TextView>(R.id.textNomeUsuarioDesejos)
        val imagePerfil = findViewById<ImageView?>(R.id.imageUsuarioDesejos)
        val usuarioAtual = authRepository.getUsuarioAtual()

        if (usuarioAtual != null) {
            usuarioId = usuarioAtual.uid
            textNome?.text = "Carregando..."

            usuarioRepository.buscarPerfilUsuario(usuarioId) { sucesso, dados, _ ->
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
                }
            }

            carregarListaDesejos()
        } else {
            startActivity(Intent(this, com.example.bibliounifornew.login.TelaRF03LoginAluno::class.java))
            finish()
            return
        }

        NavigationHelper.configurarBarraNavegacao(this)
    }

    // ─── CARREGAR DO FIRESTORE ────────────────────────────────────────────────

    private fun carregarListaDesejos() {
        db.collection("lista_desejos")
            .whereEqualTo("usuarioId", usuarioId)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    adapter.atualizarLista(emptyList())
                    return@addOnSuccessListener
                }

                val total       = result.size()
                var processados = 0
                val listaTemp   = mutableListOf<ItemListaDesejos>()

                for (doc in result) {
                    val livroId = doc.getString("livroId") ?: ""
                    val titulo  = doc.getString("titulo")  ?: ""
                    val autor   = doc.getString("autor")   ?: ""
                    val docId   = doc.id

                    if (livroId.isEmpty()) {
                        processados++
                        if (processados == total) adapter.atualizarLista(listaTemp)
                        continue
                    }

                    // Join com coleção livros para pegar capa e estoque
                    db.collection("livros").document(livroId).get()
                        .addOnSuccessListener { livroDoc ->
                            val coverUrl  = livroDoc.getString("coverUrl") ?: ""
                            val estoque   = livroDoc.getLong("estoque")
                                ?: livroDoc.getLong("quantidade")
                                ?: livroDoc.getLong("stock")
                                ?: 0L
                            val tituloFinal = titulo.ifEmpty {
                                livroDoc.getString("title") ?: livroDoc.getString("titulo") ?: livroId
                            }
                            val autorFinal = autor.ifEmpty {
                                livroDoc.getString("author") ?: livroDoc.getString("autor") ?: ""
                            }
                            listaTemp.add(
                                ItemListaDesejos(
                                    docId      = docId,
                                    livroId    = livroId,
                                    titulo     = tituloFinal,
                                    autor      = autorFinal,
                                    coverUrl   = coverUrl,
                                    disponivel = estoque > 0L
                                )
                            )
                            processados++
                            if (processados == total) adapter.atualizarLista(listaTemp)
                        }
                        .addOnFailureListener {
                            listaTemp.add(
                                ItemListaDesejos(docId = docId, livroId = livroId, titulo = titulo, autor = autor)
                            )
                            processados++
                            if (processados == total) adapter.atualizarLista(listaTemp)
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao carregar lista de desejos: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // ─── AÇÕES ────────────────────────────────────────────────────────────────

    private fun adicionarNaLivraria(item: ItemListaDesejos) {
        val dados = hashMapOf(
            "usuarioId"     to usuarioId,
            "livroId"       to item.livroId,
            "titulo"        to item.titulo,
            "autor"         to item.autor,
            "statusLeitura" to "Não Lido",
            "adicionadoEm"  to System.currentTimeMillis()
        )
        db.collection("biblioteca_usuarios").document("${usuarioId}_${item.livroId}")
            .set(dados, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(this, "\"${item.titulo}\" adicionado à sua Livraria!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao adicionar à Livraria.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun excluirDaLista(item: ItemListaDesejos, position: Int) {
        usuarioRepository.removerDaListaDesejos(usuarioId, item.livroId) { sucesso ->
            adapter.removerItem(position)
            val msg = if (sucesso) "\"${item.titulo}\" removido da lista de desejos."
                      else "Removido localmente (falha no servidor)."
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
    }

    // ─── POPUPS ───────────────────────────────────────────────────────────────

    private fun showPopupAlugar(item: ItemListaDesejos) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.popup_alugar_livro)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnAlugar   = dialog.findViewById<MaterialButton>(R.id.buttonAdicionarLivro)
        val btnCancelar = dialog.findViewById<TextView>(R.id.textCancelarPopup)

        btnAlugar?.setOnClickListener {
            dialog.dismiss()
            showPopupLivroAdicionado()
        }
        btnCancelar?.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun showPopupLivroAdicionado() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.popup_livro_adicionado)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnVerMeusLivros = dialog.findViewById<MaterialButton>(R.id.buttonVerMeusLivros)
        btnVerMeusLivros?.setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(this, TelaRF18StatusAluguel::class.java))
        }
        dialog.show()
    }
}
