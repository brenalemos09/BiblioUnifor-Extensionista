package com.example.bibliounifornew.features.usuario.notificacao

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.Notificacao
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Adapter para o RecyclerView de notificações (RF20).
 *
 * Funcionalidades:
 * - Carrega capa do livro via Coil a partir de [Notificacao.coverUrl]
 * - Formata o timestamp em texto relativo ("Hoje", "Ontem", "Há X dias")
 * - Suporta swipe-to-dismiss via [removerItem] (chamado pelo ItemTouchHelper)
 * - [atualizarLista] recebe a lista fresca e notifica a UI
 */
class NotificacaoAdapter(
    private val lista          : MutableList<Notificacao>,
    private val onNotifClick   : ((Notificacao) -> Unit)? = null
) : RecyclerView.Adapter<NotificacaoAdapter.NotificacaoViewHolder>() {

    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))

    // ─── VIEW HOLDER ──────────────────────────────────────────────────────────

    inner class NotificacaoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgCapa      : ImageView = itemView.findViewById(R.id.imgCapaItemNotif)
        val textTitulo   : TextView  = itemView.findViewById(R.id.textTituloItemNotif)
        val textAutor    : TextView  = itemView.findViewById(R.id.textAutorItemNotif)
        val textDescricao: TextView  = itemView.findViewById(R.id.textDescricaoItemNotif)
        val textTempo    : TextView  = itemView.findViewById(R.id.textTempoItemNotif)
        val checkLida    : CheckBox  = itemView.findViewById(R.id.checkLidaItemNotif)
    }

    // ─── ADAPTER CALLBACKS ────────────────────────────────────────────────────

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificacaoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notificacao, parent, false)
        return NotificacaoViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificacaoViewHolder, position: Int) {
        val notif = lista[position]

        // ── Capa via Coil ─────────────────────────────────────────────────────
        if (notif.coverUrl.isNotEmpty()) {
            holder.imgCapa.load(notif.coverUrl) {
                crossfade(true)
                placeholder(R.drawable.osda)
                error(R.drawable.osda)
            }
        } else {
            holder.imgCapa.setImageResource(R.drawable.osda)
        }

        // ── Textos ────────────────────────────────────────────────────────────
        holder.textTitulo.text    = notif.titulo
        holder.textAutor.text     = notif.autor
        holder.textDescricao.text = notif.descricao
        holder.textTempo.text     = formatarTempo(notif.timestamp)

        // ── Checkbox "Lida" ───────────────────────────────────────────────────
        holder.checkLida.setOnCheckedChangeListener(null)
        holder.checkLida.isChecked = notif.lida

        holder.checkLida.setOnCheckedChangeListener { _, isChecked ->
            val pos = holder.adapterPosition
            if (pos != RecyclerView.NO_ID.toInt()) {
                lista[pos].lida = isChecked
            }
        }

        // ── Clique no card ────────────────────────────────────────────────────
        holder.itemView.setOnClickListener {
            onNotifClick?.invoke(notif)
        }
    }

    override fun getItemCount(): Int = lista.size

    // ─── API PÚBLICA ──────────────────────────────────────────────────────────

    fun atualizarLista(nova: List<Notificacao>) {
        lista.clear()
        lista.addAll(nova)
        notifyDataSetChanged()
    }

    fun removerItem(position: Int) {
        if (position < 0 || position >= lista.size) return
        lista.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, lista.size)
    }

    // ─── HELPERS PRIVADOS ────────────────────────────────────────────────────

    private fun formatarTempo(timestamp: Long): String {
        if (timestamp <= 0L) return ""
        val agora = System.currentTimeMillis()
        val dias  = TimeUnit.MILLISECONDS.toDays(agora - timestamp)
        return when {
            dias == 0L -> "Hoje"
            dias == 1L -> "Ontem"
            dias < 7L  -> "Há $dias dias"
            else       -> dateFormatter.format(Date(timestamp))
        }
    }

    fun getNotificacaoAt(position: Int): Notificacao {
        return lista[position]
    }
}
