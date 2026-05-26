package com.example.bibliounifornew.features.usuario.amigo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.R

/**
 * Adapter para a tela de Busca de Amigos (RF17_3).
 * Exibe usuários cadastrados no Firestore com botão "+" para enviar solicitação de amizade.
 * Toda lógica Firestore fica no callback [onEnviarSolicitacao] — adapter apenas faz binding.
 */
class BuscaAmigoAdapter(
    private val lista: MutableList<UsuarioAmigo>,
    private val onEnviarSolicitacao: (UsuarioAmigo) -> Unit
) : RecyclerView.Adapter<BuscaAmigoAdapter.BuscaViewHolder>() {

    inner class BuscaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtNome  : TextView  = itemView.findViewById(R.id.txtNomeAmigo)
        val btnMais  : ImageView = itemView.findViewById(R.id.btnMaisAmigo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BuscaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_amigo_busca, parent, false)
        return BuscaViewHolder(view)
    }

    override fun onBindViewHolder(holder: BuscaViewHolder, position: Int) {
        val usuario = lista[position]
        holder.txtNome.text = usuario.nome.ifEmpty { usuario.usuario }.ifEmpty { "Usuário" }
        holder.btnMais.setOnClickListener {
            onEnviarSolicitacao(usuario)
        }
        holder.itemView.setOnClickListener {
            onEnviarSolicitacao(usuario)
        }
    }

    override fun getItemCount(): Int = lista.size

    /** Substitui a lista completa (usado ao filtrar por texto). */
    fun atualizarLista(novaLista: List<UsuarioAmigo>) {
        lista.clear()
        lista.addAll(novaLista)
        notifyDataSetChanged()
    }
}
