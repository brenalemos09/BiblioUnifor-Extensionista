package com.example.bibliounifornew.usuario

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class TelaRF16ListaDesejosActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf16_lista_desejos)

        // Configuração do Livro 1: Vidas Secas (ID: 1)
        configurarLivro(
            cardId = R.id.cardD1,
            btnSuaLivrariaId = R.id.btnSuaLivraria1,
            btnAlugarId = R.id.btnAlugar1,
            btnExcluirId = R.id.btnExcluir1,
            menuIconId = R.id.menuD1,
            livroId = 1,
            disponivel = true
        )

        // Configuração do Livro 2: O Ceifador (ID: 2)
        configurarLivro(
            cardId = R.id.cardD2,
            btnSuaLivrariaId = R.id.btnSuaLivraria2,
            btnAlugarId = R.id.btnAlugar2,
            btnExcluirId = R.id.btnExcluir2,
            menuIconId = R.id.menuD2,
            livroId = 2,
            disponivel = false
        )
    }

    private fun configurarLivro(
        cardId: Int,
        btnSuaLivrariaId: Int,
        btnAlugarId: Int,
        btnExcluirId: Int,
        menuIconId: Int,
        livroId: Int,
        disponivel: Boolean
    ) {
        val card = findViewById<MaterialCardView>(cardId)
        val btnSuaLivraria = findViewById<MaterialButton>(btnSuaLivrariaId)
        val btnAlugar = findViewById<MaterialButton>(btnAlugarId)
        val btnExcluir = findViewById<MaterialButton>(btnExcluirId)
        val menuIcon = findViewById<View>(menuIconId)

        // 1) BOTÃO "Minha Livraria"
        btnSuaLivraria.setOnClickListener {
            Toast.makeText(this, "Livro adicionado à sua livraria", Toast.LENGTH_SHORT).show()
        }

        // 2) BOTÃO "Alugar Livro"
        btnAlugar.setOnClickListener {
            if (disponivel) {
                Toast.makeText(this, "Livro alugado com sucesso", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Livro indisponível no momento", Toast.LENGTH_SHORT).show()
            }
        }

        // 3) BOTÃO EXCLUIR (Novo botão abaixo do Alugar)
        btnExcluir.setOnClickListener {
            card.visibility = View.GONE
            Toast.makeText(this, "Livro removido da lista de desejos", Toast.LENGTH_SHORT).show()
        }

        // 4) CLIQUE NOS 3 PONTINHOS (Navega para Detalhes)
        menuIcon.setOnClickListener {
            val intent = Intent(this, TelaRF12TelaDoLivro::class.java)
            intent.putExtra("LIVRO_ID", livroId.toString())
            startActivity(intent)
        }
    }
}
