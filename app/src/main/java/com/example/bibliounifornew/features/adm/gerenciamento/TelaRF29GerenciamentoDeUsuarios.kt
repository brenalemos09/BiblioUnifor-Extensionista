package com.example.bibliounifornew.features.adm.gerenciamento

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.R

class TelaRF29GerenciamentoDeUsuarios : AppCompatActivity() {

    private lateinit var adapter: UsuariosAdmAdapter
    private val listaUsuarios  = mutableListOf<ItemUsuarioAdm>()
    private val listaCompleta  = mutableListOf<ItemUsuarioAdm>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf29_gerenciamentousuarios)

        // ─── RECYCLERVIEW ─────────────────────────────────────────────────────
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewUsuariosAdm)
        adapter = UsuariosAdmAdapter(listaUsuarios) { item ->
            val intent = Intent(this, TelaRF30UsuariosParaADM::class.java)
            intent.putExtra("USUARIO_ID",    item.uid)
            intent.putExtra("USUARIO_NOME",  item.nome)
            intent.putExtra("USUARIO_EMAIL", item.email)
            startActivity(intent)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // ─── BUSCA ────────────────────────────────────────────────────────────
        val editBusca = findViewById<EditText>(R.id.editBuscarUsuario)
        editBusca?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filtrarLista(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        NavigationHelperADM.configurarBarraNavegacao(this)
    }

    override fun onResume() {
        super.onResume()
        carregarUsuariosMock()
    }

    private fun carregarUsuariosMock() {
        listaCompleta.clear()
        listaCompleta.add(ItemUsuarioAdm("1", "João Silva", "joao@email.com", "joaosilva"))
        listaCompleta.add(ItemUsuarioAdm("2", "Maria Oliveira", "maria@email.com", "maria_oli"))
        listaCompleta.add(ItemUsuarioAdm("3", "Carlos Souza", "carlos@email.com", "csouza"))
        listaCompleta.add(ItemUsuarioAdm("4", "Ana Costa", "ana@email.com", "anacosta"))
        listaCompleta.add(ItemUsuarioAdm("5", "Pedro Santos", "pedro@email.com", "psantos"))
        
        filtrarLista("")
    }

    private fun filtrarLista(query: String) {
        val filtrado = if (query.isBlank()) listaCompleta
        else listaCompleta.filter {
            it.nome.contains(query, ignoreCase = true) ||
            it.email.contains(query, ignoreCase = true) ||
            it.usuario.contains(query, ignoreCase = true)
        }
        listaUsuarios.clear()
        listaUsuarios.addAll(filtrado)
        adapter.notifyDataSetChanged()
    }
}

