package com.example.bibliounifornew.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions

class UsuarioRepository {

    private val db = FirebaseFirestore.getInstance()

    fun salvarUsuarioFirestore(
        uid: String,
        nome: String,
        usuario: String,
        email: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        // Mapeando os dados para o padrão NoSQL do Firestore
        val userMap = hashMapOf(
            "uid" to uid,
            "nome" to nome,
            "usuario" to usuario,
            "email" to email,
            "tipoPerfil" to "estudante", // Define como estudante por padrão
            "statusCadastro" to "pendente"
        )

        // Salva na coleção "usuarios" usando o UID como nome do documento
        db.collection("usuarios").document(uid)
            .set(userMap)
            .addOnSuccessListener {
                onComplete(true, null)
            }
            .addOnFailureListener { e ->
                onComplete(false, e.message)
            }
    }

    // Atualiza um campo específico do perfil no banco de dados
    fun atualizarCampoPerfil(uid: String, campo: String, valor: String, onComplete: (Boolean, String?) -> Unit) {
        db.collection("usuarios").document(uid)
            .update(campo, valor)
            .addOnSuccessListener { onComplete(true, null) }
            .addOnFailureListener { e -> onComplete(false, e.message) }
    }

    fun buscarPerfilUsuario(uid: String, onComplete: (Boolean, Map<String, Any>?, String?) -> Unit) {
        db.collection("usuarios").document(uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    onComplete(true, document.data, null)
                } else {
                    onComplete(false, null, "Usuário não encontrado no banco de dados.")
                }
            }
            .addOnFailureListener { exception ->
                onComplete(false, null, exception.message)
            }
    }

    /**
     * Observa o perfil do usuário em tempo real com SnapshotListener.
     * Retorna um ListenerRegistration que DEVE ser cancelado em onDestroy() da Activity/Fragment.
     * Uso: snapshotListener = usuarioRepository.observarPerfilUsuario(uid) { dados -> ... }
     *      snapshotListener?.remove() // no onDestroy
     */
    fun observarPerfilUsuario(uid: String, onChange: (Map<String, Any>?) -> Unit): ListenerRegistration {
        return db.collection("usuarios").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) {
                    onChange(null)
                    return@addSnapshotListener
                }
                onChange(snapshot.data)
            }
    }

    /**
     * Salva múltiplos campos do perfil de uma só vez com merge,
     * preservando campos que não foram alterados.
     */
    fun salvarPerfilCompleto(
        uid: String,
        campos: Map<String, Any>,
        onComplete: (Boolean, String?) -> Unit
    ) {
        db.collection("usuarios").document(uid)
            .set(campos, SetOptions.merge())
            .addOnSuccessListener { onComplete(true, null) }
            .addOnFailureListener { e -> onComplete(false, e.message) }
    }

    /**
     * Remove um item do histórico do usuário no Firestore.
     * A collection segue o padrão: historico_usuarios/{uid}_{livroId}
     */
    fun removerDoHistorico(uid: String, livroId: String, onComplete: (Boolean) -> Unit) {
        val documentoId = "${uid}_${livroId}"
        db.collection("historico_usuarios").document(documentoId)
            .delete()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    /**
     * Salva ou atualiza o item na lista de desejos no Firestore.
     * Collection: lista_desejos/{uid}_{livroId}
     */
    fun salvarListaDesejos(
        uid: String,
        livroId: String,
        dados: Map<String, Any>,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val documentoId = "${uid}_${livroId}"
        db.collection("lista_desejos").document(documentoId)
            .set(dados, SetOptions.merge())
            .addOnSuccessListener { onComplete(true, null) }
            .addOnFailureListener { e -> onComplete(false, e.message) }
    }

    /**
     * Remove item da lista de desejos no Firestore.
     */
    fun removerDaListaDesejos(uid: String, livroId: String, onComplete: (Boolean) -> Unit) {
        val documentoId = "${uid}_${livroId}"
        db.collection("lista_desejos").document(documentoId)
            .delete()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun verificarECriarUsuarioGoogle(
        uid: String,
        nome: String?,
        email: String?,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val docRef = db.collection("usuarios").document(uid)

        docRef.get().addOnSuccessListener { document ->
            if (!document.exists()) {
                // É o primeiro acesso! Vamos criar o perfil dele no Firestore.
                val userMap = hashMapOf(
                    "uid" to uid,
                    "nome" to (nome ?: "Usuário Google"),
                    "email" to email,
                    "tipoPerfil" to "estudante",
                    "statusCadastro" to "aprovado" // Contas Google já são validadas nativamente
                )

                docRef.set(userMap)
                    .addOnSuccessListener { onComplete(true, null) }
                    .addOnFailureListener { e -> onComplete(false, e.message) }
            } else {
                // O usuário já existe no banco, apenas liberamos o login.
                onComplete(true, null)
            }
        }.addOnFailureListener { e ->
            onComplete(false, e.message)
        }
    }
}