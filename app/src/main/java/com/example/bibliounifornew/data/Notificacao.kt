package com.example.bibliounifornew.data

/**
 * Modelo de dados para uma notificação na tela RF20.
 * Futuramente pode ser populado via Firestore (collection "notificacoes_usuarios").
 */
data class Notificacao(
    val id: String,
    val titulo: String,
    val autor: String,
    val descricao: String,
    val tempo: String,
    var lida: Boolean = false,
    val capaResId: Int = 0
)
