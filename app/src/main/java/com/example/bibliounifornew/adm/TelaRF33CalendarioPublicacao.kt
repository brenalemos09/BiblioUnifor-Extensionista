package com.example.bibliounifornew.adm

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.InputMethodManager
import android.widget.CalendarView
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.*

class TelaRF33CalendarioPublicacao : AppCompatActivity() {

    private lateinit var calendarView: CalendarView
    private lateinit var etData: EditText
    private lateinit var tvMesAno: TextView
    
    private val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
        isLenient = false
    }
    private val sdfMesAno = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.popup_calendario_publicacao)

        calendarView = findViewById(R.id.calendarViewPublicacao)
        etData = findViewById(R.id.textDataSelecionada)
        tvMesAno = findViewById(R.id.textMesAno)
        
        val btnConfirmar = findViewById<MaterialButton>(R.id.btnConfirmar)
        val btnVoltar = findViewById<TextView>(R.id.textVoltar)
        val btnEditar = findViewById<ImageButton>(R.id.btnEditarData)
        val btnAnterior = findViewById<ImageButton>(R.id.btnMesAnterior)
        val btnProximo = findViewById<ImageButton>(R.id.btnProximoMes)

        // Receber data atual se vier da tela anterior
        val dataVindaDeFora = intent.getStringExtra("dataAtual") ?: ""
        if (dataVindaDeFora.isNotEmpty() && dataVindaDeFora.length == 10) {
            try {
                val date = sdf.parse(dataVindaDeFora)
                if (date != null) {
                    calendarView.date = date.time
                    etData.setText(dataVindaDeFora)
                    atualizarTextoMesAno(date.time)
                }
            } catch (e: Exception) {
                configurarDataPadrao()
            }
        } else {
            configurarDataPadrao()
        }

        // Botão Lápis: Habilitar edição, focar e abrir teclado
        btnEditar.setOnClickListener {
            etData.isEnabled = true
            etData.requestFocus()
            etData.setSelection(etData.text.length)
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(etData, InputMethodManager.SHOW_IMPLICIT)
        }

        // Calendário -> Campo de Texto
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val cal = Calendar.getInstance()
            cal.set(year, month, dayOfMonth)
            val dataFormatada = sdf.format(cal.time)
            
            // Atualiza o campo sem disparar o loop do TextWatcher se possível, 
            // ou apenas aceita que o TextWatcher vai reprocessar (isUpdating ajuda)
            etData.setText(dataFormatada)
            atualizarTextoMesAno(cal.timeInMillis)
        }

        // Campo de Texto -> Calendário (Máscara Automática e Sincronização)
        etData.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false
            private var oldString = ""

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                oldString = s.toString()
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isUpdating) return

                val str = s.toString()
                val unmasked = str.replace("/", "")

                // Se o usuário está apagando a barra, apaga o número anterior também
                var formatted = ""
                if (unmasked.length <= 8) {
                    val sb = StringBuilder()
                    for (i in unmasked.indices) {
                        sb.append(unmasked[i])
                        if ((i == 1 || i == 3) && i != unmasked.length - 1) {
                            sb.append("/")
                        }
                    }
                    formatted = sb.toString()
                } else {
                    formatted = oldString // Mantém o anterior se passar de 10 chars
                }

                isUpdating = true
                s?.replace(0, s.length, formatted)
                isUpdating = false

                // Sincronizar com o Calendário apenas se a data estiver completa (10 caracteres)
                if (formatted.length == 10) {
                    try {
                        val date = sdf.parse(formatted)
                        if (date != null) {
                            calendarView.date = date.time
                            atualizarTextoMesAno(date.time)
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@TelaRF33CalendarioPublicacao, "Data inválida", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })

        // Navegação por setas (Muda o mês no calendário)
        btnAnterior.setOnClickListener { mudarMes(-1) }
        btnProximo.setOnClickListener { mudarMes(1) }

        btnConfirmar.setOnClickListener {
            val dataFinal = etData.text.toString()
            try {
                val date = sdf.parse(dataFinal)
                if (date != null) {
                    val resultIntent = Intent()
                    resultIntent.putExtra("dataSelecionada", dataFinal)
                    setResult(RESULT_OK, resultIntent)
                    finish()
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Informe uma data válida (dd/mm/aaaa)", Toast.LENGTH_SHORT).show()
            }
        }

        btnVoltar.setOnClickListener { finish() }
    }

    private fun configurarDataPadrao() {
        val dataAtual = calendarView.date
        etData.setText(sdf.format(Date(dataAtual)))
        atualizarTextoMesAno(dataAtual)
    }

    private fun mudarMes(quantidade: Int) {
        val cal = Calendar.getInstance()
        cal.timeInMillis = calendarView.date
        cal.add(Calendar.MONTH, quantidade)
        calendarView.date = cal.timeInMillis
        
        // Ao mudar via seta, atualizamos o EditText com o dia correspondente no novo mês
        etData.setText(sdf.format(cal.time))
        atualizarTextoMesAno(cal.timeInMillis)
    }

    private fun atualizarTextoMesAno(timeInMillis: Long) {
        tvMesAno.text = sdfMesAno.format(Date(timeInMillis)).replaceFirstChar { it.uppercase() }
    }
}