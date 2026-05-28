package com.example.bibliounifornew.features.adm.gerenciamento

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.bibliounifornew.R
import com.example.bibliounifornew.features.adm.solicitacoes.ItemAluguel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TelaRFAdmUsuarioAlugados : AppCompatActivity() {

    private lateinit var adapter: AlugueisUsuarioAdapter
    private val listaAlugueis = mutableListOf<ItemAluguel>()

    private var usuarioId   : String = ""
    private var usuarioNome : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf_adm_usuario_alugados)

        usuarioId   = intent.getStringExtra("USUARIO_ID")   ?: ""
        usuarioNome = intent.getStringExtra("USUARIO_NOME") ?: "Usuário"

        val rv = findViewById<RecyclerView>(R.id.recyclerViewAlugueis)
        rv.layoutManager = LinearLayoutManager(this)
        adapter = AlugueisUsuarioAdapter(listaAlugueis)
        rv.adapter = adapter

        carregarDadosUsuarioMock()
        carregarLivrosAlugadosMock()

        NavigationHelperADM.configurarBarraNavegacao(this)
    }

    private fun carregarDadosUsuarioMock() {
        findViewById<TextView>(R.id.textNomeUsuario)?.text  = usuarioNome
        findViewById<TextView>(R.id.textEmailUsuario)?.text = "usuario@email.com"
    }

    private fun carregarLivrosAlugadosMock() {
        listaAlugueis.clear()
        listaAlugueis.add(
            ItemAluguel(
                docId = "1",
                uidAluno = usuarioId,
                idLivro = "L1",
                dataMs = System.currentTimeMillis() - 86400000 * 5,
                status = "ativo",
                nomeUsuario = usuarioNome,
                tituloLivro = "O Senhor dos Anéis",
                autorLivro = "J.R.R. Tolkien",
                coverUrl = ""
            )
        )
        listaAlugueis.add(
            ItemAluguel(
                docId = "2",
                uidAluno = usuarioId,
                idLivro = "L2",
                dataMs = System.currentTimeMillis() - 86400000 * 10,
                status = "devolvido",
                nomeUsuario = usuarioNome,
                tituloLivro = "Dom Casmurro",
                autorLivro = "Machado de Assis",
                coverUrl = ""
            )
        )
        adapter.notifyDataSetChanged()
    }
}


// ─── ADAPTER ─────────────────────────────────────────────────────────────────

/**
 * Adapter para exibir livros alugados de um único usuário.
 *
 * RF28.6 FIX — Simulação local sem backend:
 * usa [ItemAluguel.coverUrl] e [ItemAluguel.tituloLivro] pré-carregados.
 */
class AlugueisUsuarioAdapter(
    private val lista: List<ItemAluguel>
) : RecyclerView.Adapter<AlugueisUsuarioAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nomeUsuario : TextView  = view.findViewById(R.id.txtNomeUsuarioAluguel)
        val titulo      : TextView  = view.findViewById(R.id.txtTituloAluguel)
        val autor       : TextView  = view.findViewById(R.id.txtAutorAluguel)
        val data        : TextView  = view.findViewById(R.id.txtDataAluguel)
        val capa        : ImageView = view.findViewById(R.id.imgCapaAluguel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_aluguel_adm, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]
        holder.nomeUsuario.text = item.nomeUsuario
        holder.titulo.text      = item.tituloLivro
        holder.autor.text       = item.autorLivro

        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
        holder.data.text = if (item.dataMs > 0) sdf.format(Date(item.dataMs)) else "--/--/----"

        // Usa a coverUrl pré-carregada — Simulação local sem query no bind
        holder.capa.load(item.coverUrl.ifEmpty { R.drawable.osda }) {
            placeholder(R.drawable.osda)
            error(R.drawable.osda)
        }
    }

    override fun getItemCount(): Int = lista.size
}
