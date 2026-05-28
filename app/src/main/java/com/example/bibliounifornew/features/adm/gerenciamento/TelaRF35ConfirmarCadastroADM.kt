package com.example.bibliounifornew.features.adm.gerenciamento

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.R
import android.os.Handler
import android.os.Looper

class TelaRF35ConfirmarCadastroADM : AppCompatActivity() {

    private lateinit var adapter    : ConfirmacaoAdapter
    private val listaPendentes      = mutableListOf<ItemUsuarioPendente>()
    private val listaCompleta       = mutableListOf<ItemUsuarioPendente>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf35_confirmar_cadastro_adm)

        // ─── RECYCLERVIEW ─────────────────────────────────────────────────────
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewConfirmacao)
        adapter = ConfirmacaoAdapter(listaPendentes) { item, position ->
            exibirPopupConfirmacao(item, position)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // ─── BARRA DE PESQUISA ────────────────────────────────────────────────
        val editPesquisa = findViewById<EditText>(R.id.editPesquisarUsuario)
        editPesquisa?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filtrarLista(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        carregarPendentesMock()
        NavigationHelperADM.configurarBarraNavegacao(this)
    }

    /**
     * Carrega usuários mockados.
     */
    private fun carregarPendentesMock() {
        listaCompleta.clear()
        listaCompleta.add(ItemUsuarioPendente("1", "Carlos Andrade", "carlos@email.com"))
        listaCompleta.add(ItemUsuarioPendente("2", "Fernanda Lima", "fernanda@email.com"))
        listaCompleta.add(ItemUsuarioPendente("3", "João Silva", "joao@email.com"))
        filtrarLista("")
    }

    private fun filtrarLista(query: String) {
        val filtrado = if (query.isBlank()) listaCompleta
        else listaCompleta.filter {
            it.nome.contains(query, ignoreCase = true) || it.email.contains(query, ignoreCase = true)
        }
        listaPendentes.clear()
        listaPendentes.addAll(filtrado)
        adapter.notifyDataSetChanged()
    }

    /**
     * Popup de confirmação de cadastro — Simulado.
     */
    private fun exibirPopupConfirmacao(item: ItemUsuarioPendente, position: Int) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_confirmacao_usuario)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnConfirmar = dialog.findViewById<Button>(R.id.btnConfirmarCadastro)
        val btnVoltar    = dialog.findViewById<TextView>(R.id.textVoltar)

        btnConfirmar?.setOnClickListener {
            btnConfirmar.isEnabled = false
            Handler(Looper.getMainLooper()).postDelayed({
                dialog.dismiss()
                Toast.makeText(this, getString(R.string.fmt_cadastro_confirmado, item.nome), Toast.LENGTH_SHORT).show()
                listaCompleta.removeAll { it.uid == item.uid }
                adapter.removerItem(position)
            }, 500)
        }

        btnVoltar?.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
}
