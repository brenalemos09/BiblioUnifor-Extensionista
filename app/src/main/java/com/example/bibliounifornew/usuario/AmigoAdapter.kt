package com.example.bibliounifornew.usuario

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.R

/**
 * Modelo leve de usuário para a lista de amigos/usuários cadastrados.
 */
data class UsuarioAmigo(
    val uid   : String = "",
    val nome  : String = "Usuário",
    val usuario: String = ""
)

/**
 * Adapter para o RecyclerView de Amigos (RF17).
 * Usa item_amigo.xml que já está no projeto.
 * Ao clicar, abre TelaRF17_5_PerfilAmigo passando o UID do usuário selecionado.
 */
class AmigoAdapter(
    private val lista: List<UsuarioAmigo>
) : RecyclerView.Adapter<AmigoAdapter.AmigoViewHolder>() {

    inner class AmigoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgAmigo     : ImageView = itemView.findViewById(R.id.imgAmigo)
        val txtNomeAmigo : TextView  = itemView.findViewById(R.id.txtNomeAmigo)
        val btnMaisAmigo : ImageView = itemView.findViewById(R.id.btnMaisAmigo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AmigoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_amigo, parent, false)
        return AmigoViewHolder(view)
    }

    override fun onBindViewHolder(holder: AmigoViewHolder, position: Int) {
        val amigo = lista[position]
        holder.txtNomeAmigo.text = amigo.nome

        val abrirPerfil = View.OnClickListener {
            val intent = Intent(holder.itemView.context, TelaRF17_5_PerfilAmigo::class.java)
            intent.putExtra("AMIGO_UID", amigo.uid)
            intent.putExtra("AMIGO_NOME", amigo.nome)
            holder.itemView.context.startActivity(intent)
        }

        holder.itemView.setOnClickListener(abrirPerfil)
        holder.imgAmigo.setOnClickListener(abrirPerfil)
        holder.txtNomeAmigo.setOnClickListener(abrirPerfil)
        holder.btnMaisAmigo.setOnClickListener(abrirPerfil)
    }

    override fun getItemCount(): Int = lista.size
}
