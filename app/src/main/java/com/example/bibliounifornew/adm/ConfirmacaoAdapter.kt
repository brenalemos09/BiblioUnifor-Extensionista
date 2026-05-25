package com.example.bibliounifornew.adm

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.R

data class ItemUsuarioPendente(
    val uid  : String = "",
    val nome : String = "Usuário",
    val email: String = ""
)

class ConfirmacaoAdapter(
    private val lista    : MutableList<ItemUsuarioPendente>,
    private val onClick  : (ItemUsuarioPendente, Int) -> Unit
) : RecyclerView.Adapter<ConfirmacaoAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtNome : TextView = view.findViewById(R.id.txtNomeConfirmacao)
        val txtEmail: TextView = view.findViewById(R.id.txtEmailConfirmacao)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_usuario_confirmacao, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]
        holder.txtNome.text  = item.nome
        holder.txtEmail.text = item.email
        holder.itemView.setOnClickListener { onClick(item, position) }
    }

    override fun getItemCount(): Int = lista.size

    fun removerItem(position: Int) {
        lista.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, lista.size)
    }
}
