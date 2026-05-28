package com.example.bibliounifornew.login

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R
import com.google.android.material.button.MaterialButton

class TelaRF05RedefinirSenhaFake : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rf05_redefinir_senha_fake)

        val editSenha = findViewById<EditText>(R.id.editNovaSenhaFake)
        val iconOlho = findViewById<ImageView>(R.id.iconOlhoNovaSenha)
        val btnSalvar = findViewById<MaterialButton>(R.id.buttonSalvarSenhaFake)
        val textSucesso = findViewById<TextView>(R.id.textSucessoSenhaFake)
        val btnVoltar = findViewById<MaterialButton>(R.id.buttonVoltarLoginFake)

        var senhaVisivel = false
        iconOlho.setOnClickListener {
            senhaVisivel = !senhaVisivel
            if (senhaVisivel) {
                editSenha.transformationMethod = HideReturnsTransformationMethod.getInstance()
                iconOlho.setImageResource(R.drawable.ic_eye_open)
            } else {
                editSenha.transformationMethod = PasswordTransformationMethod.getInstance()
                iconOlho.setImageResource(R.drawable.ic_eye_closed)
            }
            editSenha.setSelection(editSenha.text.length)
        }

        btnSalvar.setOnClickListener {
            val novaSenha = editSenha.text.toString().trim()
            if (novaSenha.isNotEmpty()) {
                // Simulação de sucesso
                textSucesso.visibility = View.VISIBLE
                btnVoltar.visibility = View.VISIBLE
                
                // Desabilitar campos após sucesso
                btnSalvar.isEnabled = false
                editSenha.isEnabled = false
                iconOlho.isEnabled = false
                
                Toast.makeText(this, "Senha alterada com sucesso!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Por favor, digite uma nova senha", Toast.LENGTH_SHORT).show()
            }
        }

        btnVoltar.setOnClickListener {
            val intent = Intent(this, TelaRF03LoginAluno::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}