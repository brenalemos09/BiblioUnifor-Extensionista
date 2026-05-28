package com.example.bibliounifornew.data

/**
 * Modelo de dados para uma notificação do usuário.
 */
data class Notificacao(
    val id       : String  = "",
    val titulo   : String  = "",
    val autor    : String  = "",
    val descricao: String  = "",
    val coverUrl : String  = "",
    val livroId  : String  = "",
    val timestamp: Long    = 0L,
    var lida     : Boolean = false
)
