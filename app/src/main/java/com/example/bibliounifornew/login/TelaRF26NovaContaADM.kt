package com.example.bibliounifornew.login

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R

class TelaRF26NovaContaADM : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf26_nova_conta_adm)

        // CAMPOS
        val nome = findViewById<EditText>(R.id.editNomeCompletoAdm)
        val usuario = findViewById<EditText>(R.id.editNomeUsuarioAdm)
        val email = findViewById<EditText>(R.id.editEmailAdmCadastro)
        val credencial = findViewById<EditText>(R.id.editCredencialAdmCadastro)
        val senha = findViewById<EditText>(R.id.editSenhaAdmCadastro)
        val confirma = findViewById<EditText>(R.id.editConfirmarSenhaAdm)

        // ERROS
        val erroEmail = findViewById<TextView>(R.id.textErroEmailAdmCadastro)
        val erroCredencial = findViewById<TextView>(R.id.textErroCredencialAdm)
        val erroSenha = findViewById<TextView>(R.id.textRegrasSenhaAdm)
        val erroSenha1 = findViewById<TextView>(R.id.textErroSenha1)
        val erroSenha2 = findViewById<TextView>(R.id.textErroSenha2)
        val erroSenhaDiferente = findViewById<TextView>(R.id.textErroSenhaDiferente)
        val erroSenhaIgual = findViewById<TextView>(R.id.textErroSenhaIgual)

        // ESTADO INICIAL
        erroEmail.visibility = View.GONE
        erroCredencial.visibility = View.GONE
        erroSenha.visibility = View.GONE
        erroSenha1.visibility = View.GONE
        erroSenha2.visibility = View.GONE
        erroSenhaDiferente.visibility = View.GONE
        erroSenhaIgual.visibility = View.GONE

        // TEXT WATCHER
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                erroEmail.visibility = View.GONE
                erroCredencial.visibility = View.GONE
                erroSenha.visibility = View.GONE
                erroSenha1.visibility = View.GONE
                erroSenha2.visibility = View.GONE
                erroSenhaDiferente.visibility = View.GONE
                erroSenhaIgual.visibility = View.GONE
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        nome.addTextChangedListener(watcher)
        usuario.addTextChangedListener(watcher)
        email.addTextChangedListener(watcher)
        credencial.addTextChangedListener(watcher)
        senha.addTextChangedListener(watcher)
        confirma.addTextChangedListener(watcher)

        // BOTÕES
        val criar = findViewById<Button>(R.id.buttonCriarContaAdm)
        val entrar = findViewById<TextView>(R.id.textEntreAquiAdm)

        // OLHOS (MOSTRAR SENHA)
        val olho1 = findViewById<ImageView>(R.id.iconOlhoSenhaAdmCadastro)
        val olho2 = findViewById<ImageView>(R.id.iconOlhoConfirmarSenhaAdm)

        var v1 = false
        var v2 = false

        olho1.setOnClickListener {
            v1 = !v1
            senha.transformationMethod = if (v1) HideReturnsTransformationMethod.getInstance() else PasswordTransformationMethod.getInstance()
            olho1.setImageResource(if (v1) R.drawable.ic_eye_open else R.drawable.ic_eye_closed)
            senha.setSelection(senha.text.length)
        }

        olho2.setOnClickListener {
            v2 = !v2
            confirma.transformationMethod = if (v2) HideReturnsTransformationMethod.getInstance() else PasswordTransformationMethod.getInstance()
            olho2.setImageResource(if (v2) R.drawable.ic_eye_open else R.drawable.ic_eye_closed)
            confirma.setSelection(confirma.text.length)
        }

        criar.setOnClickListener {
            val sNome = nome.text.toString().trim()
            val sUsuario = usuario.text.toString().trim()
            val sEmail = email.text.toString().trim()
            val sCredencial = credencial.text.toString().trim()
            val sSenha = senha.text.toString()
            val sConfirma = confirma.text.toString()

            when {
                sNome.isEmpty() || sUsuario.isEmpty() || sEmail.isEmpty() || sCredencial.isEmpty() -> {
                    Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                }
                !android.util.Patterns.EMAIL_ADDRESS.matcher(sEmail).matches() -> {
                    erroEmail.visibility = View.VISIBLE
                }
                !sCredencial.equals("DevsAB", ignoreCase = true) -> {
                    erroCredencial.visibility = View.VISIBLE
                }
                sSenha.isEmpty() -> {
                    erroSenha1.text = "Campo obrigatório"; erroSenha1.visibility = View.VISIBLE
                }
                sConfirma.isEmpty() -> {
                    erroSenha2.text = "Campo obrigatório"; erroSenha2.visibility = View.VISIBLE
                }
                sSenha.length < 8 -> {
                    erroSenha.text = "A senha deve conter pelo menos 8 caracteres"; erroSenha.visibility = View.VISIBLE
                }
                sSenha != sConfirma -> {
                    erroSenhaDiferente.visibility = View.VISIBLE
                }
                else -> {
                    popup()
                }
            }
        }

        entrar.setOnClickListener {
            startActivity(Intent(this, TelaRF23LoginADM::class.java))
            finish()
        }
    }

    private fun popup() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.popup_sucesso_cadastro)
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialog.findViewById<Button>(R.id.btnRetornarLogin)?.setOnClickListener {
            startActivity(Intent(this, TelaRF23LoginADM::class.java))
            dialog.dismiss()
            finish()
        }
        dialog.show()
    }
}
