package com.example.bibliounifornew.usuario

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.AuthRepository
import com.google.firebase.firestore.FirebaseFirestore

class TelaRF14LeituraActivity : AppCompatActivity() {

    private val authRepository = AuthRepository()
    private val db             = FirebaseFirestore.getInstance()
    private var livroIdAtual   : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf14_leitura)

        // Recebe o ID do livro passado via Intent (de TelaRF12TelaDoLivro)
        livroIdAtual = intent.getStringExtra("LIVRO_ID") ?: ""

        // ─── BOTÃO ALUGAR ─────────────────────────────────────────────────────
        // Fluxo: popup de confirmação → grava solicitação_emprestimo → popup de sucesso
        findViewById<Button>(R.id.buttonAlugarLivro).setOnClickListener {
            showPopupAlugar()
        }

        // ─── BOTÃO PROCURAR ───────────────────────────────────────────────────
        // Corrigido: navega para RF11 (Tela de Pesquisa) em vez de RF12
        findViewById<Button>(R.id.buttonProcurarLivro).setOnClickListener {
            startActivity(Intent(this, TelaRF11TelaDePesquisa::class.java))
        }

        // ─── BOTÃO ABRIR PDF ──────────────────────────────────────────────────
        // Intent nativo: abre qualquer leitor de PDF instalado no aparelho
        findViewById<Button>(R.id.buttonAbrirPdfLivro).setOnClickListener {
            abrirPdf()
        }

        // ─── BOTÃO ABRIR AUDIOBOOK ────────────────────────────────────────────
        // Intent nativo: abre qualquer app de áudio/música do aparelho
        findViewById<Button>(R.id.buttonAbrirAudioLivro).setOnClickListener {
            abrirAudiobook()
        }

        // ─── BOTÃO SETOR LOCALIZADO ──────────────────────────────────────────
        // Mantido sem alteração — lógica já estava correta
        findViewById<Button>(R.id.buttonSetorLivro).setOnClickListener {
            showPopupSetor()
        }

        // NOTA: botão "Reservar" foi removido do XML (RF14 revisão)
    }

    // ─── POPUP ALUGAR ─────────────────────────────────────────────────────────

    private fun showPopupAlugar() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_alugar_livro)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.85).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        val btnConfirmar = dialog.findViewById<Button>(R.id.buttonAdicionarLivro)
        val btnCancelar  = dialog.findViewById<TextView>(R.id.textCancelarPopup)

        btnConfirmar.setOnClickListener {
            dialog.dismiss()
            // Após confirmação: grava a solicitação no Firestore e exibe sucesso
            gravarSolicitacaoEmprestimo()
        }
        btnCancelar.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    /**
     * Grava um documento em 'solicitacoes_emprestimo' com status "pendente".
     * Essa coleção será lida futuramente pelas telas do ADM.
     */
    private fun gravarSolicitacaoEmprestimo() {
        val usuarioAtual = authRepository.getUsuarioAtual()
        if (usuarioAtual == null) {
            Toast.makeText(this, "Faça login para alugar.", Toast.LENGTH_SHORT).show()
            return
        }

        val dados = hashMapOf(
            "uidAluno"         to usuarioAtual.uid,
            "idLivro"          to livroIdAtual,
            "status"           to "pendente",
            "dataSolicitacao"  to System.currentTimeMillis()
        )

        db.collection("solicitacoes_emprestimo")
            .add(dados)
            .addOnSuccessListener {
                showPopupSucesso()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao registrar solicitação: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showPopupSucesso() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_livro_adicionado)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.85).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        dialog.findViewById<Button>(R.id.buttonVerMeusLivros).setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(this, TelaRF18StatusAluguel::class.java))
        }

        dialog.show()
    }

    private fun showPopupSetor() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_setor_localizado)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.findViewById<Button>(R.id.buttonVoltarSetor).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    // ─── INTENTS NATIVOS DE MÍDIA ─────────────────────────────────────────────

    /**
     * Abre o PDF com Intent.ACTION_VIEW e MIME "application/pdf".
     * O Android apresenta o chooser com todos os leitores disponíveis no aparelho.
     * Fallback para browser caso nenhum leitor PDF esteja instalado.
     */
    private fun abrirPdf() {
        val pdfUri = Uri.parse("https://www.google.com") // Substituir pela URL real do PDF do livro
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(pdfUri, "application/pdf")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val chooser = Intent.createChooser(intent, "Abrir PDF com...")
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(chooser)
        } else {
            // Fallback: abre no browser se não houver leitor PDF
            startActivity(Intent(Intent.ACTION_VIEW, pdfUri))
        }
    }

    /**
     * Abre o audiobook com Intent.ACTION_VIEW e MIME "audio/*".
     * O Android apresenta o chooser com todos os players de áudio do aparelho.
     */
    private fun abrirAudiobook() {
        val audioUri = Uri.parse("https://www.spotify.com") // Substituir pela URL real do audiobook
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(audioUri, "audio/*")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(Intent.createChooser(intent, "Ouvir Audiobook com..."))
    }
}
