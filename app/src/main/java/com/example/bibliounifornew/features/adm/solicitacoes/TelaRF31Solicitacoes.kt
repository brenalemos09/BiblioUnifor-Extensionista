package com.example.bibliounifornew.features.adm.solicitacoes

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.Window
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.R
import com.example.bibliounifornew.features.adm.gerenciamento.NavigationHelperADM

class TelaRF31Solicitacoes : AppCompatActivity() {

    private lateinit var adapter: SolicitacoesMidiaAdapter
    private val listaSolicit    = mutableListOf<ItemSolicitacaoMidia>()

    // Launcher moderno — substitui startActivityForResult deprecado
    private val fileLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { /* resultado não usado no protótipo */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf31_solicitacoes_adm)

        // ─── RECYCLERVIEW ─────────────────────────────────────────────────────
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewSolicitacoesMidia)
        adapter = SolicitacoesMidiaAdapter(
            lista            = listaSolicit,
            onVerSolicitacoes = { item -> abrirPopupSolicitacoes(item) },
            onEnviarAudiobook = { item -> escolherArquivo("audio/*", item, "audiobook") },
            onEnviarPdf       = { item -> escolherArquivo("application/pdf", item, "pdf") },
            onBraille         = { item -> notificarBraille(item) },
            onExcluir         = { item, pos -> abrirPopupExcluir(item, pos) }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // ─── FILTRO / BUSCA ───────────────────────────────────────────────────
        val editPesquisa = findViewById<EditText>(R.id.editPesquisaMidia)
        editPesquisa?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { /* filtro futuro */ }
            override fun afterTextChanged(s: Editable?) {}
        })

        val btnFiltro = findViewById<ImageView>(R.id.buttonFiltroMidia)
        btnFiltro?.setOnClickListener { abrirPopupFiltro() }

        carregarSolicitacoesMock()
        NavigationHelperADM.configurarBarraNavegacao(this)
    }

    /**
     * Carrega solicitações de mídia mockadas para o protótipo.
     */
    private fun carregarSolicitacoesMock() {
        val mockData = listOf(
            ItemSolicitacaoMidia("1", "u1", "l1", "PDF", "pendente", "João Silva", "O Hobbit", "J.R.R. Tolkien"),
            ItemSolicitacaoMidia("2", "u2", "l2", "Audiobook", "pendente", "Maria Oliveira", "1984", "George Orwell"),
            ItemSolicitacaoMidia("3", "u3", "l3", "Braille", "pendente", "Carlos Santos", "Dom Casmurro", "Machado de Assis")
        )
        adapter.atualizarLista(mockData)
    }

    private fun carregarSolicitacoes() {
        // Redireciona para mock no protótipo
        carregarSolicitacoesMock()
    }

    // ─── POPUP VER SOLICITAÇÕES DO USUÁRIO ───────────────────────────────────

    private fun abrirPopupSolicitacoes(item: ItemSolicitacaoMidia) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_solicitacoes_usuario_adm)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialog.findViewById<TextView>(R.id.textPopupNomeUsuario)?.text    = "Usuário: ${item.nomeUsuario}"
        dialog.findViewById<TextView>(R.id.textPopupListaSolicitacoes)?.text = "Solicitações: ${item.tiposSolicit}"
        dialog.findViewById<TextView>(R.id.textPopupStatus)?.text         = "Status: ${item.status}"
        dialog.findViewById<Button>(R.id.btnFecharSolicitacoes)?.setOnClickListener { dialog.dismiss() }
        dialog.show()
        val w = (resources.displayMetrics.widthPixels * 0.90).toInt()
        dialog.window?.setLayout(w, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    // ─── ESCOLHER ARQUIVO ────────────────────────────────────────────────────

    private fun escolherArquivo(mimeType: String, item: ItemSolicitacaoMidia, tipo: String) {
        try {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = mimeType
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            fileLauncher.launch(Intent.createChooser(intent, "Selecione o arquivo"))
            
            // Simulação de sucesso no protótipo
            Toast.makeText(this, "${tipo.replaceFirstChar { it.uppercase() }} aprovado e usuário notificado.", Toast.LENGTH_SHORT).show()
            
        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao abrir seletor de arquivo.", Toast.LENGTH_SHORT).show()
        }
    }

    // ─── NOTIFICAR BRAILLE ────────────────────────────────────────────────────

    private fun notificarBraille(item: ItemSolicitacaoMidia) {
        // Simulação de sucesso no protótipo
        Toast.makeText(this, "Braille aprovado e usuário notificado.", Toast.LENGTH_SHORT).show()
    }

    // ─── POPUP EXCLUIR ────────────────────────────────────────────────────────

    private fun abrirPopupExcluir(item: ItemSolicitacaoMidia, position: Int) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_confirmar_exclusao_solicitacao)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val editSenha  = dialog.findViewById<EditText>(R.id.editSenhaConfirmacao)
        val btnOlho    = dialog.findViewById<ImageView>(R.id.buttonVerSenha)
        val btnConfirm = dialog.findViewById<Button>(R.id.btnConfirmarExclusao)
        val btnCancel  = dialog.findViewById<Button>(R.id.btnCancelarExclusao)

        var senhaVisivel = false
        btnOlho?.setOnClickListener {
            senhaVisivel = !senhaVisivel
            editSenha?.inputType = if (senhaVisivel)
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            else
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            btnOlho.setImageResource(if (senhaVisivel) R.drawable.ic_eye_open else R.drawable.ic_eye_closed)
            editSenha?.setSelection(editSenha.text.length)
        }

        btnConfirm?.setOnClickListener {
            val senha = editSenha?.text.toString()
            if (senha != "DevsAB") {
                Toast.makeText(this, "Credencial incorreta.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // Simulação no protótipo
            adapter.removerItem(position)
            Toast.makeText(this, "Solicitação excluída.", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        btnCancel?.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    // ─── POPUP FILTRO ────────────────────────────────────────────────────────

    private fun abrirPopupFiltro() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_filtrar_midia)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val spinner  = dialog.findViewById<Spinner>(R.id.spinnerSolicitacao)
        val editNome = dialog.findViewById<EditText>(R.id.editNomeUsuario)
        val btnSalvar = dialog.findViewById<Button>(R.id.btnSalvarFiltro)
        val btnLimpar = dialog.findViewById<Button>(R.id.btnLimparFiltro)

        val opcoes = arrayOf("Selecione...", "PDF", "Audiobook", "Braille")
        spinner?.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, opcoes)

        btnSalvar?.setOnClickListener {
            Toast.makeText(this, "Filtro aplicado.", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        btnLimpar?.setOnClickListener {
            editNome?.setText("")
            spinner?.setSelection(0)
            dialog.dismiss()
        }
        dialog.show()
    }
}
