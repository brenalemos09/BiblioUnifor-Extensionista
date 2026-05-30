package com.example.bibliounifornew.features.adm.gerenciamento

import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import coil.load
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.UsuarioRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class TelaRF38ConfigADM : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()
    private val usuarioRepository = UsuarioRepository()

    // Launcher de galeria
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
        val btnApagarConta   = findViewById<MaterialButton>(R.id.btnApagarConta)

        // ── Carrega dados do perfil (RF36.1–RF36.4) ──────────────────────────
        // Tenta "administradores" primeiro (ADMs criados após o fix RF36).
        // Fallback para "usuarios" — ADMs criados antes do fix só têm doc em "usuarios".
        val uid = auth.currentUser?.uid
        if (uid != null) {
            db.collection("administradores").document(uid).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        preencherPerfilAdm(doc, editNome, editUsuario, imageFoto)
                    } else {
                        // Fallback: ADM criado antes do fix RF36 (só tem doc em "usuarios")
                        db.collection("usuarios").document(uid).get()
                            .addOnSuccessListener { docUsuario ->
                                if (docUsuario.exists()) {
                                    preencherPerfilAdm(docUsuario, editNome, editUsuario, imageFoto)
                                }
                            }
                    }
                }
        }

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

        btnSalvar?.setOnClickListener {
            val novoNome    = editNome.text.toString().trim()
            val novoUsuario = editUsuario.text.toString().trim()

            if (novoNome.isEmpty() || novoUsuario.isEmpty()) {
                Toast.makeText(this, getString(R.string.erro_preencha_nome_usuario), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (uid == null) {
                Toast.makeText(this, getString(R.string.erro_sessao_expirada), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnSalvar.isEnabled = false
            // Grava em ambas as coleções para manter consistência após o fix RF36
            val campos = mapOf("nome" to novoNome, "usuario" to novoUsuario)
            val batch = db.batch()
            batch.set(db.collection("administradores").document(uid), campos, SetOptions.merge())
            batch.set(db.collection("usuarios").document(uid), campos, SetOptions.merge())
            batch.commit()
                .addOnSuccessListener {
                    btnSalvar.isEnabled = true
                    Toast.makeText(this, getString(R.string.msg_alteracoes_salvas), Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    btnSalvar.isEnabled = true
                    Toast.makeText(this, getString(R.string.erro_salvar_perfil), Toast.LENGTH_SHORT).show()
                }
        }

        btnRedefinirSenha?.setOnClickListener {
            startActivity(Intent(this, TelaRF39RedefinirADMInterno::class.java))
        }

        btnApagarConta?.setOnClickListener {
            exibirPopupApagarConta(uid)
        }
    }

    private fun preencherPerfilAdm(
        doc        : com.google.firebase.firestore.DocumentSnapshot,
        editNome   : android.widget.EditText,
        editUsuario: android.widget.EditText,
        imageFoto  : ImageView?
    ) {
        editNome.setText(doc.getString("nome")    ?: "")
        editUsuario.setText(doc.getString("usuario") ?: "")
        val fotoUrl = doc.getString("fotoUrl") ?: ""
        if (fotoUrl.isNotEmpty()) {
            imageFoto?.load(fotoUrl) {
                placeholder(R.drawable.user_placeholder)
                error(R.drawable.user_placeholder)
            }
        }
    }

    private fun processarESubirFoto(uri: Uri) {
        val uid       = auth.currentUser?.uid ?: return
        val imageFoto = findViewById<ImageView>(R.id.imageFotoAdm)
        imageFoto?.alpha = 0.5f

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val inputStream = contentResolver.openInputStream(uri)
                val original    = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                if (original == null) {
                    withContext(Dispatchers.Main) {
                        imageFoto?.alpha = 1.0f
                        Toast.makeText(this@TelaRF38ConfigADM, getString(R.string.erro_processar_imagem), Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                val redimensionado = Bitmap.createScaledBitmap(original, 400, 400, true)
                original.recycle()
                val baos = java.io.ByteArrayOutputStream()
                redimensionado.compress(Bitmap.CompressFormat.JPEG, 80, baos)
                redimensionado.recycle()
                val bytes = baos.toByteArray()

                withContext(Dispatchers.Main) {
                    if (isFinishing || isDestroyed) return@withContext
                    // Upload para Storage e salva URL em "administradores"
                    usuarioRepository.uploadFotoPerfil(uid, bytes, "administradores") { sucesso, url, _ ->
                        if (isFinishing || isDestroyed) return@uploadFotoPerfil
                        imageFoto?.alpha = 1.0f
                        if (sucesso && url != null) {
                            // Sincroniza fotoUrl também em "usuarios" para consistência RBAC
                            db.collection("usuarios").document(uid)
                                .set(mapOf("fotoUrl" to url), SetOptions.merge())
                            imageFoto?.load(url) {
                                placeholder(R.drawable.user_placeholder)
                                error(R.drawable.user_placeholder)
                            }
                            Toast.makeText(this@TelaRF38ConfigADM, getString(R.string.msg_foto_atualizada), Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@TelaRF38ConfigADM, getString(R.string.erro_salvar_perfil), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    imageFoto?.alpha = 1.0f
                    Toast.makeText(this@TelaRF38ConfigADM, getString(R.string.erro_processar_imagem), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun exibirPopupApagarConta(uid: String?) {
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

            val currentUser = auth.currentUser
            val email       = currentUser?.email

            if (currentUser == null || email.isNullOrEmpty()) {
                Toast.makeText(this, getString(R.string.erro_sessao_expirada), Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                return@setOnClickListener
            }

            btnConfirmar.isEnabled = false

            val credencial = EmailAuthProvider.getCredential(email, senha)
            currentUser.reauthenticate(credencial)
                .addOnSuccessListener {
                    val firestoreDelete = if (!uid.isNullOrEmpty()) {
                        db.collection("administradores").document(uid).delete()
                    } else null

                    currentUser.delete()
                        .addOnSuccessListener {
                            firestoreDelete?.addOnFailureListener { }
                            dialog.dismiss()
                            Toast.makeText(this, getString(R.string.msg_conta_apagada_sucesso), Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, com.example.bibliounifornew.login.TelaRF01BemVindo::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        }
                        .addOnFailureListener {
                            btnConfirmar.isEnabled = true
                            Toast.makeText(this, getString(R.string.erro_apagar_conta), Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener {
                    btnConfirmar.isEnabled = true
                    Toast.makeText(this, getString(R.string.erro_senha_incorreta), Toast.LENGTH_SHORT).show()
                }
        }

        dialog.show()

        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(dialog.window?.attributes)
        layoutParams.width  = (resources.displayMetrics.widthPixels * 0.90).toInt()
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        dialog.window?.attributes = layoutParams
    }
}
