package com.example.bibliounifornew.features.usuario.solicitacao

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.CheckBox
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.Solicitacao
import com.example.bibliounifornew.data.SolicitacaoRepository
import com.example.bibliounifornew.features.usuario.livro.TelaRF12TelaDoLivro
import com.example.bibliounifornew.data.UsuarioRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class TelaRF19SolicitacoesTermosCondicoes : AppCompatActivity() {

    // ─── DEPENDÊNCIAS ────────────────────────────────────────────────────────
    // SolicitacaoRepository encapsula toda a lógica de persistência.
    private val solicitacaoRepository = SolicitacaoRepository()
    private val usuarioRepository     = UsuarioRepository()
    private val auth                  = FirebaseAuth.getInstance()
    private val db                    = FirebaseFirestore.getInstance()

    private var tituloLivro: String = ""
    private var autorLivro: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf19_solicitacoes_termos_condicoes)

        // ─── EXTRAS RECEBIDOS DE TelaRF19Solicitacoes ────────────────────────
        val tipoMidia = intent.getStringExtra("TIPO_MIDIA") ?: ""
        val livroId   = intent.getStringExtra("LIVRO_ID")   ?: ""
        tituloLivro   = intent.getStringExtra("TITULO")     ?: ""
        autorLivro    = intent.getStringExtra("AUTOR")      ?: ""

        // ─── VIEWS ────────────────────────────────────────────────────────────
        val scrollView   = findViewById<ScrollView>(R.id.scrollTermos)
        val checkBox     = findViewById<CheckBox>(R.id.checkTelaAceitarTermos)
        val btnConfirmar = findViewById<Button>(R.id.buttonConfirmarTermosTela)
        val textAviso    = findViewById<TextView>(R.id.textAvisoScroll)

        // Bloqueados até o usuário rolar o scroll até o fim dos termos
        checkBox.isEnabled     = false
        btnConfirmar.isEnabled = false
        btnConfirmar.alpha     = 0.5f

        // ─── LÓGICA DE SCROLL → HABILITA CHECKBOX ────────────────────────────
        scrollView.viewTreeObserver.addOnScrollChangedListener {
            val child = scrollView.getChildAt(scrollView.childCount - 1)
            val diff  = child.bottom - (scrollView.height + scrollView.scrollY)
            if (diff <= 0) {
                checkBox.isEnabled = true
                textAviso.visibility = android.view.View.GONE
            }
        }

        // ─── CHECKBOX → HABILITA BOTÃO ────────────────────────────────────────
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            btnConfirmar.isEnabled = isChecked
            btnConfirmar.alpha     = if (isChecked) 1.0f else 0.5f
        }

        // ─── CONFIRMAR → PERSISTE NO FIRESTORE ───────────────────────────────
        btnConfirmar.setOnClickListener {
            gravarSolicitacao(tipoMidia, livroId)
        }
    }

    // ─── PERSISTÊNCIA VIA REPOSITORY ─────────────────────────────────────────

    /**
     * Verifica duplicata e grava a solicitação de forma segura.
     *
     * Passo 1 (IO thread): consulta "solicitacoes_midia" para o par uid+livroId
     *   com status em {pendente, em_andamento}. Se já existir, exibe Toast e para.
     *
     * Passo 2 (Main thread): chama [SolicitacaoRepository.gravarSolicitacao] via
     *   callback — o botão fica desabilitado até o Firestore confirmar o write.
     *
     * @param tipoMidia "PDF" | "Braille" | "Audiobook" | "Reserva"
     * @param livroId   ID do documento na coleção "livros"
     */
    private fun gravarSolicitacao(tipoMidia: String, livroId: String) {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrEmpty()) {
            Toast.makeText(this, "Faça login para solicitar.", Toast.LENGTH_SHORT).show()
            return
        }

        val btnConfirmar = findViewById<Button>(R.id.buttonConfirmarTermosTela)
        btnConfirmar?.isEnabled = false

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // ── DEDUPLICATION CHECK ───────────────────────────────────────
                // Evita que o mesmo usuário crie N solicitações para o mesmo livro.
                val duplicata = db.collection("solicitacoes_midia")
                    .whereEqualTo("uidUsuario", uid)
                    .whereEqualTo("idLivro", livroId)
                    .whereIn("status", listOf("pendente", "em_andamento"))
                    .get()
                    .await()

                if (!duplicata.isEmpty) {
                    withContext(Dispatchers.Main) {
                        if (isFinishing || isDestroyed) return@withContext
                        btnConfirmar?.isEnabled = true
                        Toast.makeText(
                            this@TelaRF19SolicitacoesTermosCondicoes,
                            getString(R.string.msg_solicitacao_duplicada),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    return@launch
                }

                // ── GRAVAR NO FIRESTORE ───────────────────────────────────────
                val solicitacao = Solicitacao(
                    uidUsuario      = uid,
                    uidAluno        = uid,      // alias de compatibilidade com RF31 (ADM)
                    idLivro         = livroId,
                    tipos           = tipoMidia,
                    status          = "pendente",
                    dataSolicitacao = System.currentTimeMillis()
                )

                withContext(Dispatchers.Main) {
                    if (isFinishing || isDestroyed) return@withContext

                    solicitacaoRepository.gravarSolicitacao(solicitacao) { sucesso, _, erro ->
                        if (isFinishing || isDestroyed) return@gravarSolicitacao

                        if (sucesso) {
                            // RF15.8: Registra no histórico a solicitação
                            val acao = when (tipoMidia) {
                                "Reserva" -> "Reserva Solicitada"
                                else      -> "$tipoMidia Solicitado"
                            }
                            usuarioRepository.registrarNoHistorico(
                                uid, livroId, tituloLivro, autorLivro, acao)
                            showPopupSucesso(livroId)
                        } else {
                            btnConfirmar?.isEnabled = true
                            Toast.makeText(
                                this@TelaRF19SolicitacoesTermosCondicoes,
                                "Erro ao registrar solicitação: $erro",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    if (isFinishing || isDestroyed) return@withContext
                    btnConfirmar?.isEnabled = true
                    Toast.makeText(
                        this@TelaRF19SolicitacoesTermosCondicoes,
                        getString(R.string.erro_conexao_banco),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    // ─── POPUP DE SUCESSO ─────────────────────────────────────────────────────

    /**
     * Exibe o popup de confirmação. Ao clicar em OK, retorna à tela do livro
     * (RF12) usando FLAG_ACTIVITY_CLEAR_TOP + LIVRO_ID, preservando a pilha de
     * navegação sem criar uma instância nova sem ID.
     *
     * ┌── Back stack após solicitação ──────────────────────────────┐
     * │  RF12 (TelaRF12TelaDoLivro / TelaLivroActivity)             │
     * │    ← RF19 (TelaRF19Solicitacoes)         [destruída]        │
     * │       ← RF19Terms (esta Activity)        [destruída]        │
     * └─────────────────────────────────────────────────────────────┘
     * FLAG_ACTIVITY_CLEAR_TOP remove RF19 e RF19Terms e reutiliza RF12.
     * O LIVRO_ID é passado para garantir que o onCreate/onNewIntent tenha dados.
     */
    private fun showPopupSucesso(livroId: String) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.telarf19_solicitacoes_voltar_biblioteca)
        dialog.window?.setBackgroundDrawable(
            android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT)
        )
        dialog.setCancelable(false)

        dialog.findViewById<Button>(R.id.buttonPopupOkSolicitacao)
            ?.setOnClickListener {
                dialog.dismiss()
                voltarParaLivro(livroId)
            }

        dialog.show()
    }

    /**
     * Navegação correta de retorno à tela do livro.
     * Passa o LIVRO_ID explicitamente para que a Activity de destino
     * possa re-popular a UI mesmo que seja recriada pelo CLEAR_TOP.
     */
    private fun voltarParaLivro(livroId: String) {
        val intent = Intent(this, TelaRF12TelaDoLivro::class.java)
            .putExtra("LIVRO_ID", livroId)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
        finish()
    }
}
