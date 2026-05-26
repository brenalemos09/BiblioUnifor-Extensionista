package com.example.bibliounifornew.features.adm.dashboard

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R
import com.example.bibliounifornew.features.adm.gerenciamento.TelaRF29GerenciamentoDeUsuarios
import com.example.bibliounifornew.features.adm.gerenciamento.TelaRF32LivrosCRUD
import com.example.bibliounifornew.features.adm.gerenciamento.TelaRF33CadastroLivro
import com.google.android.material.button.MaterialButton

class TelaRF28CrudADM : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf27_crud_adm)


        val btnCriarMidia = findViewById<MaterialButton>(R.id.buttonCriarMidia)
        val btnVerificarMidia = findViewById<MaterialButton>(R.id.buttonVerificarMidia)
        val btnGerenciarUsuario = findViewById<MaterialButton>(R.id.buttonGerenciarUsuarios)

        btnCriarMidia.setOnClickListener {
            val intent = Intent(this@TelaRF28CrudADM, TelaRF33CadastroLivro::class.java)
            startActivity(intent)
        }

        btnVerificarMidia.setOnClickListener {
            val intent = Intent(this@TelaRF28CrudADM, TelaRF32LivrosCRUD::class.java)
            startActivity(intent)
        }

        btnGerenciarUsuario.setOnClickListener {
            val intent = Intent(this@TelaRF28CrudADM, TelaRF29GerenciamentoDeUsuarios::class.java)
            startActivity(intent)
        }

        // Botão de voltar (Simulado ou se houver um header com voltar)
        // Se desejar adicionar um botão de voltar no layout futuramente.
    }
}
