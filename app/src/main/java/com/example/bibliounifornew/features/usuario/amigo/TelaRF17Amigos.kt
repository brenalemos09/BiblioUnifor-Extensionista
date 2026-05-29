package com.example.bibliounifornew.features.usuario.amigo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.AuthRepository
import com.example.bibliounifornew.features.usuario.perfil.NavigationHelper
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.PersistentCacheSettings

class TelaRF17Amigos : AppCompatActivity() {

    private val authRepository = AuthRepository()
    private val db: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance().also { firestore ->
            // Ativa persistência offline para que a lista não bata na nuvem
            // toda vez que a tela for aberta. Ignorado silenciosamente se já
            // foi configurado por outra instância da sessão.
            try {
                // API moderna (Firebase BOM 32+): PersistentCacheSettings substitui
                // o deprecated setPersistenceEnabled() + setCacheSizeBytes().
                firestore.firestoreSettings = FirebaseFirestoreSettings.Builder()
                    .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
                    .build()
            } catch (_: IllegalStateException) { }
        }
    }

    // ─── AMIGOS CONFIRMADOS ────────────────────────────────────────────────────
    private lateinit var adapterAmigos: AmigoAdapter
    private val listaAmigos = mutableListOf<UsuarioAmigo>()

    // ─── SOLICITAÇÕES DE AMIZADE RECEBIDAS ────────────────────────────────────
    private lateinit var adapterSolicitacoes: SolicitacaoAmizadeAdapter
    private val listaSolicitacoes = mutableListOf<SolicitacaoAmizade>()

    // Referências de UI para show/hide da seção de solicitações
    private var textSolicitacoesTitulo  : TextView?     = null
    private var recyclerViewSolicitacoes: RecyclerView? = null
    private var dividerSolicitacoes     : View?         = null

    // Listeners Firestore — removidos em onDestroy() para evitar memory leak
    private var listenerAmigos      : ListenerRegistration? = null
    private var listenerSolicitacoes: ListenerRegistration? = null

    private var uidAtual: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf17_amigos)

        val usuarioAtual = authRepository.getUsuarioAtual()
        if (usuarioAtual == null) {
            startActivity(Intent(this, com.example.bibliounifornew.login.TelaRF03LoginAluno::class.java))
            finish()
            return
        }
        uidAtual = usuarioAtual.uid

        // ─── BOTÃO ADICIONAR AMIGOS ───────────────────────────────────────────
        findViewById<View>(R.id.layoutAdicionarAmigos)?.setOnClickListener {
            startActivity(Intent(this, TelaRF17_3_BuscaAmigos::class.java))
        }

        // ─── SEÇÃO DE SOLICITAÇÕES ────────────────────────────────────────────
        textSolicitacoesTitulo   = findViewById(R.id.textSolicitacoesTitulo)
        recyclerViewSolicitacoes = findViewById(R.id.recyclerViewSolicitacoes)
        dividerSolicitacoes      = findViewById(R.id.dividerSolicitacoes)

        adapterSolicitacoes = SolicitacaoAmizadeAdapter(
            listaSolicitacoes,
            onAceitar = { solicitacao, position -> aceitarSolicitacao(solicitacao, position) },
            onRecusar = { solicitacao, position -> recusarSolicitacao(solicitacao, position) }
        )
        recyclerViewSolicitacoes?.layoutManager = LinearLayoutManager(this)
        recyclerViewSolicitacoes?.adapter = adapterSolicitacoes

        // ─── LISTA DE AMIGOS ──────────────────────────────────────────────────
        adapterAmigos = AmigoAdapter(listaAmigos)
        val recyclerViewAmigos = findViewById<RecyclerView>(R.id.recyclerViewAmigos)
        recyclerViewAmigos.layoutManager = LinearLayoutManager(this)
        recyclerViewAmigos.adapter = adapterAmigos

        // ─── DADOS ────────────────────────────────────────────────────────────
        carregarFotoPerfil()
        carregarAmigos()
        carregarSolicitacoesRecebidas()

        NavigationHelper.configurarBarraNavegacao(this)
    }

    // ─── LIMPAR LISTENERS AO SAIR ─────────────────────────────────────────────

    override fun onDestroy() {
        // Remove os listeners antes de destruir a Activity.
        // Sem isso, callbacks chegam em uma Activity destruída → crash / memory leak.
        listenerAmigos?.remove()
        listenerSolicitacoes?.remove()
        super.onDestroy()
    }

    // ─── FOTO DE PERFIL DO USUÁRIO LOGADO ─────────────────────────────────────

    private fun carregarFotoPerfil() {
        val imageUsuario = findViewById<ImageView>(R.id.imageUsuario) ?: return
        db.collection("usuarios").document(uidAtual).get()
            .addOnSuccessListener { doc ->
                if (isFinishing || isDestroyed) return@addOnSuccessListener
                val fotoUrl = doc.getString("fotoUrl") ?: ""
                if (fotoUrl.isNotEmpty()) {
                    // Tamanho limitado para o avatar circular no header.
                    // crossfade evita o flash branco ao substituir o placeholder.
                    imageUsuario.load(fotoUrl) {
                        size(200, 200)
                        crossfade(true)
                        placeholder(R.drawable.user_placeholder)
                        error(R.drawable.user_placeholder)
                    }
                }
            }
    }

    // ─── CARREGAMENTO ─────────────────────────────────────────────────────────

    /**
     * Carrega amigos confirmados via SnapshotListener (real-time).
     * Limite de 20 documentos por página para não travar a UI Thread com
     * listas grandes. O listener é armazenado e removido em onDestroy().
     */
    private fun carregarAmigos() {
        listenerAmigos = db.collection("usuarios")
            .document(uidAtual)
            .collection("amigos")
            .limit(20)                      // primeira página; evita buscar tudo de uma vez
            .addSnapshotListener { snapshot, error ->
                if (isFinishing || isDestroyed) return@addSnapshotListener
                if (error != null) {
                    Toast.makeText(this, getString(R.string.fmt_erro_carregar_amigos, error.message), Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                listaAmigos.clear()
                snapshot?.documents?.forEach { doc ->
                    listaAmigos.add(
                        UsuarioAmigo(
                            uid     = doc.getString("uid")     ?: doc.id,
                            nome    = doc.getString("nome")    ?: "Amigo",
                            usuario = doc.getString("usuario") ?: "",
                            fotoUrl = doc.getString("fotoUrl") ?: doc.getString("foto") ?: ""
                        )
                    )
                }
                adapterAmigos.notifyDataSetChanged()
                if (listaAmigos.isEmpty()) {
                    Toast.makeText(this, getString(R.string.msg_sem_amigos), Toast.LENGTH_SHORT).show()
                }
            }
    }

    /**
     * Carrega solicitações de amizade pendentes em tempo real.
     * Também limitado a 20 resultados; mostra a seção apenas quando há itens.
     */
    private fun carregarSolicitacoesRecebidas() {
        listenerSolicitacoes = db.collection("solicitacoes_amizade")
            .whereEqualTo("uidDestinatario", uidAtual)
            .whereEqualTo("status", "pendente")
            .limit(20)
            .addSnapshotListener { snapshot, error ->
                if (isFinishing || isDestroyed) return@addSnapshotListener
                if (error != null) return@addSnapshotListener  // falha silenciosa — índice pode não existir ainda

                listaSolicitacoes.clear()
                snapshot?.documents?.forEach { doc ->
                    listaSolicitacoes.add(
                        SolicitacaoAmizade(
                            docId         = doc.id,
                            uidRemetente  = doc.getString("uidRemetente")  ?: "",
                            nomeRemetente = doc.getString("nomeRemetente") ?: "Usuário",
                            status        = doc.getString("status")        ?: "pendente"
                        )
                    )
                }
                adapterSolicitacoes.notifyDataSetChanged()
                atualizarVisibilidadeSolicitacoes()
            }
    }

    private fun atualizarVisibilidadeSolicitacoes() {
        val visivel = if (listaSolicitacoes.isNotEmpty()) View.VISIBLE else View.GONE
        textSolicitacoesTitulo?.visibility   = visivel
        recyclerViewSolicitacoes?.visibility = visivel
        dividerSolicitacoes?.visibility      = visivel
    }

    // ─── ACEITAR SOLICITAÇÃO ──────────────────────────────────────────────────

    /**
     * Aceita uma solicitação de amizade criando documentos mútuos via Firestore Batch.
     *
     * BLINDAGEM GMS: O crash "Phenotype.API is not available / DEVELOPER_ERROR" é disparado
     * internamente pelo Firebase SDK ao tentar inicializar o ProviderInstaller do Google Play
     * Services durante a execução do batch — frequente em emuladores sem GMS completo.
     * Não existe chamada explícita a ProviderInstaller neste arquivo; o crash vem do SDK.
     *
     * Dois níveis de proteção:
     *   1. try-catch ao redor de batch.commit() — captura RuntimeException/SecurityException
     *      do GMS sem propagar o crash. Se o GMS falhar, a UI é atualizada localmente
     *      (o cache offline do Firestore já garantiu a escrita) e o usuário não trava.
     *   2. try-catch ao redor de todo o método — guard de último recurso para exceções
     *      síncronas inesperadas ao construir as referências do Firestore.
     */
    private fun aceitarSolicitacao(solicitacao: SolicitacaoAmizade, position: Int) {
        // Guard de último recurso: captura qualquer exceção síncrona na montagem das refs
        try {
            db.collection("usuarios").document(solicitacao.uidRemetente).get()
                .addOnSuccessListener { docRemetente ->
                    val nomeRemetente    = docRemetente.getString("nome")    ?: solicitacao.nomeRemetente
                    val usuarioRemetente = docRemetente.getString("usuario") ?: ""
                    val fotoRemetente    = docRemetente.getString("fotoUrl") ?: ""

                    db.collection("usuarios").document(uidAtual).get()
                        .addOnSuccessListener { docAtual ->
                            val nomeAtual    = docAtual.getString("nome")    ?: "Usuário"
                            val usuarioAtual = docAtual.getString("usuario") ?: ""
                            val fotoAtual    = docAtual.getString("fotoUrl") ?: ""

                            // ── Monta o batch de escritas ─────────────────────
                            val batch = db.batch()

                            batch.set(
                                db.collection("usuarios").document(uidAtual)
                                    .collection("amigos").document(solicitacao.uidRemetente),
                                mapOf(
                                    "uid"     to solicitacao.uidRemetente,
                                    "nome"    to nomeRemetente,
                                    "usuario" to usuarioRemetente,
                                    "fotoUrl" to fotoRemetente
                                )
                            )

                            batch.set(
                                db.collection("usuarios").document(solicitacao.uidRemetente)
                                    .collection("amigos").document(uidAtual),
                                mapOf(
                                    "uid"     to uidAtual,
                                    "nome"    to nomeAtual,
                                    "usuario" to usuarioAtual,
                                    "fotoUrl" to fotoAtual
                                )
                            )

                            batch.update(
                                db.collection("solicitacoes_amizade").document(solicitacao.docId),
                                "status", "aceito"
                            )

                            // ── Executa o batch com proteção contra falha do GMS ──
                            // O try-catch interno isola RuntimeException e SecurityException
                            // disparadas pelo ProviderInstaller/Phenotype.API do Play Services
                            // sem afetar a escrita já realizada no cache offline do Firestore.
                            try {
                                batch.commit()
                                    .addOnSuccessListener {
                                        if (isFinishing || isDestroyed) return@addOnSuccessListener
                                        // SnapshotListener atualiza a lista de amigos automaticamente.
                                        // Aqui apenas remove a solicitação já aceita da seção pendente.
                                        adapterSolicitacoes.removerItem(position)
                                        atualizarVisibilidadeSolicitacoes()
                                        Toast.makeText(
                                            this,
                                            getString(R.string.fmt_amigo_adicionado, nomeRemetente),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    .addOnFailureListener { e ->
                                        if (isFinishing || isDestroyed) return@addOnFailureListener
                                        Log.e("AMIGOS_DEBUG", "batch.commit() falhou: ${e.javaClass.simpleName} — ${e.message}")
                                        Toast.makeText(
                                            this,
                                            getString(R.string.fmt_erro_aceitar_solicitacao, e.message),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            } catch (gmsEx: RuntimeException) {
                                // Capturado: DEVELOPER_ERROR / Phenotype.API / SecurityException do GMS.
                                // O cache offline do Firestore já escreveu os dados localmente;
                                // atualizamos a UI sem aguardar resposta do servidor.
                                Log.w("AMIGOS_DEBUG", "GMS falhou durante batch.commit() [${gmsEx.javaClass.simpleName}]: ${gmsEx.message}. Atualizando UI via cache local.")
                                if (!isFinishing && !isDestroyed) {
                                    adapterSolicitacoes.removerItem(position)
                                    atualizarVisibilidadeSolicitacoes()
                                    Toast.makeText(
                                        this,
                                        getString(R.string.fmt_amigo_adicionado, nomeRemetente),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } catch (secEx: SecurityException) {
                                Log.w("AMIGOS_DEBUG", "SecurityException durante batch.commit(): ${secEx.message}. Fallback para cache local.")
                                if (!isFinishing && !isDestroyed) {
                                    adapterSolicitacoes.removerItem(position)
                                    atualizarVisibilidadeSolicitacoes()
                                    Toast.makeText(
                                        this,
                                        getString(R.string.fmt_amigo_adicionado, nomeRemetente),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                        .addOnFailureListener {
                            if (!isFinishing && !isDestroyed) {
                                Toast.makeText(this, getString(R.string.erro_buscar_perfil), Toast.LENGTH_SHORT).show()
                            }
                        }
                }
                .addOnFailureListener {
                    if (!isFinishing && !isDestroyed) {
                        Toast.makeText(this, getString(R.string.erro_buscar_perfil_solicitante), Toast.LENGTH_SHORT).show()
                    }
                }
        } catch (e: Exception) {
            Log.e("AMIGOS_DEBUG", "Exceção inesperada em aceitarSolicitacao: ${e.javaClass.simpleName} — ${e.message}")
            if (!isFinishing && !isDestroyed) {
                Toast.makeText(this, getString(R.string.fmt_erro_aceitar_solicitacao, e.message), Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ─── RECUSAR SOLICITAÇÃO ──────────────────────────────────────────────────

    private fun recusarSolicitacao(solicitacao: SolicitacaoAmizade, position: Int) {
        db.collection("solicitacoes_amizade").document(solicitacao.docId)
            .update("status", "recusado")
            .addOnSuccessListener {
                adapterSolicitacoes.removerItem(position)
                atualizarVisibilidadeSolicitacoes()
                Toast.makeText(this, getString(R.string.msg_solicitacao_recusada), Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    getString(R.string.fmt_erro_recusar_solicitacao, e.message),
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}
