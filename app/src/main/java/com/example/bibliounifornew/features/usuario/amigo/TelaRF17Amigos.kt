package com.example.bibliounifornew.features.usuario.amigo

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.R
import com.example.bibliounifornew.features.usuario.perfil.NavigationHelper

class TelaRF17Amigos : AppCompatActivity() {

    // ─── AMIGOS CONFIRMADOS ────────────────────────────────────────────────────
    private lateinit var adapterAmigos  : AmigoAdapter
    private val listaAmigos             = mutableListOf<UsuarioAmigo>()

    // ─── SOLICITAÇÕES DE AMIZADE RECEBIDAS ────────────────────────────────────
    private lateinit var adapterSolicitacoes  : SolicitacaoAmizadeAdapter
    private val listaSolicitacoes             = mutableListOf<SolicitacaoAmizade>()

    // Referências de UI para show/hide da seção de solicitações
    private var textSolicitacoesTitulo  : TextView?     = null
    private var recyclerViewSolicitacoes: RecyclerView? = null
    private var dividerSolicitacoes     : View?         = null

    private var uidAtual: String = "mock_user_123"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf17_amigos)

        // ─── BOTÃO ADICIONAR AMIGOS ───────────────────────────────────────────
        findViewById<View>(R.id.layoutAdicionarAmigos)?.setOnClickListener {
            startActivity(Intent(this, TelaRF17_3_BuscaAmigos::class.java))
        }

        // ─── REFERÊNCIAS DA SEÇÃO DE SOLICITAÇÕES ─────────────────────────────
        textSolicitacoesTitulo   = findViewById(R.id.textSolicitacoesTitulo)
        recyclerViewSolicitacoes = findViewById(R.id.recyclerViewSolicitacoes)
        dividerSolicitacoes      = findViewById(R.id.dividerSolicitacoes)

        // ─── ADAPTER DE SOLICITAÇÕES ──────────────────────────────────────────
        adapterSolicitacoes = SolicitacaoAmizadeAdapter(
            listaSolicitacoes,
            onAceitar = { solicitacao, position -> aceitarSolicitacao(solicitacao, position) },
            onRecusar = { solicitacao, position -> recusarSolicitacao(solicitacao, position) }
        )
        recyclerViewSolicitacoes?.layoutManager = LinearLayoutManager(this)
        recyclerViewSolicitacoes?.adapter = adapterSolicitacoes

        // ─── ADAPTER DE AMIGOS ────────────────────────────────────────────────
        adapterAmigos = AmigoAdapter(listaAmigos)
        val recyclerViewAmigos = findViewById<RecyclerView>(R.id.recyclerViewAmigos)
        recyclerViewAmigos?.layoutManager = LinearLayoutManager(this)
        recyclerViewAmigos?.adapter = adapterAmigos

        // ─── CARREGAMENTO DE DADOS ────────────────────────────────────────────
        carregarAmigosMock()
        carregarSolicitacoesRecebidasMock()

        // Configurar Barra de Navegação
        NavigationHelper.configurarBarraNavegacao(this)
    }

    private fun carregarAmigosMock() {
        listaAmigos.clear()
        listaAmigos.add(UsuarioAmigo("1", "Maria Silva", "maria_silva"))
        listaAmigos.add(UsuarioAmigo("2", "João Pereira", "joao_p"))
        listaAmigos.add(UsuarioAmigo("3", "Ana Souza", "ana_souza"))
        adapterAmigos.notifyDataSetChanged()
    }

    private fun carregarSolicitacoesRecebidasMock() {
        listaSolicitacoes.clear()
        listaSolicitacoes.add(SolicitacaoAmizade("s1", "4", "Carlos Eduardo", "pendente"))
        adapterSolicitacoes.notifyDataSetChanged()
        atualizarVisibilidadeSolicitacoes()
    }

    private fun atualizarVisibilidadeSolicitacoes() {
        val visivel = if (listaSolicitacoes.isNotEmpty()) View.VISIBLE else View.GONE
        textSolicitacoesTitulo?.visibility   = visivel
        recyclerViewSolicitacoes?.visibility = visivel
        dividerSolicitacoes?.visibility      = visivel
    }

    private fun aceitarSolicitacao(solicitacao: SolicitacaoAmizade, position: Int) {
        adapterSolicitacoes.removerItem(position)
        atualizarVisibilidadeSolicitacoes()

        listaAmigos.add(UsuarioAmigo(
            uid     = solicitacao.uidRemetente,
            nome    = solicitacao.nomeRemetente,
            usuario = solicitacao.nomeRemetente.lowercase().replace(" ", "_")
        ))
        adapterAmigos.notifyDataSetChanged()

        Toast.makeText(
            this,
            "${solicitacao.nomeRemetente} foi adicionado(a) como amigo(a)!",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun recusarSolicitacao(solicitacao: SolicitacaoAmizade, position: Int) {
        adapterSolicitacoes.removerItem(position)
        atualizarVisibilidadeSolicitacoes()
        Toast.makeText(this, "Solicitação recusada.", Toast.LENGTH_SHORT).show()
    }
}
