package com.example.bibliounifornew.features.usuario.solicitacao

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R
import com.example.bibliounifornew.features.usuario.livro.TelaRF12TelaDoLivro

class TelaRF19SolicitacoesVoltarBiblioteca : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf19_solicitacoes_voltar_biblioteca)

        val buttonVoltarBiblioteca = findViewById<Button>(R.id.buttonPopupOkSolicitacao)

        buttonVoltarBiblioteca?.setOnClickListener {
            val context: Context = this@TelaRF19SolicitacoesVoltarBiblioteca
            val intent = Intent(context, TelaRF12TelaDoLivro::class.java)
            startActivity(intent)
        }
    }
}
