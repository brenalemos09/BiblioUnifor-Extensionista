package com.example.bibliounifornew.adm

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

data class ItemAluguel(
    val docId      : String = "",
    val uidAluno   : String = "",
    val idLivro    : String = "",
    val nomeUsuario: String = "Usuário",
    val tituloLivro: String = "Título Indisponível",
    val autorLivro : String = "Autor Desconhecido",
    val dataMs     : Long   = 0L,
    val status     : String = "pendente"
)

class AlugueisAdapter(
    private val lista          : MutableList<ItemAluguel>,
    private val onVerLivro     : (ItemAluguel) -> Unit,
    private val onVerUsuario   : (ItemAluguel) -> Unit
) : RecyclerView.Adapter<AlugueisAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtNome   : TextView      = view.findViewById(R.id.txtNomeUsuarioAluguel)
        val txtTitulo : TextView      = view.findViewById(R.id.txtTituloAluguel)
        val txtAutor  : TextView      = view.findViewById(R.id.txtAutorAluguel)
        val txtData   : TextView      = view.findViewById(R.id.txtDataAluguel)
        val btnLivro  : MaterialButton = view.findViewById(R.id.btnVerLivroAluguel)
        val btnUsuario: MaterialButton = view.findViewById(R.id.btnVerUsuarioAluguel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_aluguel_adm, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]
        holder.txtNome.text   = item.nomeUsuario
        holder.txtTitulo.text = item.tituloLivro
        holder.txtAutor.text  = item.autorLivro
        holder.txtData.text   = if (item.dataMs > 0L)
            SimpleDateFormat("dd MMM yyyy", Locale("pt", "BR")).format(Date(item.dataMs))
        else "--/--/----"

        holder.btnLivro.setOnClickListener   { onVerLivro(item)    }
        holder.btnUsuario.setOnClickListener { onVerUsuario(item)  }
    }

    override fun getItemCount(): Int = lista.size

    fun atualizarLista(novaLista: List<ItemAluguel>) {
        lista.clear()
        lista.addAll(novaLista)
        notifyDataSetChanged()
    }
}
