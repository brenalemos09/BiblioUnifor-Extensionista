package com.example.bibliounifornew.features.adm.gerenciamento

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.R

class TelaRF32LivrosCRUD : AppCompatActivity() {

    private lateinit var adapter: LivrosCrudAdapter
    private val listaLivros    = mutableListOf<ItemLivroAdm>()
    private val listaCompleta  = mutableListOf<ItemLivroAdm>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf32_livroscrud)

        // ─── RECYCLERVIEW ─────────────────────────────────────────────────────
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewMidias)
        adapter = LivrosCrudAdapter(
            lista    = listaLivros,
            onEditar = { item ->
                val intent = Intent(this, TelaRF37InfoLivroADM::class.java)
                intent.putExtra("LIVRO_ID", item.docId)
                startActivity(intent)
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // ─── BOTÃO ADICIONAR MÍDIA ────────────────────────────────────────────
        findViewById<Button>(R.id.btnAdicionarMidia)?.setOnClickListener {
            startActivity(Intent(this, TelaRF33CadastroLivro::class.java))
        }

        // ─── BUSCA EM TEMPO REAL ──────────────────────────────────────────────
        findViewById<EditText>(R.id.etProcurarMidia)?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filtrarLista(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // ─── FILTRO ───────────────────────────────────────────────────────────
        findViewById<ImageView>(R.id.btnFiltro)?.setOnClickListener {
            abrirPopupFiltro()
        }

        NavigationHelperADM.configurarBarraNavegacao(this)
    }

    /**
     * Recarrega a lista ao retornar de cadastro ou edição,
     * garantindo que novos livros e alterações apareçam sem reiniciar o app.
     */
    override fun onResume() {
        super.onResume()
        carregarLivros()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CARREGAMENTO MOCK (PROTÓTIPO)
    // ─────────────────────────────────────────────────────────────────────────
    private fun carregarLivros() {
        val txtVazio = findViewById<TextView>(R.id.txtAcervoVazio)

        // Dados estáticos para o protótipo acadêmico
        listaCompleta.clear()
        listaCompleta.addAll(listOf(
            ItemLivroAdm("1", "Código Limpo", "Robert C. Martin", "8576082675", 5, ""),
            ItemLivroAdm("2", "Arquitetura Limpa", "Robert C. Martin", "8550804606", 3, ""),
            ItemLivroAdm("3", "Kotlin em Ação", "Dmitry Jemerov", "8575225693", 2, ""),
            ItemLivroAdm("4", "Design Patterns", "Erich Gamma", "8573076100", 4, ""),
            ItemLivroAdm("5", "Estruturas de Dados", "Thomas H. Cormen", "8535236996", 1, "")
        ))

        if (listaCompleta.isEmpty()) {
            txtVazio?.visibility = View.VISIBLE
            adapter.atualizarLista(emptyList())
        } else {
            txtVazio?.visibility = View.GONE
            adapter.atualizarLista(listaCompleta)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FILTRO LOCAL
    // ─────────────────────────────────────────────────────────────────────────
    private fun filtrarLista(termo: String) {
        val txtVazio = findViewById<TextView>(R.id.txtAcervoVazio)
        val filtrado = if (termo.isBlank()) {
            listaCompleta.toList()
        } else {
            listaCompleta.filter { livro ->
                livro.titulo.contains(termo, ignoreCase = true) ||
                livro.autor.contains(termo, ignoreCase = true)  ||
                livro.isbn.contains(termo, ignoreCase = true)
            }
        }
        adapter.atualizarLista(filtrado)
        txtVazio?.visibility = if (filtrado.isEmpty()) View.VISIBLE else View.GONE
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POPUP FILTRO AVANÇADO
    // ─────────────────────────────────────────────────────────────────────────
    private fun abrirPopupFiltro() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_filtro_verificar_midia)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val editTitulo = dialog.findViewById<EditText>(R.id.editTituloFiltro)
        val editAutor  = dialog.findViewById<EditText>(R.id.editAutorFiltro)
        val editIsbn   = dialog.findViewById<EditText>(R.id.editISBNFiltro)
        val btnSalvar  = dialog.findViewById<Button>(R.id.buttonSalvarFiltro)
        val btnLimpar  = dialog.findViewById<Button>(R.id.buttonLimparFiltro)

        btnSalvar?.setOnClickListener {
            val termoTitulo = editTitulo?.text.toString()
            val termoAutor  = editAutor?.text.toString()
            val termoIsbn   = editIsbn?.text.toString()

            val txtVazio = findViewById<TextView>(R.id.txtAcervoVazio)
            val filtrado = listaCompleta.filter { livro ->
                (termoTitulo.isBlank() || livro.titulo.contains(termoTitulo, ignoreCase = true)) &&
                (termoAutor.isBlank()  || livro.autor.contains(termoAutor,   ignoreCase = true)) &&
                (termoIsbn.isBlank()   || livro.isbn.contains(termoIsbn,     ignoreCase = true))
            }
            adapter.atualizarLista(filtrado)
            txtVazio?.visibility = if (filtrado.isEmpty()) View.VISIBLE else View.GONE

            Toast.makeText(this, getString(R.string.msg_filtro_aplicado), Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        btnLimpar?.setOnClickListener {
            editTitulo?.setText("")
            editAutor?.setText("")
            editIsbn?.setText("")
            adapter.atualizarLista(listaCompleta)
            findViewById<TextView>(R.id.txtAcervoVazio)?.visibility = View.GONE
            Toast.makeText(this, getString(R.string.msg_filtros_limpos), Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
        dialog.window?.attributes?.width = (resources.displayMetrics.widthPixels * 0.9).toInt()
    }
}
