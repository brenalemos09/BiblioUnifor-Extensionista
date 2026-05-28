package com.example.bibliounifornew.features.usuario.perfil

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R
import com.example.bibliounifornew.login.TelaRF01BemVindo
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView

class TelaRF09Configuracao : AppCompatActivity() {

    private val galeria = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { processarESubirFoto(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf09_configuracao)

        // ── Mapeamento de views ───────────────────────────────────────────────
        val btnSalvar       = findViewById<MaterialButton>(R.id.buttonSalvarConfig)
        val btnRedefinir    = findViewById<MaterialButton>(R.id.buttonRedefinirSenha)
        val btnApagar       = findViewById<MaterialButton>(R.id.buttonApagarConta)
        val editNome        = findViewById<EditText>(R.id.editNome)
        val editUsuario     = findViewById<EditText>(R.id.editUsuario)
        val editBio         = findViewById<EditText>(R.id.editBio)
        val textEmail       = findViewById<TextView>(R.id.textUsuario)
        val iconEditNome    = findViewById<ImageView>(R.id.iconEditNome)
        val iconEditUsuario = findViewById<ImageView>(R.id.iconEditUsuario)
        val iconEditBio     = findViewById<ImageView>(R.id.iconEditBio)
        val imagePerfil     = findViewById<ShapeableImageView>(R.id.imagePerfilUsuario)

        // Estado inicial
        editNome.isEnabled    = false
        editUsuario.isEnabled = false
        editBio.isEnabled     = false

        // ── 1. Carregar dados Mockados ────────────────────────────────────────
        textEmail?.text = "usuario@bibliounifor.com"
        editNome.setText("Usuário Protótipo")
        editUsuario.setText("@usuario_unifor")
        editBio.setText("Estudante da Unifor testando o protótipo BiblioUnifor.")
        imagePerfil?.setImageResource(R.drawable.user_placeholder)

        // ── 2. Lógica de edição ─────────────────────
        configurarIconeEdicao(editNome, iconEditNome)
        configurarIconeEdicao(editUsuario, iconEditUsuario)
        configurarIconeEdicao(editBio, iconEditBio)

        // ── 3. Botão Salvar ───────────────────────────────────────────────────
        btnSalvar.setOnClickListener {
            btnSalvar.isEnabled = false
            btnSalvar.text      = "Salvando..."
            
            btnSalvar.postDelayed({
                btnSalvar.isEnabled = true
                btnSalvar.text      = "Salvar Alterações"
                editNome.isEnabled    = false
                editUsuario.isEnabled = false
                editBio.isEnabled     = false
                Toast.makeText(this, "Perfil salvo com sucesso!", Toast.LENGTH_SHORT).show()
            }, 1000)
        }

        // ── 4. Redefinir senha ─────────────────────────────────────────
        btnRedefinir.setOnClickListener {
            startActivity(Intent(this, TelaRF10RedefinirSenha::class.java))
        }

        // ── 5. Apagar conta ───────────────────────────────────────────────────
        btnApagar.setOnClickListener { exibirPopupApagarConta() }

        // ── 6. Clique na foto de perfil ───────────────────────────────────────
        imagePerfil?.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Foto de Perfil")
                .setMessage("Deseja mudar sua foto?")
                .setPositiveButton("Escolher da Galeria") { _, _ ->
                    galeria.launch("image/*")
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    private fun processarESubirFoto(uri: Uri) {
        val imagePerfil = findViewById<ShapeableImageView>(R.id.imagePerfilUsuario)
        try {
            imagePerfil?.setImageURI(uri)
            Toast.makeText(this, "Foto atualizada (Local)!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao carregar imagem.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun configurarIconeEdicao(editText: EditText, icone: ImageView) {
        icone.setOnClickListener {
            editText.isEnabled = true
            editText.requestFocus()
            editText.setSelection(editText.text.length)
            val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.showSoftInput(editText, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun exibirPopupApagarConta() {
        val dialogView = layoutInflater.inflate(R.layout.popup_apagar_conta, null)
        val builder    = AlertDialog.Builder(this).setView(dialogView)
        val dialog     = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnConfirmar = dialogView.findViewById<MaterialButton>(R.id.buttonConfirmarApagarConta)
        val btnCancelar  = dialogView.findViewById<TextView>(R.id.textCancelarApagarConta)
        val editSenha    = dialogView.findViewById<EditText>(R.id.editSenhaPopup)
        val iconOlho     = dialogView.findViewById<ImageView>(R.id.iconOlhoSenhaPopup)

        var senhaVisivelPopup = false
        iconOlho.setOnClickListener {
            senhaVisivelPopup = !senhaVisivelPopup
            editSenha.transformationMethod = if (senhaVisivelPopup) HideReturnsTransformationMethod.getInstance() else PasswordTransformationMethod.getInstance()
            iconOlho.setImageResource(if (senhaVisivelPopup) R.drawable.ic_eye_open else R.drawable.ic_eye_closed)
            editSenha.setSelection(editSenha.text.length)
        }

        btnConfirmar.setOnClickListener {
            val intent = Intent(this, TelaRF01BemVindo::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        btnCancelar.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
}
