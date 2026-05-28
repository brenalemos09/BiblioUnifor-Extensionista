package com.example.bibliounifornew.features.adm.gerenciamento

import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.example.bibliounifornew.R
import com.example.bibliounifornew.login.TelaRF01BemVindo
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class TelaRF38ConfigADM : AppCompatActivity() {

    // Launcher de galeria — registrado antes de onCreate
    private val galeria = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { processarESubirFoto(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf38_config_adm)

        val editNome    = findViewById<EditText>(R.id.editNomeAdm)
        val editUsuario = findViewById<EditText>(R.id.editUsuarioAdm)
        val imageFoto   = findViewById<ImageView>(R.id.imageFotoAdm)

        val btnSalvar        = findViewById<MaterialButton>(R.id.btnSalvarAlteracoes)
        val btnRedefinirSenha = findViewById<MaterialButton>(R.id.btnRedefinirSenha)
        val btnSairSessao     = findViewById<MaterialButton>(R.id.btnSairSessao)
        val btnApagarConta   = findViewById<MaterialButton>(R.id.btnApagarConta)

        // ── Olho para campo senha (decorativo, desabilitado) ──────────────────
        val editSenhaAtual = findViewById<EditText>(R.id.editSenhaAtual)
        val iconOlhoSenha  = findViewById<ImageView>(R.id.iconOlhoSenhaAtual)
        editSenhaAtual.isEnabled = false

        var senhaVisivel = false
        iconOlhoSenha.setOnClickListener {
            senhaVisivel = !senhaVisivel
            if (senhaVisivel) {
                editSenhaAtual.transformationMethod = HideReturnsTransformationMethod.getInstance()
                iconOlhoSenha.setImageResource(R.drawable.ic_eye_open)
            } else {
                editSenhaAtual.transformationMethod = PasswordTransformationMethod.getInstance()
                iconOlhoSenha.setImageResource(R.drawable.ic_eye_closed)
            }
            editSenhaAtual.setSelection(editSenhaAtual.text.length)
        }

        // ── Carrega dados do perfil (MOCK PADRONIZADO) ───────────────────────
        editNome.setText("Administrador Unifor")
        editUsuario.setText("admin@bibliounifor.com")
        imageFoto?.setImageResource(R.drawable.user_placeholder)

        // ── Clique na foto de perfil ──────────────────────────────────────────
        imageFoto?.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.alert_foto_titulo))
                .setMessage(getString(R.string.alert_foto_mensagem))
                .setPositiveButton(getString(R.string.alert_foto_galeria)) { _, _ ->
                    galeria.launch("image/*")
                }
                .setNegativeButton(getString(R.string.btn_cancelar), null)
                .show()
        }

        // ── Salvar alterações de perfil ───────────────────────────────────────
        btnSalvar?.setOnClickListener {
            val novoNome    = editNome.text.toString().trim()
            val novoUsuario = editUsuario.text.toString().trim()

            if (novoNome.isEmpty() || novoUsuario.isEmpty()) {
                Toast.makeText(this, getString(R.string.erro_preencha_nome_usuario), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnSalvar.isEnabled = false
            btnSalvar.postDelayed({
                btnSalvar.isEnabled = true
                Toast.makeText(this, getString(R.string.msg_alteracoes_salvas), Toast.LENGTH_SHORT).show()
            }, 500)
        }

        // ── Redefinir senha: abre tela interna ───────────────────────────────
        btnRedefinirSenha?.setOnClickListener {
            startActivity(Intent(this, TelaRF39RedefinirADMInterno::class.java))
        }

        // ── Logout ────────────────────────────────────────────────────────────
        btnSairSessao?.setOnClickListener {
            val intent = Intent(this, TelaRF01BemVindo::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // ── Apagar conta ──────────────────────────────────────────────────────
        btnApagarConta?.setOnClickListener {
            exibirPopupApagarConta()
        }
    }

    // ─── UPLOAD DE FOTO (MOCK) ────────────────────────────────────────────────

    private fun processarESubirFoto(uri: Uri) {
        val imageFoto = findViewById<ImageView>(R.id.imageFotoAdm)
        try {
            imageFoto?.alpha = 0.5f
            imageFoto?.postDelayed({
                imageFoto?.alpha = 1.0f
                imageFoto?.setImageURI(uri)
                Toast.makeText(this, getString(R.string.msg_foto_atualizada), Toast.LENGTH_SHORT).show()
            }, 1000)
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.erro_processar_imagem), Toast.LENGTH_SHORT).show()
        }
    }

    // ─── POPUP APAGAR CONTA (MOCK) ──────────────────────────────────────────

    private fun exibirPopupApagarConta() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_apagar_conta_adm)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnConfirmar = dialog.findViewById<MaterialButton>(R.id.buttonConfirmarApagarContaADM)
        val btnCancelar  = dialog.findViewById<MaterialButton>(R.id.buttonCancelarApagarContaADM)
        val editSenha    = dialog.findViewById<TextInputEditText>(R.id.editSenhaApagarContaADM)

        btnCancelar.setOnClickListener { dialog.dismiss() }

        btnConfirmar.setOnClickListener {
            val senha = editSenha.text.toString()
            if (senha.isEmpty()) {
                Toast.makeText(this, getString(R.string.erro_senha_vazia_apagar), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnConfirmar.isEnabled = false
            btnConfirmar.postDelayed({
                dialog.dismiss()
                Toast.makeText(this, getString(R.string.msg_conta_apagada_sucesso), Toast.LENGTH_SHORT).show()
                val intent = Intent(this, com.example.bibliounifornew.login.TelaRF01BemVindo::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }, 1000)
        }

        dialog.show()

        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(dialog.window?.attributes)
        layoutParams.width  = (resources.displayMetrics.widthPixels * 0.90).toInt()
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        dialog.window?.attributes = layoutParams
    }
}
