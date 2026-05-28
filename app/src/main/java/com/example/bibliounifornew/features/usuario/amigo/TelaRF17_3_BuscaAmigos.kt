package com.example.bibliounifornew.features.usuario.amigo

import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.R
import com.google.android.material.button.MaterialButton

class TelaRF17_3_BuscaAmigos : AppCompatActivity() {

    private lateinit var adapter       : BuscaAmigoAdapter
    private val listaCompleta          = mutableListOf<UsuarioAmigo>()
    private val listaFiltrada          = mutableListOf<UsuarioAmigo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf17_3_busca_amigos)

        // ─── RECYCLERVIEW ─────────────────────────────────────────────────────
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewBuscaAmigos)
        adapter = BuscaAmigoAdapter(listaFiltrada) { usuario ->
            enviarSolicitacaoAmizadeMock(usuario)
        }
        recyclerView?.layoutManager = LinearLayoutManager(this)
        recyclerView?.adapter = adapter

        // ─── BUSCA POR TEXTO ──────────────────────────────────────────────────
        val editBuscar  = findViewById<EditText>(R.id.editBuscarAmigo)
        val btnProcurar = findViewById<MaterialButton>(R.id.buttonBuscarAmigo)

        btnProcurar?.setOnClickListener {
            val texto = editBuscar.text.toString().trim().lowercase()
            filtrarLista(texto)
        }

        carregarUsuariosMock()
    }

    private fun carregarUsuariosMock() {
        listaCompleta.clear()
        listaCompleta.add(UsuarioAmigo("4", "Carlos Eduardo", "cadu"))
        listaCompleta.add(UsuarioAmigo("5", "Beatriz Lima", "bia_lima"))
        listaCompleta.add(UsuarioAmigo("6", "Fernando Costa", "fernandoc"))
        filtrarLista("")
    }

    private fun filtrarLista(texto: String) {
        listaFiltrada.clear()
        if (texto.isEmpty()) {
            listaFiltrada.addAll(listaCompleta)
        } else {
            listaCompleta.filterTo(listaFiltrada) {
                it.nome.lowercase().contains(texto) || it.usuario.lowercase().contains(texto)
            }
        }
        adapter.notifyDataSetChanged()

        if (listaFiltrada.isEmpty() && texto.isNotEmpty()) {
            Toast.makeText(this, "Nenhum usuário encontrado para \"$texto\".", Toast.LENGTH_SHORT).show()
        }
    }

    private fun enviarSolicitacaoAmizadeMock(destinatario: UsuarioAmigo) {
        Toast.makeText(
            this,
            "Solicitação de amizade enviada para ${destinatario.nome}!",
            Toast.LENGTH_SHORT
        ).show()
        listaCompleta.remove(destinatario)
        filtrarLista(findViewById<EditText>(R.id.editBuscarAmigo).text.toString().trim().lowercase())
    }
}
