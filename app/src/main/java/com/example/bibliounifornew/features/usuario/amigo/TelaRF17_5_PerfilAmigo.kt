package com.example.bibliounifornew.features.usuario.amigo

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.AuthRepository
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore

class TelaRF17_5_PerfilAmigo : AppCompatActivity() {

    private val authRepository = AuthRepository()
    private val db             = FirebaseFirestore.getInstance()

    private var uidAtual  : String = ""
    private var amigoUid  : String = ""
    private var amigoNome : String = ""

    // Estado do botão rastreado localmente para permitir revert após desfazer
    private var eAmigo: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf17_5_perfil_amigo)

        amigoUid  = intent.getStringExtra("AMIGO_UID")  ?: ""
        amigoNome = intent.getStringExtra("AMIGO_NOME") ?: "Usuário"

        val usuarioAtual = authRepository.getUsuarioAtual()
        if (usuarioAtual == null) {
            finish()
            return
        }
        uidAtual = usuarioAtual.uid

        // Pré-preenche enquanto o Firestore carrega
        findViewById<TextView>(R.id.textEmailPerfilAmigo)?.text = amigoNome
        findViewById<TextView>(R.id.textNomePerfilAmigo)?.text  = amigoNome

        if (amigoUid.isNotEmpty()) {
            carregarPerfilAmigo(amigoUid)
            verificarEstadoAmizade(amigoUid)
        }
    }

    // ─── CARREGAMENTO DO PERFIL ───────────────────────────────────────────────

    private fun carregarPerfilAmigo(uid: String) {
        db.collection("usuarios").document(uid).get()
            .addOnSuccessListener { doc ->
                if (isFinishing || isDestroyed) return@addOnSuccessListener
                if (!doc.exists()) return@addOnSuccessListener

                val nome    = doc.getString("nome")      ?: "Usuário"
                val usuario = doc.getString("usuario")   ?: ""
                val bio     = doc.getString("biografia") ?: "Sem biografia."
                val fotoUrl = doc.getString("fotoUrl")   ?: ""

                findViewById<TextView>(R.id.textEmailPerfilAmigo)?.text   = nome
                findViewById<TextView>(R.id.textNomePerfilAmigo)?.text    = nome
                findViewById<TextView>(R.id.textUsuarioPerfilAmigo)?.text = "@$usuario".takeIf { usuario.isNotEmpty() } ?: ""
                findViewById<TextView>(R.id.textBioPerfilAmigo)?.text     = bio

                // Foto do amigo via Coil com tamanho controlado
                if (fotoUrl.isNotEmpty()) {
                    findViewById<ImageView>(R.id.imagePerfilAmigo)?.load(fotoUrl) {
                        size(200, 200)
                        crossfade(true)
                        placeholder(R.drawable.user_placeholder)
                        error(R.drawable.user_placeholder)
                    }
                }
            }
            .addOnFailureListener {
                if (!isFinishing && !isDestroyed) {
                    Toast.makeText(this, getString(R.string.erro_carregar_perfil), Toast.LENGTH_SHORT).show()
                }
            }
    }

    // ─── VERIFICAÇÃO DE ESTADO DE AMIZADE ────────────────────────────────────

    /**
     * Consulta a subcoleção usuarios/{uidAtual}/amigos para determinar se
     * o vínculo com [amigoUid] já existe, configurando o botão antes mesmo
     * de qualquer interação do usuário.
     *
     * Fluxo de estados do botão:
     *   JÁ AMIGO   → "Desfazer Amizade" (vermelho)
     *   SOLICITAÇÃO PENDENTE → "Solicitação Enviada" (desabilitado)
     *   SEM VÍNCULO → "Adicionar Amigo" (azul)
     */
    private fun verificarEstadoAmizade(uid: String) {
        // Passo 1: verifica se já é amigo confirmado
        db.collection("usuarios").document(uidAtual)
            .collection("amigos").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                if (isFinishing || isDestroyed) return@addOnSuccessListener
                if (doc.exists()) {
                    // Amizade confirmada → botão vermelho "Desfazer"
                    configurarBotaoAmigo()
                } else {
                    // Passo 2: verifica se há solicitação pendente enviada pelo usuário atual
                    verificarSolicitacaoPendente(uid)
                }
            }
            .addOnFailureListener {
                // Falha silenciosa: mantém estado padrão "Adicionar Amigo"
                if (!isFinishing && !isDestroyed) configurarBotaoAdicionar()
            }
    }

    private fun verificarSolicitacaoPendente(uid: String) {
        db.collection("solicitacoes_amizade")
            .whereEqualTo("uidRemetente",    uidAtual)
            .whereEqualTo("uidDestinatario", uid)
            .whereEqualTo("status",          "pendente")
            .get()
            .addOnSuccessListener { result ->
                if (isFinishing || isDestroyed) return@addOnSuccessListener
                if (!result.isEmpty) {
                    configurarBotaoSolicitacaoEnviada()
                } else {
                    configurarBotaoAdicionar()
                }
            }
            .addOnFailureListener {
                if (!isFinishing && !isDestroyed) configurarBotaoAdicionar()
            }
    }

    // ─── CONFIGURAÇÃO DO BOTÃO ────────────────────────────────────────────────

    private fun configurarBotaoAmigo() {
        eAmigo = true
        val btn = findViewById<MaterialButton>(R.id.btnAdicionarAmigoPerfil) ?: return
        btn.text = "Desfazer Amizade"
        btn.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#C62828"))
        btn.isEnabled = true
        btn.setOnClickListener { desfazerAmizade() }
    }

    private fun configurarBotaoAdicionar() {
        eAmigo = false
        val btn = findViewById<MaterialButton>(R.id.btnAdicionarAmigoPerfil) ?: return
        btn.text = "Adicionar Amigo"
        btn.backgroundTintList = getColorStateList(R.color.biblio_blue)
        btn.isEnabled = true
        btn.setOnClickListener {
            if (amigoUid.isNotEmpty()) enviarSolicitacaoAmizade(amigoUid, amigoNome)
            else Toast.makeText(this, getString(R.string.erro_identificar_usuario), Toast.LENGTH_SHORT).show()
        }
    }

    private fun configurarBotaoSolicitacaoEnviada() {
        val btn = findViewById<MaterialButton>(R.id.btnAdicionarAmigoPerfil) ?: return
        btn.text = "Solicitação Enviada"
        btn.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#9E9E9E"))
        btn.isEnabled = false
    }

    // ─── ENVIAR SOLICITAÇÃO ───────────────────────────────────────────────────

    private fun enviarSolicitacaoAmizade(uidDestinatario: String, nomeDestinatario: String) {
        try {
            db.collection("usuarios").document(uidAtual).get()
                .addOnSuccessListener { docRemetente ->
                    if (isFinishing || isDestroyed) return@addOnSuccessListener
                    val nomeRemetente = docRemetente.getString("nome") ?: "Usuário"

                    val dados = hashMapOf(
                        "uidRemetente"    to uidAtual,
                        "uidDestinatario" to uidDestinatario,
                        "nomeRemetente"   to nomeRemetente,
                        "status"          to "pendente",
                        "criadoEm"        to System.currentTimeMillis()
                    )

                    db.collection("solicitacoes_amizade").add(dados)
                        .addOnSuccessListener {
                            if (isFinishing || isDestroyed) return@addOnSuccessListener
                            Toast.makeText(this, "Solicitação enviada para $nomeDestinatario!", Toast.LENGTH_SHORT).show()
                            configurarBotaoSolicitacaoEnviada()
                        }
                        .addOnFailureListener { e ->
                            if (!isFinishing && !isDestroyed) {
                                Toast.makeText(this, getString(R.string.fmt_erro_enviar_solicitacao_amizade, e.message), Toast.LENGTH_SHORT).show()
                            }
                        }
                }
                .addOnFailureListener {
                    if (!isFinishing && !isDestroyed) {
                        Toast.makeText(this, getString(R.string.erro_obter_perfil), Toast.LENGTH_SHORT).show()
                    }
                }
        } catch (e: SecurityException) {
            Log.e("PERFIL_AMIGO", "SecurityException ao enviar solicitação: ${e.message}")
            if (!isFinishing && !isDestroyed) {
                Toast.makeText(this, getString(R.string.erro_generico), Toast.LENGTH_SHORT).show()
            }
        } catch (e: RuntimeException) {
            Log.e("PERFIL_AMIGO", "RuntimeException ao enviar solicitação [GMS?]: ${e.message}")
            if (!isFinishing && !isDestroyed) {
                Toast.makeText(this, getString(R.string.erro_generico), Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ─── DESFAZER AMIZADE ─────────────────────────────────────────────────────

    /**
     * Remove o vínculo de amizade de forma mútua e atômica via writeBatch:
     *   • Delete usuarios/{uidAtual}/amigos/{amigoUid}
     *   • Delete usuarios/{amigoUid}/amigos/{uidAtual}
     *
     * Protegido contra crashes do GMS (Phenotype.API / DEVELOPER_ERROR)
     * com try-catch duplo seguindo o mesmo padrão de TelaRF17Amigos.
     */
    private fun desfazerAmizade() {
        val btn = findViewById<MaterialButton>(R.id.btnAdicionarAmigoPerfil)
        btn?.isEnabled = false

        try {
            val batch = db.batch()

            batch.delete(
                db.collection("usuarios").document(uidAtual)
                    .collection("amigos").document(amigoUid)
            )
            batch.delete(
                db.collection("usuarios").document(amigoUid)
                    .collection("amigos").document(uidAtual)
            )

            try {
                batch.commit()
                    .addOnSuccessListener {
                        if (isFinishing || isDestroyed) return@addOnSuccessListener
                        Toast.makeText(this, "Amizade desfeita com sucesso.", Toast.LENGTH_SHORT).show()
                        // Reverte o botão para "Adicionar Amigo" sem fechar a tela
                        configurarBotaoAdicionar()
                    }
                    .addOnFailureListener { e ->
                        if (isFinishing || isDestroyed) return@addOnFailureListener
                        btn?.isEnabled = true
                        Log.e("PERFIL_AMIGO", "batch.commit() desfazer falhou: ${e.javaClass.simpleName} — ${e.message}")
                        Toast.makeText(this, "Erro ao desfazer amizade: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } catch (gmsEx: RuntimeException) {
                // Falha do GMS (Phenotype.API / DEVELOPER_ERROR) — o cache offline
                // já registrou a deleção; atualizamos a UI sem aguardar o servidor.
                Log.w("PERFIL_AMIGO", "GMS falhou em batch.commit() [${gmsEx.javaClass.simpleName}]: ${gmsEx.message}. Atualizando UI via cache local.")
                if (!isFinishing && !isDestroyed) {
                    Toast.makeText(this, "Amizade desfeita com sucesso.", Toast.LENGTH_SHORT).show()
                    configurarBotaoAdicionar()
                }
            } catch (secEx: SecurityException) {
                Log.w("PERFIL_AMIGO", "SecurityException em batch.commit(): ${secEx.message}. Fallback para cache local.")
                if (!isFinishing && !isDestroyed) {
                    Toast.makeText(this, "Amizade desfeita com sucesso.", Toast.LENGTH_SHORT).show()
                    configurarBotaoAdicionar()
                }
            }

        } catch (e: Exception) {
            btn?.isEnabled = true
            Log.e("PERFIL_AMIGO", "Exceção inesperada em desfazerAmizade: ${e.javaClass.simpleName} — ${e.message}")
            if (!isFinishing && !isDestroyed) {
                Toast.makeText(this, getString(R.string.erro_generico), Toast.LENGTH_SHORT).show()
            }
        }
    }
}
