package com.example.bibliounifornew.features.usuario.livro

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.EntidadeLivro
import com.example.bibliounifornew.data.MockData
import com.example.bibliounifornew.features.usuario.biblioteca.TelaRF14LeituraActivity
import com.example.bibliounifornew.features.usuario.perfil.NavigationHelper

class TelaRF11_1_ResultadoPesquisa : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LivroAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf11_1_resultado_pesquisa)

        val termoPesquisa = intent.getStringExtra("TERMO_PESQUISA") ?: ""
        val textResultado = findViewById<TextView>(R.id.textResultadoTitulo)

        textResultado.text = "Resultado: \"$termoPesquisa\""

        configurarRecyclerView()

        if (termoPesquisa.isNotEmpty()) {
            realizarBuscaMock(termoPesquisa)
        } else {
            realizarBuscaMock("")
        }

        NavigationHelper.configurarBarraNavegacao(this)
    }

    private fun configurarRecyclerView() {
        recyclerView = findViewById(R.id.recyclerViewResultados)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = LivroAdapter(
            livros        = mutableListOf(),
            onItemClick   = { livro ->
                startActivity(
                    Intent(this, TelaRF12TelaDoLivro::class.java)
                        .putExtra("LIVRO_ID", livro.id)
                )
            },
            onSuaLivraria = { livro -> 
                Toast.makeText(this, "\"${livro.title}\" adicionado à sua Livraria!", Toast.LENGTH_SHORT).show()
            },
            onAlugarLivro = { livro ->
                startActivity(
                    Intent(this, TelaRF14LeituraActivity::class.java)
                        .putExtra("LIVRO_ID", livro.id)
                )
            }
        )

        recyclerView.adapter = adapter
    }

    private fun realizarBuscaMock(termo: String) {
        val listaMock = MockData.livros
        
        val resultados = if (termo.isEmpty()) {
            listaMock
        } else {
            listaMock.filter {
                it.title.contains(termo, ignoreCase = true) ||
                        it.author.contains(termo, ignoreCase = true)
            }
        }
        
        adapter.updateData(resultados)
    }
}
