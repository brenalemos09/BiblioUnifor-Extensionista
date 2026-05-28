package com.example.bibliounifornew.login

import android.view.inputmethod.InputMethodManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R
import com.google.android.material.button.MaterialButton

class TelaRF24RecuperacaoSenhaADM : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf24_recuperacao_senha_adm)

        // ─── LOGO ─────────────────────────────────────────────────────────────
        carregarLogoSegura(findViewById(R.id.imageLogoRecuperar))

        // ─── COMPONENTES ──────────────────────────────────────────────────────
        val editEmail = findViewById<EditText>(R.id.editEmailRecuperar)
        val btnEnviar = findViewById<MaterialButton>(R.id.buttonEnviarCodigo)
        val txtErro   = findViewById<TextView>(R.id.textErroEmailRecuperar)
        val txtVoltar = findViewById<TextView>(R.id.textVoltarLogin)

        txtErro.visibility = View.GONE

        // ─── UX: limpa erro ao digitar ou focar ───────────────────────────────
        editEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                txtErro.visibility = View.GONE
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        editEmail.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) txtErro.visibility = View.GONE
        }

        // ─── ENVIAR E-MAIL DE RECUPERAÇÃO ─────────────────────────────────────
        btnEnviar.setOnClickListener {
            val email = editEmail.text.toString().trim()

            // 1) Campo vazio
            if (email.isEmpty()) {
                txtErro.text       = "Informe seu e-mail."
                txtErro.visibility = View.VISIBLE
                return@setOnClickListener
            }

            // 2) Formato inválido
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                txtErro.text       = "Formato de e-mail inválido."
                txtErro.visibility = View.VISIBLE
                return@setOnClickListener
            }

            fecharTeclado()
            btnEnviar.isEnabled = false
            txtErro.visibility  = View.GONE

            // Protótipo: Simulação de sucesso
            Toast.makeText(this, "E-mail de recuperação enviado para $email", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, TelaRF24EmailEnviadoADM::class.java))
            finish()
        }

        // ─── VOLTAR PARA LOGIN ADM ────────────────────────────────────────────
        txtVoltar.setOnClickListener {
            fecharTeclado()
            finish()
        }
    }

    private fun fecharTeclado() {
        val view = currentFocus ?: View(this)
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    // ─── LOGO ─────────────────────────────────────────────────────────────────
    private fun carregarLogoSegura(imageView: ImageView) {
        try {
            val options = BitmapFactory.Options().apply {
                inSampleSize       = 4
                inJustDecodeBounds = false
            }
            val bitmap = BitmapFactory.decodeResource(resources, R.drawable.unifor_marca, options)
            imageView.setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
