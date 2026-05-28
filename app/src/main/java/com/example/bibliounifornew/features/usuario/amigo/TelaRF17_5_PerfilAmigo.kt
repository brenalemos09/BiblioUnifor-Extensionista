package com.example.bibliounifornew.features.usuario.amigo

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R
import android.os.Handler
import android.os.Looper
import com.google.android.material.button.MaterialButton

class TelaRF17_5_PerfilAmigo : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf17_5_perfil_amigo)

        val amigoUid  = intent.getStringExtra("AMIGO_UID")  ?: ""
        val amigoNome = intent.getStringExtra("AMIGO_NOME") ?: "Usuário Mock"

        // ─── PRÉ-PREENCHE COM DADOS DO INTENT ─────────────────────────────────
        findViewById<TextView>(R.id.textEmailPerfilAmigo)?.text    = amigoNome
        findViewById<TextView>(R.id.textNomePerfilAmigo)?.text     = amigoNome

        // ─── CARREGA DADOS COMPLETOS MOCK ─────────────────────────────────────
        carregarPerfilAmigoMock(amigoUid)

        // ─── BOTÃO ADICIONAR AMIGO ─────────────────────────────────────────────
        val btnAdicionar = findViewById<MaterialButton>(R.id.btnAdicionarAmigoPerfil)
        btnAdicionar?.setOnClickListener {
            enviarSolicitacaoAmizadeMock(amigoUid, amigoNome)
        }
    }

    private fun carregarPerfilAmigoMock(uid: String) {
        findViewById<TextView>(R.id.textUsuarioPerfilAmigo)?.text  = "@usuario_mock"
        findViewById<TextView>(R.id.textBioPerfilAmigo)?.text      = "Esta é uma biografia simulada para o protótipo."
    }

    private fun enviarSolicitacaoAmizadeMock(uidDestinatario: String, nomeDestinatario: String) {
        val btnAdicionar = findViewById<MaterialButton>(R.id.btnAdicionarAmigoPerfil)
        btnAdicionar?.isEnabled = false
        btnAdicionar?.text = "Enviando..."
        
        Handler(Looper.getMainLooper()).postDelayed({
            Toast.makeText(this, "Solicitação enviada para $nomeDestinatario!", Toast.LENGTH_SHORT).show()
            btnAdicionar?.text = "Solicitação Enviada"
        }, 800)
    }
}
