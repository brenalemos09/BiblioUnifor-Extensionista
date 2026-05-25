package com.example.bibliounifornew.usuario

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.CheckBox
import android.widget.ScrollView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TelaRF19SolicitacoesTermosCondicoes : AppCompatActivity() {

    private val db   = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf19_solicitacoes_termos_condicoes)

        // Extras recebidos de RF19Solicitacoes
        val tipoMidia = intent.getStringExtra("TIPO_MIDIA") ?: ""
        val livroId   = intent.getStringExtra("LIVRO_ID")   ?: ""

        val scrollView   = findViewById<ScrollView>(R.id.scrollTermos)
        val checkBox     = findViewById<CheckBox>(R.id.checkTelaAceitarTermos)
        val btnConfirmar = findViewById<Button>(R.id.buttonConfirmarTermosTela)

        // Inicialmente desabilitados até o usuário rolar até o fim
        checkBox.isEnabled    = false
        btnConfirmar.isEnabled = false
        btnConfirmar.alpha     = 0.5f

        // Habilita checkbox ao chegar no final do scroll
        scrollView.viewTreeObserver.addOnScrollChangedListener {
            val view = scrollView.getChildAt(scrollView.childCount - 1)
            val diff = view.bottom - (scrollView.height + scrollView.scrollY)
            if (diff <= 0) checkBox.isEnabled = true
        }

        // Habilita botão quando checkbox marcado
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            btnConfirmar.isEnabled = isChecked
            btnConfirmar.alpha     = if (isChecked) 1.0f else 0.5f
        }

        // Confirmar → grava em solicitacoes_midia e mostra popup de sucesso
        btnConfirmar.setOnClickListener {
            gravarSolicitacaoMidia(tipoMidia, livroId)
        }
    }

    // ─── GRAVA NA COLEÇÃO QUE O ADM (RF31) LÊ ────────────────────────────────

    private fun gravarSolicitacaoMidia(tipoMidia: String, livroId: String) {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrEmpty()) {
            Toast.makeText(this, "Faça login para solicitar.", Toast.LENGTH_SHORT).show()
            return
        }

        val dados = hashMapOf(
            "uidUsuario"       to uid,
            "uidAluno"         to uid,           // campo alternativo lido por RF31
            "idLivro"          to livroId,
            "tipos"            to tipoMidia,
            "status"           to "pendente",
            "dataSolicitacao"  to System.currentTimeMillis()
        )

        db.collection("solicitacoes_midia")
            .add(dados)
            .addOnSuccessListener {
                if (!isFinishing && !isDestroyed) showPopupSucesso()
            }
            .addOnFailureListener { e ->
                if (!isFinishing && !isDestroyed)
                    Toast.makeText(this, "Erro ao registrar solicitação: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // ─── POPUP DE SUCESSO ─────────────────────────────────────────────────────

    private fun showPopupSucesso() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.telarf19_solicitacoes_voltar_biblioteca)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)

        val btnOk = dialog.findViewById<Button>(R.id.buttonPopupOkSolicitacao)
        btnOk.setOnClickListener {
            dialog.dismiss()
            // Volta para a tela do livro limpando o back-stack parcial
            val intent = Intent(this, TelaRF12TelaDoLivro::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }

        dialog.show()
    }
}
