package com.example.bibliounifornew.login

import android.app.Dialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bibliounifornew.data.AppDatabase
import com.example.bibliounifornew.data.Usuario
import com.example.bibliounifornew.R
import kotlinx.coroutines.launch

class TelaRF04CadastroNovoUsuario : AppCompatActivity() {

    private var senhaVisivel = false
    private var confirmarSenhaVisivel = false

    // ANDERSON: Injeção do Banco de Dados mantida da sua versão
    private val db by lazy { AppDatabase.getDatabase(this@TelaRF04CadastroNovoUsuario) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf04_cadastrar_novo_usuario)

        //-----------------------------------
        // COMPONENTES (Versão da Parceira)
        //-----------------------------------
        val imageLogo = findViewById<ImageView>(R.id.imageLogoCadastro)
        carregarLogoSegura(imageLogo)

        val nome = findViewById<EditText>(R.id.editTextNome)
        val usuario = findViewById<EditText>(R.id.editTextUsuario)
        val email = findViewById<EditText>(R.id.editTextEmail)
        val senha = findViewById<EditText>(R.id.editTextSenha)
        val confirmaSenha = findViewById<EditText>(R.id.editTextConfirmaSenha)

        val erroEmail = findViewById<TextView>(R.id.tvErroEmail)
        val erroSenha = findViewById<TextView>(R.id.tvErroSenha)
        val btnCriar = findViewById<Button>(R.id.btnCriar)
        val olhoSenha = findViewById<ImageView>(R.id.iconOlhoSenha)
        val olhoConfirmar = findViewById<ImageView>(R.id.iconOlhoConfirmarSenha)

        //-----------------------------------
        // ENTRE AQUI
        //-----------------------------------
        findViewById<TextView>(R.id.textEntreAqui).setOnClickListener {
            startActivity(Intent(this, TelaRF03LoginAluno::class.java))
            finish()
        }

        //-----------------------------------
        // OLHO SENHA
        //-----------------------------------
        olhoSenha.setOnClickListener {
            senhaVisivel = !senhaVisivel
            if (senhaVisivel) {
                senha.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                olhoSenha.setImageResource(R.drawable.ic_eye_open) // Mantendo seus ícones
            } else {
                senha.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                olhoSenha.setImageResource(R.drawable.ic_eye_closed)
            }
            senha.setSelection(senha.text.length)
        }

        //-----------------------------------
        // OLHO CONFIRMAR SENHA
        //-----------------------------------
        olhoConfirmar.setOnClickListener {
            confirmarSenhaVisivel = !confirmarSenhaVisivel
            if (confirmarSenhaVisivel) {
                confirmaSenha.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                olhoConfirmar.setImageResource(R.drawable.ic_eye_open)
            } else {
                confirmaSenha.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                olhoConfirmar.setImageResource(R.drawable.ic_eye_closed)
            }
            confirmaSenha.setSelection(confirmaSenha.text.length)
        }

        //-----------------------------------
        // BOTÃO CRIAR (Fusão de UI + Banco de Dados)
        //-----------------------------------
        btnCriar.setOnClickListener {
            erroEmail.visibility = View.GONE
            erroSenha.visibility = View.GONE

            val nomeTexto = nome.text.toString().trim()
            val usuarioTexto = usuario.text.toString().trim()
            val emailTexto = email.text.toString().trim()
            val senhaTexto = senha.text.toString()
            val confirmaTexto = confirmaSenha.text.toString()

            // 1. Validação de Campos Vazios
            if (nomeTexto.isEmpty() || usuarioTexto.isEmpty() || emailTexto.isEmpty() || senhaTexto.isEmpty() || confirmaTexto.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 2. Validação de Email
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailTexto).matches()) {
                erroEmail.text = "E-mail inválido"
                erroEmail.visibility = View.VISIBLE
                return@setOnClickListener
            }

            // 3. Validação de Senha Forte
            val senhaValida = senhaTexto.length >= 8 && senhaTexto.any { it.isDigit() } && senhaTexto.any { it.isUpperCase() }
            if (!senhaValida) {
                erroSenha.text = "Senha deve ter 8+ letras, 1 número e 1 maiúscula"
                erroSenha.visibility = View.VISIBLE
                return@setOnClickListener
            }

            // 4. Validação de Senhas Diferentes
            if (senhaTexto != confirmaTexto) {
                erroSenha.text = "As senhas não coincidem"
                erroSenha.visibility = View.VISIBLE
                return@setOnClickListener
            }

            // 5. ANDERSON: Salvando no Banco de Dados (Sua lógica estruturada)
            lifecycleScope.launch {
                val usuarioExistente = db.usuarioDao().buscarPorEmail(emailTexto)
                if (usuarioExistente != null) {
                    erroEmail.text = "E-mail já cadastrado"
                    erroEmail.visibility = View.VISIBLE
                } else {
                    // Sem o parâmetro 'senha' como combinamos antes, focando no Firebase Auth depois
                    db.usuarioDao().inserir(
                        Usuario(
                            uid = "",
                            nome = nomeTexto,
                            usuario = usuarioTexto,
                            email = emailTexto
                        )
                    )
                    mostrarPopupSucesso()
                }
            }
        }
    }

    //-----------------------------------
    // POPUP SUCESSO (Extraído para função para manter limpeza)
    //-----------------------------------
    private fun mostrarPopupSucesso() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.popup_sucesso_cadastro)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(false)

        val btnRetornar = dialog.findViewById<Button>(R.id.btnRetornarLogin)
        btnRetornar.setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(this, TelaRF03LoginAluno::class.java))
            finish()
        }
        dialog.show()
    }

    //-----------------------------------
    // LOGO (Função da Parceira mantida)
    //-----------------------------------
    private fun carregarLogoSegura(imageView: ImageView) {
        try {
            val options = BitmapFactory.Options().apply { inSampleSize = 4 }
            val bitmap = BitmapFactory.decodeResource(resources, R.drawable.unifor_marca, options)
            imageView.setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}