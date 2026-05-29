package com.example.bibliounifornew.features.adm.solicitacoes

/**
 * Modelo de dados para representar um aluguel (solicitação de empréstimo)
 * na visão do administrador.
 *
 * Campos originários do documento Firestore (coleção "alugueis" ou
 * "solicitacoes_emprestimo"): docId, uidAluno, idLivro, dataMs, status.
 *
 * Campos enriquecidos por join (populados antes de atualizar o adapter):
 * nomeUsuario, tituloLivro, autorLivro, coverUrl.
 */
data class ItemAluguel(
    val docId          : String = "",
    val uidAluno       : String = "",
    val idLivro        : String = "",
    val dataMs         : Long   = 0L,
    val dataDevolucao  : Long   = 0L,
    val status         : String = "pendente",
    val nomeUsuario    : String = "Usuário",
    /** fotoUsuario: URL do avatar do aluno, obtida via JOIN em usuarios/{uidAluno}. */
    val fotoLivro: String = "",      // Adicione isto
    val fotoUsuario: String = "",    // Adicione isto
    val tituloLivro    : String = "Título Indisponível",
    val autorLivro     : String = "Autor Desconhecido",
    /** coverUrl: URL da capa do livro, obtida via JOIN em livros/{idLivro}. */
    val coverUrl       : String = ""
)
