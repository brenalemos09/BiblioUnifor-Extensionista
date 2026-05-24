package com.example.bibliounifornew.usuario

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.R
import com.google.android.material.button.MaterialButton

data class ItemLivraria(
    val livroId      : String = "",
    val titulo       : String = "",
    val autor        : String = "",
    val statusLeitura: String = "Não Lido"
)

class LivrariaAdapter(
    private val lista: MutableList<ItemLivraria>,
    private val onRemover: (ItemLivraria, Int) -> Unit
) : RecyclerView.Adapter<LivrariaAdapter.LivrariaViewHolder>() {

    inner class LivrariaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtTitulo : TextView      = itemView.findViewById(R.id.txtTituloLivraria)
        val txtAutor  : TextView      = itemView.findViewById(R.id.txtAutorLivraria)
        val txtStatus : TextView      = itemView.findViewById(R.id.txtStatusLivraria)
        val btnMenu   : ImageView     = itemView.findViewById(R.id.btnMenuLivraria)
        val btnRemover: MaterialButton = itemView.findViewById(R.id.btnRemoverLivraria)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LivrariaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_livro_livraria, parent, false)
        return LivrariaViewHolder(view)
    }

    override fun onBindViewHolder(holder: LivrariaViewHolder, position: Int) {
        val item = lista[position]
        holder.txtTitulo.text = item.titulo.ifEmpty { item.livroId }
        holder.txtAutor.text  = item.autor
        holder.txtStatus.text = item.statusLeitura

        // Abre RF12 passando o livroId correto — sem crash de ID hardcoded
        val abrirDetalhes = View.OnClickListener {
            val intent = Intent(holder.itemView.context, TelaRF12TelaDoLivro::class.java)
            intent.putExtra("LIVRO_ID", item.livroId)
            holder.itemView.context.startActivity(intent)
        }
        holder.itemView.setOnClickListener(abrirDetalhes)
        holder.btnMenu.setOnClickListener(abrirDetalhes)

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
