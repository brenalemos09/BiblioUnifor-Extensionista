package com.example.bibliounifornew.features.usuario.amigo

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.AuthRepository
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore

class TelaRF17_5_PerfilAmigo : AppCompatActivity() {

    private val authRepository = AuthRepository()
    private val db             = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf17_5_perfil_amigo)

        val amigoUid  = intent.getStringExtra("AMIGO_UID")  ?: ""
        val amigoNome = intent.getStringExtra("AMIGO_NOME") ?: "Usuário"

        // ─── PRÉ-PREENCHE COM DADOS DO INTENT ─────────────────────────────────
        findViewById<TextView>(R.id.textEmailPerfilAmigo)?.text    = amigoNome
        findViewById<TextView>(R.id.textNomePerfilAmigo)?.text     = amigoNome

        // ─── CARREGA DADOS COMPLETOS DO FIRESTORE ─────────────────────────────
        if (amigoUid.isNotEmpty()) {
            carregarPerfilAmigo(amigoUid)
        }

        // ─── BOTÃO ADICIONAR AMIGO ─────────────────────────────────────────────
        val btnAdicionar = findViewById<MaterialButton>(R.id.btnAdicionarAmigoPerfil)
        btnAdicionar?.setOnClickListener {
            if (amigoUid.isNotEmpty()) {
                enviarSolicitacaoAmizade(amigoUid, amigoNome)
            } else {
                Toast.makeText(this, "Não foi possível identificar o usuário.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun carregarPerfilAmigo(uid: String) {
        db.collection("usuarios").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val nome    = doc.getString("nome")    ?: "Usuário"
                    val usuario = doc.getString("usuario") ?: ""
                    val bio     = doc.getString("biografia") ?: "Sem biografia."

                    findViewById<TextView>(R.id.textEmailPerfilAmigo)?.text    = nome
                    findViewById<TextView>(R.id.textNomePerfilAmigo)?.text     = nome
                    findViewById<TextView>(R.id.textUsuarioPerfilAmigo)?.text  = usuario
                    findViewById<TextView>(R.id.textBioPerfilAmigo)?.text      = bio
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao carregar perfil.", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Envia solicitação de amizade para o usuário exibido no perfil.
     * Grava em 'solicitacoes_amizade' com status "pendente".
     */
    private fun enviarSolicitacaoAmizade(uidDestinatario: String, nomeDestinatario: String) {
        val usuarioAtual = authRepository.getUsuarioAtual() ?: run {
            Toast.makeText(this, "Faça login para adicionar amigos.", Toast.LENGTH_SHORT).show()
            return
        }
        val uidRemetente = usuarioAtual.uid

        // Busca o nome do remetente para incluir na solicitação
        db.collection("usuarios").document(uidRemetente).get()
            .addOnSuccessListener { docRemetente ->
                val nomeRemetente = docRemetente.getString("nome") ?: "Usuário"

                val dados = hashMapOf(
                    "uidRemetente"    to uidRemetente,
                    "uidDestinatario" to uidDestinatario,
                    "nomeRemetente"   to nomeRemetente,
                    "status"          to "pendente",
                    "criadoEm"        to System.currentTimeMillis()
                )

                db.collection("solicitacoes_amizade")
                    .add(dados)
                    .addOnSuccessListener {
                        Toast.makeText(
                            this,
                            "Solicitação enviada para $nomeDestinatario!",
                            Toast.LENGTH_SHORT
                        ).show()
                        // Desabilita o botão após envio para evitar duplicidade
                        findViewById<MaterialButton>(R.id.btnAdicionarAmigoPerfil)?.apply {
                            isEnabled = false
                            text = "Solicitação Enviada"
                        }
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
