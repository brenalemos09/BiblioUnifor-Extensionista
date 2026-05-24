package com.example.bibliounifornew.usuario

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.AppDatabase
import com.example.bibliounifornew.data.AuthRepository
import com.example.bibliounifornew.data.LivroRepository
import com.example.bibliounifornew.data.UsuarioRepository
import com.example.bibliounifornew.viewmodel.LivroViewModel
import com.example.bibliounifornew.viewmodel.LivroViewModelFactory
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.launch

class TelaRF12TelaDoLivro : AppCompatActivity() {

    private lateinit var viewModel      : LivroViewModel
    private val authRepository          = AuthRepository()
    private val usuarioRepository       = UsuarioRepository()
    private val db                      = FirebaseFirestore.getInstance()

    private var livroIdAtual : String = ""
    /** Armazenados após carregarDadosDoLivro para uso nos botões Livraria / Lista de Desejos */
    private var tituloAtual  : String = ""
    private var autorAtual   : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf12_teladolivro)

        val database   = AppDatabase.getDatabase(applicationContext)
        val repository = LivroRepository(database.livroDao(), FirebaseFirestore.getInstance())
        viewModel = ViewModelProvider(this, LivroViewModelFactory(repository))
            .get(LivroViewModel::class.java)

        val livroId = intent.getStringExtra("LIVRO_ID") ?: ""
        livroIdAtual = livroId

        if (livroId.isNotEmpty()) {
            carregarDadosDoLivro(livroId)
        }

        configurarBotoesDeStatus()
        configurarBotoesAcao()
    }

    // ─── CARREGAMENTO DE DADOS ────────────────────────────────────────────────

    private fun carregarDadosDoLivro(id: String) {
        lifecycleScope.launch {
            try {
                val livro = viewModel.buscarLivroPorId(id)
                livro?.let {
                    tituloAtual = it.title ?: ""
                    autorAtual  = it.author ?: ""

                    findViewById<TextView>(R.id.textTituloLivro)?.text = tituloAtual
                    findViewById<TextView>(R.id.textAutorLivro)?.text  = autorAtual
                    findViewById<TextView>(R.id.textSobreLivro)?.text  = it.description

                    val imgCapa = findViewById<ImageView>(R.id.imageLivroDetalhes)
                    if (!it.coverUrl.isNullOrEmpty()) {
                        imgCapa?.load(it.coverUrl) {
                            placeholder(R.drawable.osda)
                            error(R.drawable.osda)
                        }
                    } else {
                        imgCapa?.setImageResource(R.drawable.osda)
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@TelaRF12TelaDoLivro, "Erro ao carregar livro.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ─── BOTÕES DE STATUS DE LEITURA ─────────────────────────────────────────

    private fun configurarBotoesDeStatus() {
        val btnNaoLido = findViewById<MaterialButton>(R.id.buttonNaoLido) ?: return
        val btnLendo   = findViewById<MaterialButton>(R.id.buttonLendo)   ?: return
        val btnLido    = findViewById<MaterialButton>(R.id.buttonLido)    ?: return

        // Estado inicial: nenhum selecionado (todos inativos)
        definirBotaoInativo(btnNaoLido)
        definirBotaoInativo(btnLendo)
        definirBotaoInativo(btnLido)

        btnNaoLido.setOnClickListener {
            definirBotaoAtivo(btnNaoLido)
            definirBotaoInativo(btnLendo)
            definirBotaoInativo(btnLido)
            salvarStatusNoFirestore("Não Lido")
        }
        btnLendo.setOnClickListener {
            definirBotaoInativo(btnNaoLido)
            definirBotaoAtivo(btnLendo)
            definirBotaoInativo(btnLido)
            salvarStatusNoFirestore("Lendo")
        }
        btnLido.setOnClickListener {
            definirBotaoInativo(btnNaoLido)
            definirBotaoInativo(btnLendo)
            definirBotaoAtivo(btnLido)
            salvarStatusNoFirestore("Lido")
        }
    }

    /**
     * Botão ativo: fundo biblio_blue + texto branco (padrão visual do app).
     */
    private fun definirBotaoAtivo(btn: MaterialButton) {
        btn.backgroundTintList = getColorStateList(R.color.biblio_blue)
        btn.setTextColor(getColor(android.R.color.white))
    }

    /**
     * Botão inativo: fundo biblio_detalhes (azul claro) + texto biblio_dark.
     */
    private fun definirBotaoInativo(btn: MaterialButton) {
        btn.backgroundTintList = getColorStateList(R.color.biblio_detalhes)
        btn.setTextColor(getColor(R.color.biblio_dark))
    }

    /**
     * Persiste o status de leitura em biblioteca_usuarios/{uid}_{livroId}.
     * Usa SetOptions.merge() para não sobrescrever outros campos do documento.
     */
    private fun salvarStatusNoFirestore(status: String) {
        if (livroIdAtual.isEmpty()) return
        val uid = authRepository.getUsuarioAtual()?.uid ?: return
        val docId = "${uid}_${livroIdAtual}"

        val campos = hashMapOf(
            "usuarioId"     to uid,
            "livroId"       to livroIdAtual,
            "titulo"        to tituloAtual,
            "autor"         to autorAtual,
            "statusLeitura" to status,
            "atualizadoEm"  to System.currentTimeMillis()
        )

        db.collection("biblioteca_usuarios").document(docId)
            .set(campos, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(this, "Status: $status salvo!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao salvar status.", Toast.LENGTH_SHORT).show()
            }
    }

    // ─── BOTÕES DE AÇÃO ───────────────────────────────────────────────────────

    private fun configurarBotoesAcao() {
        // Botão "Lista de Desejos" → salva em lista_desejos/{uid}_{livroId}
        findViewById<MaterialButton>(R.id.buttonListaDesejos)?.setOnClickListener {
            adicionarListaDesejos()
        }

        // Botão "Sua Livraria" → adiciona/atualiza em biblioteca_usuarios/{uid}_{livroId}
        findViewById<MaterialButton>(R.id.buttonSuaLivraria)?.setOnClickListener {
            adicionarSuaLivraria()
        }

        // Botão "Ver Mais"
        findViewById<MaterialButton>(R.id.buttonVerMais)?.setOnClickListener {
            val intent = Intent(this, TelaRF13VerMaisLivro::class.java)
            intent.putExtra("LIVRO_ID", livroIdAtual)
            startActivity(intent)
        }

        // Botão "Solicitar"
        findViewById<MaterialButton>(R.id.buttonSolicitar)?.setOnClickListener {
            val intent = Intent(this, TelaRF19Solicitacoes::class.java)
            intent.putExtra("LIVRO_ID", livroIdAtual)
            startActivity(intent)
        }

        // Botão "Ler Agora" → abre RF14 passando o livroId
        findViewById<MaterialButton>(R.id.buttonLer)?.setOnClickListener {
            val intent = Intent(this, TelaRF14LeituraActivity::class.java)
            intent.putExtra("LIVRO_ID", livroIdAtual)
            startActivity(intent)
        }
    }

    /**
     * Salva o livro na lista de desejos do usuário.
     * Documento: lista_desejos/{uid}_{livroId}
     * Campos adicionais título/autor são armazenados para exibição em RF16.
     */
    private fun adicionarListaDesejos() {
        if (livroIdAtual.isEmpty()) {
            Toast.makeText(this, "Livro sem ID. Tente novamente.", Toast.LENGTH_SHORT).show()
            return
        }
        val uid = authRepository.getUsuarioAtual()?.uid ?: run {
            Toast.makeText(this, "Faça login para usar esta função.", Toast.LENGTH_SHORT).show()
            return
        }

        val dados = hashMapOf(
            "usuarioId"    to uid,
            "livroId"      to livroIdAtual,
            "titulo"       to tituloAtual,
            "autor"        to autorAtual,
            "adicionadoEm" to System.currentTimeMillis()
        )

        usuarioRepository.salvarListaDesejos(uid, livroIdAtual, dados) { sucesso, _ ->
            if (sucesso) {
                Toast.makeText(this, "\"${tituloAtual.ifEmpty { livroIdAtual }}\" adicionado à Lista de Desejos!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Erro ao adicionar à Lista de Desejos.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Adiciona o livro à livraria pessoal do usuário.
     * Documento: biblioteca_usuarios/{uid}_{livroId}
     * Inclui titulo/autor para exibição dinâmica na RF15.
     * SetOptions.merge() garante que statusLeitura existente não seja sobrescrito.
     */
    private fun adicionarSuaLivraria() {
        if (livroIdAtual.isEmpty()) {
            Toast.makeText(this, "Livro sem ID. Tente novamente.", Toast.LENGTH_SHORT).show()
            return
        }
        val uid = authRepository.getUsuarioAtual()?.uid ?: run {
            Toast.makeText(this, "Faça login para usar esta função.", Toast.LENGTH_SHORT).show()
            return
        }

        val docId = "${uid}_${livroIdAtual}"
        val dados = hashMapOf(
            "usuarioId"     to uid,
            "livroId"       to livroIdAtual,
            "titulo"        to tituloAtual,
            "autor"         to autorAtual,
            "statusLeitura" to "Não Lido",   // Default ao adicionar; botões de status podem alterar depois
            "adicionadoEm"  to System.currentTimeMillis()
        )

        db.collection("biblioteca_usuarios").document(docId)
            .set(dados, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(this, "\"${tituloAtual.ifEmpty { livroIdAtual }}\" adicionado à sua Livraria!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao adicionar à Livraria: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
