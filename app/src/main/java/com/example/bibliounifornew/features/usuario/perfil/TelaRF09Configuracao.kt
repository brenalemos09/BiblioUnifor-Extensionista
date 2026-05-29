package com.example.bibliounifornew.features.usuario.perfil

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.AuthRepository
import com.example.bibliounifornew.data.UsuarioRepository
import com.example.bibliounifornew.login.TelaRF01BemVindo
import com.example.bibliounifornew.login.TelaRF03LoginAluno
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage

class TelaRF09Configuracao : AppCompatActivity() {

    private val authRepository    = AuthRepository()
    private val usuarioRepository = UsuarioRepository()
    private val db                = FirebaseFirestore.getInstance()
    private var usuarioAtual      : FirebaseUser? = null

    // Listener em tempo real — cancelado em onDestroy para evitar memory leak
    private var snapshotListener  : ListenerRegistration? = null

    // PickVisualMedia: API moderna — sem permissão READ_EXTERNAL_STORAGE em Android 13+
    private val galeria = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        uri?.let { processarESubirFoto(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf09_configuracao)

        usuarioAtual = authRepository.getUsuarioAtual()

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

        // Estado inicial: campos bloqueados até o lápis ser pressionado
        editNome.isEnabled    = false
        editUsuario.isEnabled = false
        editBio.isEnabled     = false

        // ── 1. Carregar dados via Snapshot Listener ───────────────────────────
        if (usuarioAtual != null) {
            textEmail?.text = usuarioAtual!!.email

            snapshotListener = usuarioRepository.observarPerfilUsuario(usuarioAtual!!.uid) { dados ->
                if (dados != null) {
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
                    // Carrega foto de perfil se disponível
                    val fotoUrl = dados["fotoUrl"] as? String ?: ""
                    if (fotoUrl.isNotEmpty()) {
                        imagePerfil?.load(fotoUrl) {
                            placeholder(R.drawable.user_placeholder)
                            error(R.drawable.user_placeholder)
                        }
                    }
                }
            }
        } else {
            startActivity(Intent(this, TelaRF03LoginAluno::class.java))
            finish()
            return
        }

        // ── 2. Lógica de edição (lápis destrava o campo) ─────────────────────
        configurarIconeEdicao(editNome, iconEditNome)
        configurarIconeEdicao(editUsuario, iconEditUsuario)
        configurarIconeEdicao(editBio, iconEditBio)

        // ── 3. Botão Salvar ───────────────────────────────────────────────────
        btnSalvar.setOnClickListener {
            val uid = usuarioAtual?.uid ?: return@setOnClickListener
            val campos = mapOf(
                "nome"      to editNome.text.toString().trim(),
                "usuario"   to editUsuario.text.toString().trim(),
                "biografia" to editBio.text.toString().trim()
            )
            btnSalvar.isEnabled = false
            btnSalvar.text      = "Salvando..."
            usuarioRepository.salvarPerfilCompleto(uid, campos) { sucesso, erro ->
                btnSalvar.isEnabled = true
                btnSalvar.text      = "Salvar Alterações"
                if (sucesso) {
                    editNome.isEnabled    = false
                    editUsuario.isEnabled = false
                    editBio.isEnabled     = false
                    Toast.makeText(this, "Perfil salvo com sucesso!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Erro ao salvar: $erro", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // ── 4. Redefinir senha → RF10 ─────────────────────────────────────────
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
                    galeria.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    override fun onResume() {
        super.onResume()
        // Força sincronização ao voltar para a tela (Ex: se mudou foto no Dashboard)
        usuarioAtual?.uid?.let { uid ->
            usuarioRepository.buscarPerfilUsuario(uid) { sucesso, dados, _ ->
                if (sucesso && dados != null) {
                    val imagePerfil = findViewById<ShapeableImageView>(R.id.imagePerfilUsuario)
                    val fotoUrl = dados["fotoUrl"] as? String ?: ""
                    if (fotoUrl.isNotEmpty()) {
                        imagePerfil?.load(fotoUrl) {
                            placeholder(R.drawable.user_placeholder)
                            error(R.drawable.user_placeholder)
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        snapshotListener?.remove()
    }

    // ─── UPLOAD DE FOTO ───────────────────────────────────────────────────────

    /**
     * Abre uma InputStream segura a partir do URI selecionado na galeria e faz
     * o upload para o Firebase Storage via [storageRef.putStream].
     *
     * Ao contrário de putFile(uri), que falha com "Object does not exist at location"
     * quando a URI do picker já foi revogada pelo sistema (comportamento documentado
     * em dispositivos Android 10+), contentResolver.openInputStream(imageUri) mantém
     * o acesso ao arquivo enquanto o stream não é fechado.
     *
     * Fluxo:
     *   1. Abre stream → verifica null antes de qualquer operação
     *   2. putStream(stream) → upload para Storage
     *   3. addOnSuccessListener → fecha stream + busca downloadUrl
     *   4. Persiste fotoUrl no Firestore em usuarios/{uid}
     *   5. Atualiza ImageView local via Coil
     */
    private fun processarESubirFoto(imageUri: Uri) {
        val uid = usuarioAtual?.uid
        if (uid.isNullOrBlank()) {
            // UID vazio antes mesmo de tocar no Storage → sessão expirada
            Toast.makeText(this, getString(R.string.erro_sessao_expirada), Toast.LENGTH_SHORT).show()
            return
        }

        val imagePerfil = findViewById<ShapeableImageView>(R.id.imagePerfilUsuario)

        // Hierarquia limpa: usuarios/{uid}/perfil.jpg
        // Separar por .child() evita problemas de encoding em UIDs com caracteres especiais.
        val storageRef = FirebaseStorage.getInstance()
            .reference
            .child("usuarios")
            .child(uid)
            .child("perfil.jpg")

        // Log para inspecionar o caminho exato montado — remover antes de produção
        Log.d("UPLOAD_DEBUG", "UID: $uid")
        Log.d("UPLOAD_DEBUG", "Caminho do Storage: ${storageRef.path}")

        try {
            val stream = contentResolver.openInputStream(imageUri)
            if (stream == null) {
                Log.e("UPLOAD_DEBUG", "openInputStream retornou null para URI: $imageUri")
                Toast.makeText(this, getString(R.string.erro_processar_imagem), Toast.LENGTH_SHORT).show()
                return
            }

            imagePerfil?.alpha = 0.5f

            storageRef.putStream(stream)
                .addOnSuccessListener {
                    stream.close()
                    Log.d("UPLOAD_DEBUG", "putStream concluído. Buscando downloadUrl...")

                    storageRef.downloadUrl
                        .addOnSuccessListener { downloadUri ->
                            if (isFinishing || isDestroyed) return@addOnSuccessListener
                            val url = downloadUri?.toString()
                            if (url.isNullOrBlank()) {
                                imagePerfil?.alpha = 1.0f
                                Toast.makeText(this, getString(R.string.erro_processar_imagem), Toast.LENGTH_SHORT).show()
                                return@addOnSuccessListener
                            }
                            Log.d("UPLOAD_DEBUG", "downloadUrl obtida: $url")

                            // Persiste fotoUrl no documento do usuário
                            db.collection("usuarios").document(uid)
                                .set(mapOf("fotoUrl" to url), SetOptions.merge())
                                .addOnSuccessListener {
                                    if (isFinishing || isDestroyed) return@addOnSuccessListener
                                    imagePerfil?.alpha = 1.0f
                                    imagePerfil?.load(url) {
                                        crossfade(true)
                                        placeholder(R.drawable.user_placeholder)
                                        error(R.drawable.user_placeholder)
                                    }
                                    Toast.makeText(this, getString(R.string.msg_foto_atualizada), Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { e ->
                                    imagePerfil?.alpha = 1.0f
                                    Log.e("UPLOAD_DEBUG", "Falha ao salvar fotoUrl no Firestore: ${e.message}")
                                    Toast.makeText(this, getString(R.string.fmt_erro_ao_salvar, e.message), Toast.LENGTH_SHORT).show()
                                }
                        }
                        .addOnFailureListener { e ->
                            imagePerfil?.alpha = 1.0f
                            // Causa real do "Object does not exist": arquivo não chegou ao bucket
                            // ou as Security Rules do Storage bloquearam a gravação.
                            Log.e("UPLOAD_DEBUG", "downloadUrl falhou [${e.javaClass.simpleName}]: ${e.message}")
                            Toast.makeText(
                                this,
                                "Erro ao obter URL [${e.javaClass.simpleName}]: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                }
                .addOnFailureListener { e ->
                    stream.close()
                    imagePerfil?.alpha = 1.0f
                    // Exibe o tipo da exceção para distinguir SecurityException (Rules)
                    // de IOException (rede) ou StorageException (bucket inexistente)
                    Log.e("UPLOAD_DEBUG", "putStream falhou [${e.javaClass.simpleName}]: ${e.message}")
                    Toast.makeText(
                        this,
                        "Erro ao enviar foto [${e.javaClass.simpleName}]: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }

        } catch (e: Exception) {
            imagePerfil?.alpha = 1.0f
            Log.e("UPLOAD_DEBUG", "Exceção inesperada no upload [${e.javaClass.simpleName}]: ${e.message}")
            Toast.makeText(this, getString(R.string.erro_processar_imagem), Toast.LENGTH_SHORT).show()
        }
    }

    // ─── HELPERS ──────────────────────────────────────────────────────────────

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
            if (senhaVisivelPopup) {
                editSenha.transformationMethod = HideReturnsTransformationMethod.getInstance()
                iconOlho.setImageResource(R.drawable.ic_eye_open)
            } else {
                editSenha.transformationMethod = PasswordTransformationMethod.getInstance()
                iconOlho.setImageResource(R.drawable.ic_eye_closed)
            }
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
