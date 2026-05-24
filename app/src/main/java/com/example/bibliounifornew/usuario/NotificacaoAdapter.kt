package com.example.bibliounifornew.usuario

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.Notificacao

class NotificacaoAdapter(
    private val lista: MutableList<Notificacao>
) : RecyclerView.Adapter<NotificacaoAdapter.NotificacaoViewHolder>() {

    // -------------------------------------------------------
    // ViewHolder
    // -------------------------------------------------------
    inner class NotificacaoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgCapa      : ImageView = itemView.findViewById(R.id.imgCapaItemNotif)
        val textTitulo   : TextView  = itemView.findViewById(R.id.textTituloItemNotif)
        val textAutor    : TextView  = itemView.findViewById(R.id.textAutorItemNotif)
        val textDescricao: TextView  = itemView.findViewById(R.id.textDescricaoItemNotif)
        val textTempo    : TextView  = itemView.findViewById(R.id.textTempoItemNotif)
        val checkLida    : CheckBox  = itemView.findViewById(R.id.checkLidaItemNotif)
    }

    // -------------------------------------------------------
    // Adapter callbacks
    // -------------------------------------------------------
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificacaoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notificacao, parent, false)
        return NotificacaoViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificacaoViewHolder, position: Int) {
        val notif = lista[position]
        holder.imgCapa.setImageResource(notif.capaResId)
        holder.textTitulo.text    = notif.titulo
        holder.textAutor.text     = notif.autor
        holder.textDescricao.text = notif.descricao
        holder.textTempo.text     = notif.tempo
        holder.checkLida.isChecked = notif.lida

        // Marca como lida ao clicar no checkbox
        holder.checkLida.setOnCheckedChangeListener { _, isChecked ->
            lista[holder.adapterPosition].lida = isChecked
        }
    }

    override fun getItemCount(): Int = lista.size

    // -------------------------------------------------------
    // Ação de remoção — chamada pelo ItemTouchHelper no Swipe
    // -------------------------------------------------------
    fun removerItem(position: Int) {
        lista.removeAt(position)
        notifyItemRemoved(position)
        // Notifica que os itens abaixo mudaram de posição (garante animação fluida)
        notifyItemRangeChanged(position, lista.size)
    }
}
