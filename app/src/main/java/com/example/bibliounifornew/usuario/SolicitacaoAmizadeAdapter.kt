package com.example.bibliounifornew.usuario

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.R
import com.google.android.material.button.MaterialButton

/** Modelo de solicitação de amizade recebida. */
data class SolicitacaoAmizade(
    val docId            : String = "",
    val uidRemetente     : String = "",
    val nomeRemetente    : String = "Usuário",
    val usuarioRemetente : String = "",
    val status           : String = "pendente"
)

/**
 * Adapter para a lista de solicitações de amizade recebidas.
 * Exibe nome do remetente + botões "Aceitar" / "Recusar".
 * Toda lógica Firestore fica nos callbacks — adapter apenas faz binding.
 */
class SolicitacaoAmizadeAdapter(
    private val lista    : MutableList<SolicitacaoAmizade>,
    private val onAceitar: (SolicitacaoAmizade, Int) -> Unit,
    private val onRecusar: (SolicitacaoAmizade, Int) -> Unit
) : RecyclerView.Adapter<SolicitacaoAmizadeAdapter.SolicitacaoViewHolder>() {

    inner class SolicitacaoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtNome    : TextView      = itemView.findViewById(R.id.txtNomeSolicitante)
        val btnAceitar : MaterialButton = itemView.findViewById(R.id.btnAceitarSolicitacao)
        val btnRecusar : MaterialButton = itemView.findViewById(R.id.btnRecusarSolicitacao)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SolicitacaoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_solicitacao_amizade, parent, false)
        return SolicitacaoViewHolder(view)
    }

    override fun onBindViewHolder(holder: SolicitacaoViewHolder, position: Int) {
        val solicitacao = lista[position]
        holder.txtNome.text = solicitacao.nomeRemetente

        holder.btnAceitar.setOnClickListener {
            onAceitar(solicitacao, holder.adapterPosition)
        }
        holder.btnRecusar.setOnClickListener {
            onRecusar(solicitacao, holder.adapterPosition)
        }
    }

    override fun getItemCount(): Int = lista.size

    fun removerItem(position: Int) {
        lista.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, lista.size)
    }
}
