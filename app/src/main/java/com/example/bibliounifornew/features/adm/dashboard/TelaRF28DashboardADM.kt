package com.example.bibliounifornew.features.adm.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R
import com.example.bibliounifornew.features.adm.gerenciamento.NavigationHelperADM
import com.example.bibliounifornew.features.adm.gerenciamento.TelaRF27CrudAdm
import com.example.bibliounifornew.features.adm.gerenciamento.TelaRF35ConfirmarCadastroADM
import com.example.bibliounifornew.features.adm.gerenciamento.TelaRF38ConfigADM
import com.example.bibliounifornew.features.adm.solicitacoes.TelaRF31Solicitacoes
import com.example.bibliounifornew.features.adm.solicitacoes.TelaRF36ListaAlugueisADM
import coil.load
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class TelaRF28DashboardADM : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf28_inicial_adm)

        // ─── NAVEGAÇÃO ────────────────────────────────────────────────────────
        findViewById<MaterialButton>(R.id.buttonCrudAdm)?.setOnClickListener {
            startActivity(Intent(this, TelaRF27CrudAdm::class.java))
        }
        findViewById<MaterialButton>(R.id.buttonVerAlugueis)?.setOnClickListener {
            startActivity(Intent(this, TelaRF36ListaAlugueisADM::class.java))
        }
        findViewById<MaterialButton>(R.id.buttonVerAtrasos)?.setOnClickListener {
            startActivity(Intent(this, TelaRF34FinanceiroADM::class.java))
        }
        findViewById<MaterialButton>(R.id.buttonVerCadastros)?.setOnClickListener {
            startActivity(Intent(this, TelaRF35ConfirmarCadastroADM::class.java))
        }
        findViewById<MaterialButton>(R.id.buttonVerSolicitacoes)?.setOnClickListener {
            startActivity(Intent(this, TelaRF31Solicitacoes::class.java))
        }
        findViewById<ImageView>(R.id.iconConfigAdm)?.setOnClickListener {
            startActivity(Intent(this, TelaRF38ConfigADM::class.java))
        }

        // ─── DADOS MOCKADOS ───────────────────────────────────────────────────
        carregarDadosPerfilMock()
        carregarEstatisticasMock()
        carregarAtrasosCriticosMock()
        carregarAnaliseAlugueisMock()

        NavigationHelperADM.configurarBarraNavegacao(this)
    }

    override fun onResume() {
        super.onResume()
        carregarDadosPerfilMock()
        carregarEstatisticasMock()
        carregarAtrasosCriticosMock()
        carregarAnaliseAlugueisMock()
    }

    private fun carregarDadosPerfilMock() {
        val txtBemVindo = findViewById<TextView>(R.id.textBemVindoAdm)
        val imageFoto = findViewById<ImageView>(R.id.imageFotoAdmInterna)

        txtBemVindo.text = "Bem-vindo, Administrador"
        imageFoto?.setImageResource(R.drawable.user_placeholder)
    }

    private fun carregarEstatisticasMock() {
        val txtUsuarios  = findViewById<TextView>(R.id.txtContadorUsuarios)
        val txtAlugueis  = findViewById<TextView>(R.id.txtContadorAlugueis)
        val containerCad = findViewById<LinearLayout>(R.id.containerCadastrosPendentes)

        txtUsuarios?.text = "150"
        txtAlugueis?.text = "42"

        containerCad?.removeAllViews()
        containerCad?.addView(criarLinhaTexto("• João Silva", "#415E5E", false))
        containerCad?.addView(criarLinhaTexto("• Maria Oliveira", "#415E5E", false))
        containerCad?.addView(criarLinhaTexto("• Carlos Santos", "#415E5E", false))
    }

    private fun carregarAtrasosCriticosMock() {
        val container = findViewById<LinearLayout>(R.id.containerAtrasosCriticos) ?: return
        container.removeAllViews()
        container.addView(criarLinhaAtrasoCritico("Pedro Rocha", "5 dias"))
        container.addView(criarLinhaAtrasoCritico("Ana Costa", "3 dias"))
        container.addView(criarLinhaAtrasoCritico("Lucas Souza", "1 dia"))
    }

    private fun carregarAnaliseAlugueisMock() {
        val container = findViewById<LinearLayout>(R.id.containerAnaliseAlugueis) ?: return
        container.removeAllViews()
        container.addView(criarLinhaAnalise("Maio 2024", "128"))
        container.addView(criarLinhaAnalise("Abril 2024", "115"))
        container.addView(criarLinhaAnalise("Março 2024", "95"))
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers de criação de Views programáticas
    // ─────────────────────────────────────────────────────────────────────────

    private fun criarLinhaTexto(texto: String, colorHex: String, bold: Boolean): TextView {
        return TextView(this).apply {
            text    = texto
            setTextColor(android.graphics.Color.parseColor(colorHex))
            textSize = 16f
            if (bold) setTypeface(typeface, android.graphics.Typeface.BOLD)
            setPadding(0, 8, 0, 8)
        }
    }

    /** Linha horizontal: nome à esquerda (weight 1) + dias em vermelho à direita */
    private fun criarLinhaAtrasoCritico(nome: String, dias: String): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams = lp
            setPadding(0, 8, 0, 8)

            addView(TextView(context).apply {
                text = nome
                setTextColor(android.graphics.Color.parseColor("#415E5E"))
                textSize = 16f
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            })
            addView(TextView(context).apply {
                text = dias
                setTextColor(resources.getColor(R.color.biblio_red, null))
                textSize = 16f
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                gravity = Gravity.END
            })
        }
    }

    /** Linha horizontal: nome mês (weight 1) + count em preto negrito à direita */
    private fun criarLinhaAnalise(nomeMes: String, count: String): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(0, 8, 0, 8)

            addView(TextView(context).apply {
                text = nomeMes
                setTextColor(android.graphics.Color.parseColor("#415E5E"))
                textSize = 16f
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            })
            addView(TextView(context).apply {
                text = count
                setTextColor(android.graphics.Color.BLACK)
                textSize = 18f
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                gravity = Gravity.END
            })
        }
    }
}
