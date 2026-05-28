package com.example.bibliounifornew.data

/**
 * Modelo de uma solicitação de mídia.
 */
data class Solicitacao(
    val uidUsuario      : String = "",
    val uidAluno        : String = "",
    val idLivro         : String = "",
    val tipos           : String = "",
    val status          : String = "pendente",
    val dataSolicitacao : Long   = System.currentTimeMillis()
)
