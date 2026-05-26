package com.example.bibliounifornew.features.adm.dashboard

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.R
import com.example.bibliounifornew.features.adm.gerenciamento.NavigationHelperADM
import com.example.bibliounifornew.features.adm.gerenciamento.TelaRF37InfoLivroADM
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class TelaRF34FinanceiroADM : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var recyclerView: RecyclerView
    private val listaVencidos = mutableListOf<LivroVencidoModel>()

    // Adapter genérico inserido para o código compilar com o novo XML.
    // Substitua pelo seu Adapter oficial se já tiver criado um.
    private lateinit var adapter: VencidosAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf34_finaceiro_adm)

        val btnVerPendentes = findViewById<Button>(R.id.btnPendentesRetirada)
        val btnRenovarSelecionado = findViewById<Button>(R.id.btnRenovarAluguel)
        val txtNenhumVencido = findViewById<TextView>(R.id.txtNenhumVencido)
        recyclerView = findViewById(R.id.recyclerViewLivrosVencidos)

        // Configuração do RecyclerView da nova UI
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = VencidosAdapter(listaVencidos) { modelo, acao ->
            when (acao) {
                "ABRIR_LIVRO" -> {
                    val intent = Intent(this, TelaRF37InfoLivroADM::class.java)
                    intent.putExtra("LIVRO_ID", modelo.idLivro)
                    startActivity(intent)
                }
                "RENOVAR" -> {
                    renovarAluguel(modelo.docIdAtual)
                }
            }
        }
        recyclerView.adapter = adapter

        // Busca a lista de vencidos no Firestore
        carregarVencidos(txtNenhumVencido)

        // ─── VER PENDENTES ────────────────────────────────────────────────────
        btnVerPendentes?.setOnClickListener {
            // Se houver pelo menos um item, abre o modal usando o primeiro como referência (Temporário)
            if (listaVencidos.isNotEmpty()) {
                exibirPopupPendentes(listaVencidos[0])
            } else {
                Toast.makeText(this, "A lista está vazia.", Toast.LENGTH_SHORT).show()
            }
        }

        NavigationHelperADM.configurarBarraNavegacao(this)
    }

    /**
     * Busca todos os aluguéis vencidos (dataDevolucao < agora e status == ativo)
     */
    private fun carregarVencidos(txtNenhumVencido: TextView) {
        val agora = System.currentTimeMillis()

        db.collection("solicitacoes_emprestimo")
            .whereLessThan("dataDevolucao", agora)
            .whereEqualTo("status", "ativo")
            .limit(50) // Limite de segurança
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    txtNenhumVencido.visibility = View.GONE

                    for (doc in result.documents) {
                        val docId = doc.id
                        val uidAluno = doc.getString("uidAluno") ?: ""
                        val idLivro = doc.getString("idLivro") ?: ""
                        val dataMs = doc.getLong("dataSolicitacao") ?: 0L
                        val multaVal = doc.getLong("multa") ?: 20L

                        val diasVencidos = ((agora - (doc.getLong("dataDevolucao") ?: agora)) /
                                (1000 * 60 * 60 * 24)).coerceAtLeast(0)

                        val modelo = LivroVencidoModel(
                            docIdAtual = docId,
                            uidAlunoAtual = uidAluno,
                            idLivro = idLivro,
                            diasVencidos = diasVencidos,
                            multa = multaVal
                        )

                        // Joins para buscar Nomes e Livros (Processamento assíncrono)
                        db.collection("usuarios").document(uidAluno).get().addOnSuccessListener { u ->
                            modelo.nomeUsuario = u.getString("nome") ?: "Usuário Desconhecido"
                            adapter.notifyDataSetChanged()
                        }

                        db.collection("livros").document(idLivro).get().addOnSuccessListener { l ->
                            modelo.tituloLivro = l.getString("title") ?: "Livro Indisponível"
                            adapter.notifyDataSetChanged()
                        }

                        listaVencidos.add(modelo)
                    }
                    adapter.notifyDataSetChanged()

                } else {
                    txtNenhumVencido.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao carregar vencidos", Toast.LENGTH_SHORT).show()
                txtNenhumVencido.visibility = View.VISIBLE
            }
    }

    /**
     * Renova o aluguel adicionando 14 dias.
     */
    private fun renovarAluguel(docId: String) {
        val novaDevolucao = System.currentTimeMillis() + (14L * 24 * 60 * 60 * 1000)

        db.collection("solicitacoes_emprestimo").document(docId)
            .set(
                mapOf(
                    "dataDevolucao" to novaDevolucao,
                    "status" to "ativo",
                    "multa" to 0L
                ),
                SetOptions.merge()
            )
            .addOnSuccessListener {
                Toast.makeText(this, "Aluguel renovado por 14 dias.", Toast.LENGTH_SHORT).show()
                // Atualiza a lista local removendo o item renovado
                listaVencidos.removeAll { it.docIdAtual == docId }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Falha ao renovar aluguel.", Toast.LENGTH_SHORT).show()
            }
    }

    // ─── POPUP PENDENTES ─────────────────────────────────────────────────────

    private fun exibirPopupPendentes(modeloReferencia: LivroVencidoModel) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_pendentes_retirada)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnNotificarAtraso = dialog.findViewById<Button>(R.id.btnNotificarAtraso)
        val btnNotificarValor = dialog.findViewById<Button>(R.id.btnNotificarValor)
        val btnConfirmarAluguel = dialog.findViewById<Button>(R.id.btnConfirmacaoAluguel)
        val btnRemoverRegistro = dialog.findViewById<Button>(R.id.btnRemoverRegistro)

        btnNotificarAtraso?.setOnClickListener {
            val notif = hashMapOf(
                "uidAluno" to modeloReferencia.uidAlunoAtual,
                "docAluguel" to modeloReferencia.docIdAtual,
                "tipo" to "atraso",
                "mensagem" to "Seu aluguel está atrasado. Regularize para evitar bloqueios.",
                "criadoEm" to System.currentTimeMillis()
            )
            db.collection("notificacoes").add(notif)
            Toast.makeText(this, "Aviso enviado.", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        btnNotificarValor?.setOnClickListener {
            val notif = hashMapOf(
                "uidAluno" to modeloReferencia.uidAlunoAtual,
                "docAluguel" to modeloReferencia.docIdAtual,
                "tipo" to "multa",
                "mensagem" to "Existe valor pendente referente ao seu aluguel.",
                "criadoEm" to System.currentTimeMillis()
            )
            db.collection("notificacoes").add(notif)
            Toast.makeText(this, "Valor notificado.", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        btnConfirmarAluguel?.setOnClickListener {
            db.collection("solicitacoes_emprestimo").document(modeloReferencia.docIdAtual)
                .set(mapOf("status" to "confirmado"), SetOptions.merge())
            Toast.makeText(this, "Aluguel confirmado.", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        btnRemoverRegistro?.setOnClickListener {
            db.collection("solicitacoes_emprestimo").document(modeloReferencia.docIdAtual)
                .delete()
                .addOnSuccessListener {
                    listaVencidos.remove(modeloReferencia)
                    adapter.notifyDataSetChanged()
                }
            Toast.makeText(this, "Registro removido.", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }
}

// ==============================================================================
// CLASSES DE SUPORTE (MODELO E ADAPTER BÁSICO)
// ==============================================================================

data class LivroVencidoModel(
    val docIdAtual: String,
    val uidAlunoAtual: String,
    val idLivro: String,
    val diasVencidos: Long,
    val multa: Long,
    var nomeUsuario: String = "Carregando...",
    var tituloLivro: String = "Carregando..."
)

class VencidosAdapter(
    private val lista: List<LivroVencidoModel>,
    private val onClick: (LivroVencidoModel, String) -> Unit
) : RecyclerView.Adapter<VencidosAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // IDs temporários apenas para não quebrar. Em um projeto real,
        // crie um item_livro_vencido.xml e vincule aqui.
        val txtTitulo = view.findViewById<TextView>(android.R.id.text1)
        val txtSub = view.findViewById<TextView>(android.R.id.text2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Usando um layout nativo do Android apenas como placeholder
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]
        holder.txtTitulo.text = "${item.tituloLivro} (Atraso: ${item.diasVencidos} dias)"
        holder.txtSub.text = "Usuário: ${item.nomeUsuario} | Multa: R$ ${item.multa},00"

        holder.itemView.setOnClickListener {
            // Abre os detalhes do livro
            onClick(item, "ABRIR_LIVRO")
        }

        holder.itemView.setOnLongClickListener {
            // Renova o livro no clique longo (apenas para teste da lógica)
            onClick(item, "RENOVAR")
            true
        }
    }

    override fun getItemCount() = lista.size
}