package com.example.bibliounifornew.features.usuario.amigo

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.bibliounifornew.R

/**
 * Modelo leve de usuário para a lista de amigos.
 * fotoUrl é carregado da subcoleção usuarios/{uid}/amigos — pode estar vazio.
 */
data class UsuarioAmigo(
    val uid    : String = "",
    val nome   : String = "Usuário",
    val usuario: String = "",
    val fotoUrl: String = ""
)

/**
 * Adapter para o RecyclerView de Amigos (RF17).
 * Carrega avatares via Coil com tamanho limitado a 200×200 px para evitar
 * pressão de memória ao exibir muitos itens em sequência.
 */
class AmigoAdapter(
    private val lista: List<UsuarioAmigo>
) : RecyclerView.Adapter<AmigoAdapter.AmigoViewHolder>() {

    inner class AmigoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgAmigo    : ImageView = itemView.findViewById(R.id.imgAmigo)
        val txtNomeAmigo: TextView  = itemView.findViewById(R.id.txtNomeAmigo)
        val btnMaisAmigo: ImageView = itemView.findViewById(R.id.btnMaisAmigo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AmigoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_amigo, parent, false)
        return AmigoViewHolder(view)
    }

    override fun onBindViewHolder(holder: AmigoViewHolder, position: Int) {
        val amigo = lista[position]
        holder.txtNomeAmigo.text = amigo.nome

        // Avatar via Coil: tamanho fixo para evitar OOM ao reciclar muitos itens.
        // Crossfade reduz o salto visual entre placeholder e imagem real.
        holder.imgAmigo.load(amigo.fotoUrl.ifEmpty { null }) {
            size(200, 200)
            crossfade(true)
            placeholder(R.drawable.user_placeholder)
            error(R.drawable.user_placeholder)
            fallback(R.drawable.user_placeholder)
        }

        val abrirPerfil = View.OnClickListener {
            val intent = Intent(holder.itemView.context, TelaRF17_5_PerfilAmigo::class.java)
            intent.putExtra("AMIGO_UID",  amigo.uid)
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
