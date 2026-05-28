package com.example.bibliounifornew.features.usuario.biblioteca

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.Button
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.R
import com.example.bibliounifornew.features.usuario.perfil.NavigationHelper

class TelaRF21Historico : AppCompatActivity() {

    private lateinit var adapter  : HistoricoAdapter
    private val listaHistorico    = mutableListOf<ItemHistorico>()
    private val listaFiltrada     = mutableListOf<ItemHistorico>()
    private var usuarioId         : String = "mock_user_123"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf21_historico)

        // ─── CABEÇALHO ────────────────────────────────────────────────────────
        val textCabecalho = findViewById<TextView>(R.id.textEmailHistorico)
        textCabecalho?.text = "joao.silva@unifor.br"

        NavigationHelper.configurarBarraNavegacao(this)

        // ─── PESQUISA ────────────────────────────────────────────────────────
        val editPesquisa = findViewById<EditText>(R.id.editPesquisaHistorico)
        editPesquisa?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filtrar(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // ─── RECYCLERVIEW ─────────────────────────────────────────────────────
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewHistorico)
        adapter = HistoricoAdapter(listaFiltrada) { item, position ->
            showPopupRemover(item.titulo) {
                val originalPos = listaHistorico.indexOf(item)
                if (originalPos != -1) listaHistorico.removeAt(originalPos)
                
                adapter.removerItem(position)
                Toast.makeText(this, "Item removido do seu histórico.", Toast.LENGTH_SHORT).show()
            }
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        carregarHistorico()
    }

    private fun carregarHistorico() {
        listaHistorico.clear()
        val mockData = listOf(
            ItemHistorico("1", "O Codificador Limpo", "Robert C. Martin", "Lido", System.currentTimeMillis() - 86400000 * 5),
            ItemHistorico("2", "Design Patterns", "Gang of Four", "Lido", System.currentTimeMillis() - 86400000 * 15),
            ItemHistorico("3", "Refatoração", "Martin Fowler", "Lido", System.currentTimeMillis() - 86400000 * 30),
            ItemHistorico("4", "Kotlin em Ação", "Dmitry Jemerov", "Lido", System.currentTimeMillis() - 86400000 * 45)
        )
        listaHistorico.addAll(mockData)
        listaFiltrada.clear()
        listaFiltrada.addAll(listaHistorico)
        adapter.notifyDataSetChanged()
    }

    private fun filtrar(texto: String) {
        listaFiltrada.clear()
        if (texto.isEmpty()) {
            listaFiltrada.addAll(listaHistorico)
        } else {
            val query = texto.lowercase()
            listaHistorico.forEach {
                if (it.titulo.lowercase().contains(query) || it.autor.lowercase().contains(query)) {
                    listaFiltrada.add(it)
                }
            }
        }
        adapter.notifyDataSetChanged()
    }

    private fun showPopupRemover(nomeLivro: String, onConfirm: () -> Unit) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_remover_historico)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val textMensagem = dialog.findViewById<TextView>(R.id.textMensagemPopupRemoverHistorico)
        val btnConfirmar = dialog.findViewById<Button>(R.id.buttonPopupRemoverHistorico)
        val btnCancelar  = dialog.findViewById<TextView>(R.id.textCancelarPopupRemoverHistorico)

        textMensagem?.text = "Tem certeza que deseja remover \"$nomeLivro\" do seu histórico?"

        btnConfirmar?.setOnClickListener {
            onConfirm()
            dialog.dismiss()
        }

        btnCancelar?.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }
}
