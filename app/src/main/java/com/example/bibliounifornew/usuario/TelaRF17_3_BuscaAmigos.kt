package com.example.bibliounifornew.usuario

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.AuthRepository
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore

class TelaRF17_3_BuscaAmigos : AppCompatActivity() {

    private val authRepository = AuthRepository()
    private val db             = FirebaseFirestore.getInstance()

    private lateinit var adapter       : BuscaAmigoAdapter
    private val listaCompleta          = mutableListOf<UsuarioAmigo>()
    private val listaFiltrada          = mutableListOf<UsuarioAmigo>()
    private val uidsJaAmigos           = mutableSetOf<String>()
    private var uidAtual               : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf17_3_busca_amigos)

        // ─── AUTENTICAÇÃO ─────────────────────────────────────────────────────
        val usuarioAtual = authRepository.getUsuarioAtual()
        if (usuarioAtual == null) {
            startActivity(Intent(this, com.example.bibliounifornew.login.TelaRF03LoginAluno::class.java))
            finish()
            return
        }
        uidAtual = usuarioAtual.uid

        // ─── RECYCLERVIEW ─────────────────────────────────────────────────────
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewBuscaAmigos)
        adapter = BuscaAmigoAdapter(listaFiltrada) { usuario ->
            enviarSolicitacaoAmizade(usuario)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // ─── BUSCA POR TEXTO ──────────────────────────────────────────────────
        val editBuscar  = findViewById<EditText>(R.id.editBuscarAmigo)
        val btnProcurar = findViewById<MaterialButton>(R.id.buttonBuscarAmigo)

        btnProcurar.setOnClickListener {
            val texto = editBuscar.text.toString().trim().lowercase()
            filtrarLista(texto)
        }

        // ─── CARREGA AMIGOS EXISTENTES, DEPOIS TODOS OS USUÁRIOS ─────────────
        carregarAmigosEUsuarios()
    }

    /**
     * Carrega primeiro a lista de amigos do usuário (para excluir da busca),
     * depois busca todos os usuários no Firestore.
     */
    private fun carregarAmigosEUsuarios() {
        db.collection("usuarios").document(uidAtual).collection("amigos")
            .get()
            .addOnSuccessListener { resultAmigos ->
                uidsJaAmigos.clear()
                uidsJaAmigos.addAll(resultAmigos.documents.map { it.id })

                // Com a lista de amigos em mãos, carregamos todos os usuários
                carregarTodosUsuarios()
            }
            .addOnFailureListener {
                // Mesmo sem a lista de amigos conseguimos buscar usuários (sem filtro de amigo)
                carregarTodosUsuarios()
            }
    }

    private fun carregarTodosUsuarios() {
        db.collection("usuarios")
            .get()
            .addOnSuccessListener { result ->
                listaCompleta.clear()
                for (doc in result.documents) {
                    // Exclui: o próprio usuário e quem já é amigo
                    if (doc.id == uidAtual || doc.id in uidsJaAmigos) continue

                    listaCompleta.add(
                        UsuarioAmigo(
                            uid     = doc.id,
                            nome    = doc.getString("nome")    ?: "Usuário",
                            usuario = doc.getString("usuario") ?: ""
                        )
                    )
                }
                // Exibe todos inicialmente
                filtrarLista("")
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao buscar usuários: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun filtrarLista(texto: String) {
        listaFiltrada.clear()
        if (texto.isEmpty()) {
            listaFiltrada.addAll(listaCompleta)
        } else {
            listaCompleta.filterTo(listaFiltrada) {
                it.nome.lowercase().contains(texto) || it.usuario.lowercase().contains(texto)
            }
        }
        adapter.notifyDataSetChanged()

        if (listaFiltrada.isEmpty() && texto.isNotEmpty()) {
            Toast.makeText(this, "Nenhum usuário encontrado para \"$texto\".", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Envia uma solicitação de amizade gravando um documento em 'solicitacoes_amizade'.
     * Campos: uidRemetente, uidDestinatario, nomeRemetente, status ("pendente"), criadoEm.
     */
    private fun enviarSolicitacaoAmizade(destinatario: UsuarioAmigo) {
        // Busca o nome do remetente para incluir no documento da solicitação
        db.collection("usuarios").document(uidAtual).get()
            .addOnSuccessListener { docRemetente ->
                val nomeRemetente = docRemetente.getString("nome") ?: "Usuário"

                val dados = hashMapOf(
                    "uidRemetente"      to uidAtual,
                    "uidDestinatario"   to destinatario.uid,
                    "nomeRemetente"     to nomeRemetente,
                    "status"            to "pendente",
                    "criadoEm"          to System.currentTimeMillis()
                )

                db.collection("solicitacoes_amizade")
                    .add(dados)
                    .addOnSuccessListener {
                        Toast.makeText(
                            this,
                            "Solicitação de amizade enviada para ${destinatario.nome}!",
                            Toast.LENGTH_SHORT
                        ).show()
                        // Remove da lista local para evitar reenvio
                        listaCompleta.remove(destinatario)
                        filtrarLista(
                            findViewById<EditText>(R.id.editBuscarAmigo).text.toString().trim().lowercase()
                        )
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Erro ao enviar solicitação: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao obter seu perfil. Tente novamente.", Toast.LENGTH_SHORT).show()
            }
    }
}
