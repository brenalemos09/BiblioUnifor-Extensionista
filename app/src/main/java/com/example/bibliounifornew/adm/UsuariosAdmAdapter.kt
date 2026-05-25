package com.example.bibliounifornew.adm

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.R

data class ItemUsuarioAdm(
    val uid    : String = "",
    val nome   : String = "Usuário",
    val email  : String = "",
    val usuario: String = ""
)

class UsuariosAdmAdapter(
    private val lista  : MutableList<ItemUsuarioAdm>,
    private val onClick: (ItemUsuarioAdm) -> Unit
) : RecyclerView.Adapter<UsuariosAdmAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtNome : TextView = view.findViewById(R.id.txtNomeUsuarioAdm)
        val txtEmail: TextView = view.findViewById(R.id.txtEmailUsuarioAdm)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_usuario_adm, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]
        holder.txtNome.text  = item.nome
        holder.txtEmail.text = item.email
        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount(): Int = lista.size

    fun atualizarLista(novaLista: List<ItemUsuarioAdm>) {
        lista.clear()
        lista.addAll(novaLista)
        notifyDataSetChanged()
    }
}
