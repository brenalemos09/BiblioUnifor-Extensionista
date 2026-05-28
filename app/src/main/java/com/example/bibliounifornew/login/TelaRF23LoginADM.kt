package com.example.bibliounifornew.login

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R
import com.example.bibliounifornew.features.adm.dashboard.TelaRF28DashboardADM

class TelaRF23LoginADM : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf23_login_adm)

        val imageLogo = findViewById<ImageView>(R.id.imageLogoAdm)
        carregarLogoSegura(imageLogo)

        // ─── CAMPOS ──────────────────────────────────────────────────────────
        val editEmail      = findViewById<EditText>(R.id.editEmailAdm)
        val editSenha      = findViewById<EditText>(R.id.editSenhaAdm)
        val editCredencial = findViewById<EditText>(R.id.editCredencialAdm)
        val erro           = findViewById<TextView>(R.id.textErroAdm)
        val criarConta     = findViewById<TextView>(R.id.textCriarContaAdm)
        val esqueceuSenha  = findViewById<TextView>(R.id.textEsqueceuSenhaAdm)
        val botaoEntrar    = findViewById<Button>(R.id.buttonEntrarAdm)
        val btnOlho        = findViewById<ImageView>(R.id.iconOlhoSenhaAdm)

        // UX: limpa erro ao focar OU ao digitar em qualquer campo
        val limparErro = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                erro.visibility = View.GONE
            }
            override fun afterTextChanged(s: Editable?) {}
        }
        listOf(editEmail, editSenha, editCredencial).forEach { campo ->
            campo.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) erro.visibility = View.GONE }
            campo.addTextChangedListener(limparErro)
        }

        // ─── MOSTRAR/OCULTAR SENHA ────────────────────────────────────────────
        var senhaVisivel = false
        btnOlho.setOnClickListener {
            senhaVisivel = !senhaVisivel
            editSenha.transformationMethod = if (senhaVisivel)
                HideReturnsTransformationMethod.getInstance()
            else
                PasswordTransformationMethod.getInstance()
            btnOlho.setImageResource(if (senhaVisivel) R.drawable.ic_eye_open else R.drawable.ic_eye_closed)
            editSenha.setSelection(editSenha.text.length)
        }

        // ─── LOGIN SIMULADO (PROTÓTIPO LOCAL COM CREDENCIAIS MOCKADAS) ───────
        botaoEntrar.setOnClickListener {
            val sEmail      = editEmail.text.toString().trim()
            val sSenha      = editSenha.text.toString()
            val sCredencial = editCredencial.text.toString().trim()

            if (sEmail == "admin@bibliounifor.com" && sSenha == "Admin123" && sCredencial == "DevsAB") {
                Toast.makeText(this, "Acesso administrativo concedido!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, TelaRF28DashboardADM::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Credenciais administrativas inválidas", Toast.LENGTH_SHORT).show()
                erro.text = "Credenciais administrativas inválidas"
                erro.visibility = View.VISIBLE
            }
        }

        // ─── NAVEGAÇÃO ────────────────────────────────────────────────────────
        criarConta.setOnClickListener {
            startActivity(Intent(this, TelaRF26NovaContaADM::class.java))
        }

        esqueceuSenha.setOnClickListener {
            startActivity(Intent(this, TelaRF24RecuperacaoSenhaADM::class.java))
        }
    }

    private fun carregarLogoSegura(imageView: ImageView) {
        try {
            val options = BitmapFactory.Options().apply {
                inSampleSize = 4
                inJustDecodeBounds = false
            }
            val bitmap = BitmapFactory.decodeResource(resources, R.drawable.unifor_marca, options)
            imageView.setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}