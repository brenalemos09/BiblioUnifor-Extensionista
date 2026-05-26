package com.example.bibliounifornew.features.usuario.biblioteca

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.R
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ItemHistorico(
    val livroId  : String = "",
    val titulo   : String = "",
    val autor    : String = "",
    val dataLido : Long   = 0L
)

class HistoricoAdapter(
    private val lista: MutableList<ItemHistorico>,
    private val onRemover: (ItemHistorico, Int) -> Unit
) : RecyclerView.Adapter<HistoricoAdapter.HistoricoViewHolder>() {

    inner class HistoricoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtTitulo : TextView       = itemView.findViewById(R.id.txtTituloHistorico)
        val txtAutor  : TextView       = itemView.findViewById(R.id.txtAutorHistorico)
        val txtData   : TextView       = itemView.findViewById(R.id.txtDataHistorico)
        val btnRemover: MaterialButton = itemView.findViewById(R.id.btnRemoverHistorico)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoricoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_historico, parent, false)
        return HistoricoViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoricoViewHolder, position: Int) {
        val item = lista[position]
        holder.txtTitulo.text = item.titulo
        holder.txtAutor.text  = item.autor
        holder.txtData.text   = if (item.dataLido > 0L) {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(item.dataLido))
        } else {
            "—"
        }
        holder.btnRemover.setOnClickListener {
            onRemover(item, holder.adapterPosition)
        }
    }

    override fun getItemCount(): Int = lista.size

    fun removerItem(position: Int) {
        lista.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, lista.size)
    }
}
