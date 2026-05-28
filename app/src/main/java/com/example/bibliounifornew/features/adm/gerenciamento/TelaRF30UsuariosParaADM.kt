package com.example.bibliounifornew.features.adm.gerenciamento

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.example.bibliounifornew.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class TelaRF30UsuariosParaADM : AppCompatActivity() {

    private var usuarioId    : String = ""
    private var usuarioNome  : String = ""
    private var usuarioEmail : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf30_usuariosparaadm)

        usuarioId    = intent.getStringExtra("USUARIO_ID")    ?: ""
        usuarioNome  = intent.getStringExtra("USUARIO_NOME")  ?: "Usuário"
        usuarioEmail = intent.getStringExtra("USUARIO_EMAIL") ?: ""

        val textNome  = findViewById<TextView>(R.id.textNomeUsuario)
        val textEmail = findViewById<TextView>(R.id.textEmailUsuario)
        val textTipo  = findViewById<TextView>(R.id.textTipoUsuario)

        textNome?.text  = usuarioNome
        textEmail?.text = usuarioEmail

        textTipo?.text = "ALUNO"

        findViewById<MaterialButton>(R.id.buttonSolicitacoes)?.setOnClickListener {
            exibirPopupSolicitacoes()
        }
        findViewById<MaterialButton>(R.id.buttonLivrosAlugados)?.setOnClickListener {
            val i = Intent(this, TelaRFAdmUsuarioAlugados::class.java)
            i.putExtra("USUARIO_ID",   usuarioId)
            i.putExtra("USUARIO_NOME", usuarioNome)
            startActivity(i)
        }
        findViewById<MaterialButton>(R.id.buttonAtrasos)?.setOnClickListener {
            exibirPopupAtraso()
        }
        findViewById<MaterialButton>(R.id.buttonPermissao)?.setOnClickListener {
            exibirPopupPermissao(textTipo)
        }
        findViewById<MaterialButton>(R.id.buttonExcluirConta)?.setOnClickListener {
            exibirPopupExcluirConta()
        }
    }

    private fun exibirPopupSolicitacoes() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_solicitacoes_usuario_adm)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val txtNome   = dialog.findViewById<TextView>(R.id.textPopupNomeUsuario)
        val txtLista  = dialog.findViewById<TextView>(R.id.textPopupListaSolicitacoes)
        val txtStatus = dialog.findViewById<TextView>(R.id.textPopupStatus)
        val cardLivro = dialog.findViewById<MaterialCardView>(R.id.cardSolicitacaoLivro)
        val txtTitulo = dialog.findViewById<TextView>(R.id.textTituloLivroSolicitado)
        val txtAutor  = dialog.findViewById<TextView>(R.id.textAutorLivroSolicitado)
        val txtData   = dialog.findViewById<TextView>(R.id.textDataLivroSolicitado)
        val imgCapa   = dialog.findViewById<ImageView>(R.id.imageLivroSolicitado)

        txtNome?.text  = getString(R.string.popup_solicitacoes_label_usuario, usuarioNome.uppercase())
        txtLista?.text  = "Total: 1 solicitações (EMPRÉSTIMO)"
        txtStatus?.text = "Status: PENDENTE"
        
        cardLivro?.visibility = View.VISIBLE
        txtTitulo?.text = "O Senhor dos Anéis"
        txtAutor?.text  = "J.R.R. Tolkien"
        txtData?.text   = "Solicitado em: 25/05/2024"
        imgCapa?.setImageResource(R.drawable.osda)

        dialog.findViewById<Button>(R.id.btnFecharSolicitacoes)?.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun exibirPopupAtraso() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_atraso_aluguel_usuario)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val txtMensagem = dialog.findViewById<TextView>(R.id.textNomeLivroAtrasado)
        val txtMulta    = dialog.findViewById<TextView>(R.id.textValorMulta)

        txtMensagem?.text = "O usuário possui 1 aluguel atrasado: 'Dom Casmurro'"
        txtMulta?.text    = "R$ 5,00"

        dialog.findViewById<Button>(R.id.buttonFecharAtraso)?.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun exibirPopupPermissao(textTipo: TextView?) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_mudar_permissao_adm)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val editSenha   = dialog.findViewById<TextInputEditText>(R.id.editSenhaPermissao)
        val btnMudar    = dialog.findViewById<Button>(R.id.buttonMudarPermissao)
        val btnCancelar = dialog.findViewById<TextView>(R.id.textCancelarPermissao)

        btnMudar?.setOnClickListener {
            val senha = editSenha?.text?.toString()?.trim() ?: ""
            if (senha.isEmpty()) {
                Toast.makeText(this, "Digite sua senha", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            textTipo?.text = "ADM"
            Toast.makeText(this, "Permissão alterada com sucesso!", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        btnCancelar?.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun exibirPopupExcluirConta() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_apagar_conta_adm)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val editSenha    = dialog.findViewById<TextInputEditText>(R.id.editSenhaApagarContaADM)
        val btnConfirmar = dialog.findViewById<Button>(R.id.buttonConfirmarApagarContaADM)
        val btnCancelar  = dialog.findViewById<Button>(R.id.buttonCancelarApagarContaADM)

        btnConfirmar?.setOnClickListener {
            val senha = editSenha?.text?.toString()?.trim() ?: ""
            if (senha.isEmpty()) {
                Toast.makeText(this, "Digite sua senha", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(this, "Conta removida com sucesso!", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
            finish()
        }

        btnCancelar?.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
}

