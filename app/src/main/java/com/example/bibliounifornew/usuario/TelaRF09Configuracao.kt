package com.example.bibliounifornew.usuario

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.AuthRepository
import com.example.bibliounifornew.data.UsuarioRepository
import com.example.bibliounifornew.login.TelaRF01BemVindo
import com.example.bibliounifornew.login.TelaRF03LoginAluno
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ListenerRegistration

class TelaRF09Configuracao : AppCompatActivity() {

    private val authRepository = AuthRepository()
    private val usuarioRepository = UsuarioRepository()
    private var usuarioAtual: FirebaseUser? = null

    // Listener em tempo real — cancelado em onDestroy para evitar memory leak
    private var snapshotListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf09_configuracao)

        usuarioAtual = authRepository.getUsuarioAtual()

        // Mapeamento de Views
        val btnSalvar      = findViewById<MaterialButton>(R.id.buttonSalvarConfig)
        val btnRedefinir   = findViewById<MaterialButton>(R.id.buttonRedefinirSenha)
        val btnApagar      = findViewById<MaterialButton>(R.id.buttonApagarConta)
        val editNome       = findViewById<EditText>(R.id.editNome)
        val editUsuario    = findViewById<EditText>(R.id.editUsuario)
        val editBio        = findViewById<EditText>(R.id.editBio)
        val textEmail      = findViewById<TextView>(R.id.textUsuario)
        val iconEditNome   = findViewById<ImageView>(R.id.iconEditNome)
        val iconEditUsuario = findViewById<ImageView>(R.id.iconEditUsuario)
        val iconEditBio    = findViewById<ImageView>(R.id.iconEditBio)

        // Estado inicial: campos desabilitados até o lápis ser pressionado
        editNome.isEnabled    = false
        editUsuario.isEnabled = false
        editBio.isEnabled     = false

        // 1. CARREGAR DADOS VIA SNAPSHOT LISTENER (atualiza em tempo real)
        if (usuarioAtual != null) {
            textEmail?.text = usuarioAtual!!.email

            snapshotListener = usuarioRepository.observarPerfilUsuario(usuarioAtual!!.uid) { dados ->
                if (dados != null) {
                    // Só sobrescreve se o campo NÃO estiver em edição ativa
                    if (!editNome.isEnabled) {
                        editNome.setText(dados["nome"] as? String ?: "")
                    }
                    if (!editUsuario.isEnabled) {
                        editUsuario.setText(dados["usuario"] as? String ?: "")
                    }
                    if (!editBio.isEnabled) {
                        val bio = dados["biografia"] as? String ?: ""
                        editBio.setText(bio.ifEmpty { "Escreva um pouco sobre você..." })
                    }
                }
            }
        } else {
            startActivity(Intent(this, TelaRF03LoginAluno::class.java))
            finish()
            return
        }

        // 2. LÓGICA DE EDIÇÃO (lápis destrava o campo)
        configurarIconeEdicao(editNome, iconEditNome)
        configurarIconeEdicao(editUsuario, iconEditUsuario)
        configurarIconeEdicao(editBio, iconEditBio)

        // 3. BOTÃO SALVAR — salva todos os campos editáveis de uma vez com merge
        btnSalvar.setOnClickListener {
            val uid = usuarioAtual?.uid ?: return@setOnClickListener

            val campos = mapOf(
                "nome"      to editNome.text.toString().trim(),
                "usuario"   to editUsuario.text.toString().trim(),
                "biografia" to editBio.text.toString().trim()
            )

            btnSalvar.isEnabled = false
            btnSalvar.text = "Salvando..."

            usuarioRepository.salvarPerfilCompleto(uid, campos) { sucesso, erro ->
                btnSalvar.isEnabled = true
                btnSalvar.text = "Salvar Alterações"

                if (sucesso) {
                    // Trava todos os campos novamente após salvar
                    editNome.isEnabled    = false
                    editUsuario.isEnabled = false
                    editBio.isEnabled     = false
                    Toast.makeText(this, "Perfil salvo com sucesso!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Erro ao salvar: $erro", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // 4. REDEFINIR SENHA (abre RF10)
        btnRedefinir.setOnClickListener {
            startActivity(Intent(this, TelaRF10RedefinirSenha::class.java))
        }

        // 5. APAGAR CONTA
        btnApagar.setOnClickListener {
            exibirPopupApagarConta()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Crucial: remover o listener para evitar memory leak
        snapshotListener?.remove()
    }

    // Destrava o campo ao clicar no ícone de lápis
    private fun configurarIconeEdicao(editText: EditText, icone: ImageView) {
        icone.setOnClickListener {
            editText.isEnabled = true
            editText.requestFocus()
            editText.setSelection(editText.text.length)
            val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE)
                    as android.view.inputmethod.InputMethodManager
            imm.showSoftInput(editText, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun exibirPopupApagarConta() {
        val dialogView = layoutInflater.inflate(R.layout.popup_apagar_conta, null)
        val builder = AlertDialog.Builder(this).setView(dialogView)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnConfirmar = dialogView.findViewById<MaterialButton>(R.id.buttonConfirmarApagarConta)
        val btnCancelar  = dialogView.findViewById<TextView>(R.id.textCancelarApagarConta)
        val editSenha    = dialogView.findViewById<EditText>(R.id.editSenhaPopup)
        val iconOlho     = dialogView.findViewById<ImageView>(R.id.iconOlhoSenhaPopup)

        var senhaVisivel = false
        iconOlho.setOnClickListener {
            senhaVisivel = !senhaVisivel
            editSenha.inputType = if (senhaVisivel)
                android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            else
                android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            iconOlho.setImageResource(if (senhaVisivel) R.drawable.ic_eye_open else R.drawable.ic_eye_closed)
            editSenha.setSelection(editSenha.text.length)
        }

        btnConfirmar.setOnClickListener {
            authRepository.getUsuarioAtual()?.delete()
            val intent = Intent(this, TelaRF01BemVindo::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        btnCancelar.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
}
