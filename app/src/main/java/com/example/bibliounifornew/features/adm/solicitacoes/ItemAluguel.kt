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
    /** dataDevolucao: calculada no momento da aprovação (dataEmprestimo + 14 dias). */
    val dataDevolucao  : Long   = 0L,
    val status         : String = "pendente",
    val nomeUsuario    : String = "Usuário",
    val tituloLivro    : String = "Título Indisponível",
    val autorLivro     : String = "Autor Desconhecido",
    val coverUrl       : String = ""
)
