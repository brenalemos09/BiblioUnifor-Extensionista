package com.example.bibliounifornew.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

/**
 * SolicitacaoRepository — camada de acesso a dados para solicitações de mídia.
 *
 * Responsabilidades:
 *   - Gravar novas solicitações no Firestore (coleção "solicitacoes_midia")
 *   - Escutar solicitações do usuário em tempo real (SnapshotListener)
 *   - Cancelar solicitações (soft-delete via status)
 *
 * Coleção: "solicitacoes_midia"
 * Compatível com: TelaRF31Solicitacoes (leitura ADM)
 */
class SolicitacaoRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    companion object {
        private const val COLECAO = "solicitacoes_midia"
    }

    // ─── ESCRITA ──────────────────────────────────────────────────────────────

    /**
     * Grava uma nova [Solicitacao] no Firestore.
     * O ID do documento é gerado automaticamente pelo Firestore (.add).
     *
     * @param solicitacao Objeto totalmente preenchido (uidUsuario, idLivro, tipos, etc.)
     * @param onResult    Callback: (sucesso: Boolean, docId?: String, erro?: String)
     */
    fun gravarSolicitacao(
        solicitacao: Solicitacao,
        onResult: (sucesso: Boolean, docId: String?, erro: String?) -> Unit
    ) {
        db.collection(COLECAO)
            .add(solicitacao.toFirestoreMap())
            .addOnSuccessListener { docRef ->
                onResult(true, docRef.id, null)
            }
            .addOnFailureListener { e ->
                onResult(false, null, e.message)
            }
    }

    // ─── LEITURA REATIVA ──────────────────────────────────────────────────────

    /**
     * Escuta em tempo real as solicitações pendentes de um usuário.
     * Retorna um [ListenerRegistration] que DEVE ser cancelado em onDestroy()
     * para evitar memory leak.
     *
     * @param uid       UID do usuário logado
     * @param onChange  Chamado imediatamente com a lista atual e a cada mudança
     */
    fun escutarSolicitacoesDoUsuario(
        uid: String,
        onChange: (List<Solicitacao>) -> Unit
    ): ListenerRegistration {
        return db.collection(COLECAO)
            .whereEqualTo("uidUsuario", uid)
            .orderBy("dataSolicitacao", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    onChange(emptyList())
                    return@addSnapshotListener
                }
                val lista = snapshot.documents.mapNotNull { doc ->
                    doc.data?.let { Solicitacao.fromFirestore(doc.id, it) }
                }
                onChange(lista)
            }
    }

    // ─── CANCELAMENTO ─────────────────────────────────────────────────────────

    /**
     * Cancela uma solicitação alterando seu status para "cancelado".
     * Usa merge para não sobrescrever campos existentes.
     */
    fun cancelarSolicitacao(
        docId: String,
        onResult: (Boolean) -> Unit
    ) {
        db.collection(COLECAO).document(docId)
            .update("status", "cancelado")
            .addOnSuccessListener { onResult(true)  }
            .addOnFailureListener { onResult(false) }
    }
}
