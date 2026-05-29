package com.example.bibliounifornew.features.adm.solicitacoes

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.bibliounifornew.R
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AlugueisAdapter(
    private val lista        : MutableList<ItemAluguel>,
    private val onVerLivro   : (ItemAluguel) -> Unit,
    private val onVerUsuario : (ItemAluguel) -> Unit,
    private val onAprovar    : (ItemAluguel) -> Unit,
    private val onReceber    : (ItemAluguel) -> Unit
) : RecyclerView.Adapter<AlugueisAdapter.ViewHolder>() {

    private val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgAvatar  : ImageView     = view.findViewById(R.id.imgAvatarUsuarioAluguel)
        val imgCapa    : ImageView     = view.findViewById(R.id.imgCapaAluguel)
        val txtNome    : TextView      = view.findViewById(R.id.txtNomeUsuarioAluguel)
        val txtTitulo  : TextView      = view.findViewById(R.id.txtTituloAluguel)
        val txtAutor   : TextView      = view.findViewById(R.id.txtAutorAluguel)
        val txtData    : TextView      = view.findViewById(R.id.txtDataAluguel)
        val txtStatus  : TextView      = view.findViewById(R.id.txtStatusAluguelAdm)
        val btnLivro   : MaterialButton = view.findViewById(R.id.btnVerLivroAluguel)
        val btnUsuario : MaterialButton = view.findViewById(R.id.btnVerUsuarioAluguel)
        val btnAprovar : MaterialButton = view.findViewById(R.id.btnAprovarAluguel)
        val btnReceber : MaterialButton = view.findViewById(R.id.btnReceberLivro)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_aluguel_adm, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]

        // ── Textos ────────────────────────────────────────────────────────────
        holder.txtNome.text   = item.nomeUsuario
        holder.txtTitulo.text = item.tituloLivro
        holder.txtAutor.text  = item.autorLivro
        holder.txtData.text   = if (item.dataMs > 0L) sdf.format(Date(item.dataMs)) else "--/--/----"

        // ── Avatar do aluno via Coil — fallback neutro user_placeholder ────────
        holder.imgAvatar.load(item.fotoUsuario.ifEmpty { null }) {
            size(120, 120)
            crossfade(true)
            placeholder(R.drawable.user_placeholder)
            error(R.drawable.user_placeholder)
            fallback(R.drawable.user_placeholder)
        }

        // ── Capa do livro via Coil — fallback ic_sem_capa (sem Tolkien) ────────
        holder.imgCapa.load(item.coverUrl.ifEmpty { null }) {
            crossfade(true)
            placeholder(R.drawable.ic_sem_capa)
            error(R.drawable.ic_sem_capa)
            fallback(R.drawable.ic_sem_capa)
        }

        // ── Status badge com cor contextual ───────────────────────────────────
        val (statusLabel, statusColor) = when (item.status.lowercase()) {
            "pendente"  -> "Aguardando aprovação" to Color.parseColor("#E65100")
            "ativo"     -> {
                val devolucao = if (item.dataDevolucao > 0L)
                    " — Devolução: ${sdf.format(Date(item.dataDevolucao))}" else ""
                "Ativo$devolucao" to Color.parseColor("#2E7D32")
            }
            "atrasado"  -> "Em atraso" to Color.parseColor("#C62828")
            "devolvido" -> "Devolvido" to Color.parseColor("#757575")
            else        -> item.status to Color.parseColor("#444444")
        }
        holder.txtStatus.text = statusLabel
        holder.txtStatus.setTextColor(statusColor)

        // ── Navegação ─────────────────────────────────────────────────────────
        holder.btnLivro.setOnClickListener   { onVerLivro(item)   }
        holder.btnUsuario.setOnClickListener { onVerUsuario(item) }

        // ── Ação condicional por status ───────────────────────────────────────
        when {
            item.status.equals("pendente", ignoreCase = true) -> {
                holder.btnAprovar.visibility = View.VISIBLE
                holder.btnReceber.visibility = View.GONE
                holder.btnAprovar.setOnClickListener { onAprovar(item) }
            }
            item.status.equals("ativo",    ignoreCase = true) ||
            item.status.equals("atrasado", ignoreCase = true) -> {
                holder.btnAprovar.visibility = View.GONE
                holder.btnReceber.visibility = View.VISIBLE
                holder.btnReceber.setOnClickListener { onReceber(item) }
            }
            else -> {
                holder.btnAprovar.visibility = View.GONE
                holder.btnReceber.visibility = View.GONE
            }
        }
    }

    override fun getItemCount(): Int = lista.size

    fun atualizarLista(novaLista: List<ItemAluguel>) {
        lista.clear()
        lista.addAll(novaLista)
        notifyDataSetChanged()
    }

    fun removerItem(docId: String) {
        val idx = lista.indexOfFirst { it.docId == docId }
        if (idx >= 0) {
            lista.removeAt(idx)
            notifyItemRemoved(idx)
            notifyItemRangeChanged(idx, lista.size)
        }
    }
}
