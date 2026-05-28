package com.example.bibliounifornew.features.adm.dashboard

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.R
import com.example.bibliounifornew.features.adm.gerenciamento.NavigationHelperADM
import com.example.bibliounifornew.features.adm.gerenciamento.TelaRF37InfoLivroADM
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TelaRF34FinanceiroADM : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private val listaVencidos = mutableListOf<LivroVencidoModel>()
    private lateinit var adapter: VencidosAdapter

    companion object {
        const val MULTA_POR_DIA = 2L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf34_finaceiro_adm)

        val btnVerPendentes  = findViewById<MaterialButton>(R.id.btnPendentesRetirada)
        val txtNenhumVencido = findViewById<TextView>(R.id.txtNenhumVencido)
        recyclerView         = findViewById(R.id.recyclerViewLivrosVencidos)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = VencidosAdapter(listaVencidos) { modelo, acao ->
            when (acao) {
                "ABRIR_LIVRO"  -> {
                    val intent = Intent(this, TelaRF37InfoLivroADM::class.java)
                    intent.putExtra("LIVRO_ID", modelo.idLivro)
                    startActivity(intent)
                }
                "RENOVAR"      -> renovarAluguelMock(modelo)
                "VER_DETALHES" -> exibirPopupPendentes(modelo)
            }
        }
        recyclerView.adapter = adapter

        carregarVencidosMock(txtNenhumVencido)

        btnVerPendentes?.setOnClickListener {
            if (listaVencidos.isNotEmpty()) {
                exibirPopupPendentes(listaVencidos[0])
            } else {
                Toast.makeText(this, "Nenhuma pendência encontrada", Toast.LENGTH_SHORT).show()
            }
        }

        NavigationHelperADM.configurarBarraNavegacao(this)
    }

    private fun carregarVencidosMock(txtNenhumVencido: TextView?) {
        listaVencidos.clear()
        
        val agora = System.currentTimeMillis()
        val umDiaMs = 24 * 60 * 60 * 1000L
        
        listaVencidos.add(LivroVencidoModel(
            docIdAtual = "venc1",
            uidAlunoAtual = "u1",
            idLivro = "l1",
            dataDevolucaoMs = agora - (3 * umDiaMs),
            diasVencidos = 3,
            multa = 6,
            nomeUsuario = "Alice Smith",
            tituloLivro = "O Senhor dos Anéis"
        ))

        listaVencidos.add(LivroVencidoModel(
            docIdAtual = "venc2",
            uidAlunoAtual = "u2",
            idLivro = "l2",
            dataDevolucaoMs = agora - (5 * umDiaMs),
            diasVencidos = 5,
            multa = 10,
            nomeUsuario = "Bob Brown",
            tituloLivro = "Cálculo Volume 1"
        ))

        if (listaVencidos.isEmpty()) {
            txtNenhumVencido?.visibility = View.VISIBLE
        } else {
            txtNenhumVencido?.visibility = View.GONE
            adapter.notifyDataSetChanged()
        }
    }

    private fun renovarAluguelMock(modelo: LivroVencidoModel) {
        Handler(Looper.getMainLooper()).postDelayed({
            Toast.makeText(this, "Aluguel renovado com sucesso!", Toast.LENGTH_SHORT).show()
            listaVencidos.remove(modelo)
            adapter.notifyDataSetChanged()
            findViewById<TextView>(R.id.txtNenhumVencido)?.visibility = if (listaVencidos.isEmpty()) View.VISIBLE else View.GONE
        }, 500)
    }

    private fun exibirPopupPendentes(modelo: LivroVencidoModel) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_pendentes_retirada)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val sdf            = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
        val prazoFormatado = sdf.format(Date(modelo.dataDevolucaoMs))
        val multaFormatada = String.format("%.2f", modelo.multa.toDouble()).replace('.', ',')
        
        // Tentando preencher os campos do popup
        dialog.findViewById<TextView>(R.id.textInfoPendencia)?.text = 
            "Livro: ${modelo.tituloLivro}\nUsuário: ${modelo.nomeUsuario}\nPrazo: $prazoFormatado\nAtraso: ${modelo.diasVencidos} dias\nMulta: R$ $multaFormatada"

        val btnNotificarAtraso  = dialog.findViewById<MaterialButton>(R.id.btnNotificarAtraso)
        val btnNotificarValor   = dialog.findViewById<MaterialButton>(R.id.btnNotificarValor)
        val btnConfirmarAluguel = dialog.findViewById<MaterialButton>(R.id.btnConfirmacaoAluguel)
        val btnRemoverRegistro  = dialog.findViewById<MaterialButton>(R.id.btnRemoverRegistro)

        btnNotificarAtraso?.setOnClickListener {
            Toast.makeText(this, "Aviso de atraso enviado ao usuário!", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        btnNotificarValor?.setOnClickListener {
            Toast.makeText(this, "Notificação de multa enviada!", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        btnConfirmarAluguel?.setOnClickListener {
            Toast.makeText(this, "Aluguel confirmado com sucesso!", Toast.LENGTH_SHORT).show()
            listaVencidos.remove(modelo)
            adapter.notifyDataSetChanged()
            findViewById<TextView>(R.id.txtNenhumVencido)?.visibility = if (listaVencidos.isEmpty()) View.VISIBLE else View.GONE
            dialog.dismiss()
        }

        btnRemoverRegistro?.setOnClickListener {
            Toast.makeText(this, "Registro removido localmente.", Toast.LENGTH_SHORT).show()
            listaVencidos.remove(modelo)
            adapter.notifyDataSetChanged()
            findViewById<TextView>(R.id.txtNenhumVencido)?.visibility = if (listaVencidos.isEmpty()) View.VISIBLE else View.GONE
            dialog.dismiss()
        }

        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.90).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.show()
    }
}

data class LivroVencidoModel(
    val docIdAtual       : String,
    val uidAlunoAtual    : String,
    val idLivro          : String,
    val dataDevolucaoMs  : Long,
    val diasVencidos     : Long,
    val multa            : Long,
    var nomeUsuario      : String = "Usuário",
    var tituloLivro      : String = "Livro"
)

class VencidosAdapter(
    private val lista   : List<LivroVencidoModel>,
    private val onClick : (LivroVencidoModel, String) -> Unit
) : RecyclerView.Adapter<VencidosAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtTitulo  : TextView      = view.findViewById(R.id.txtTituloVencido)
        val txtUsuario : TextView      = view.findViewById(R.id.txtUsuarioVencido)
        val txtDias    : TextView      = view.findViewById(R.id.txtDiasAtrasoVencido)
        val txtMulta   : TextView      = view.findViewById(R.id.txtMultaVencido)
        val btnRenovar : MaterialButton = view.findViewById(R.id.btnRenovarVencido)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_livro_vencido, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]
        holder.txtTitulo.text  = item.tituloLivro
        holder.txtUsuario.text = "Usuário: ${item.nomeUsuario}"
        holder.txtDias.text    = "Atraso: ${item.diasVencidos} dias"
        holder.txtMulta.text   = "Multa: R$ ${String.format("%.2f", item.multa.toDouble()).replace('.', ',')}"

        holder.itemView.setOnClickListener { onClick(item, "VER_DETALHES") }
        holder.btnRenovar.setOnClickListener { onClick(item, "RENOVAR") }
    }

    override fun getItemCount() = lista.size
}
