package com.example.bibliounifornew.usuario

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R

class TelaRF19Solicitacoes : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf19_solicitacoes)

        // Recebe o ID do livro da tela anterior (RF12)
        val livroId = intent.getStringExtra("LIVRO_ID") ?: ""

        val btnPdf      = findViewById<Button>(R.id.buttonSolicitarPdf)
        val btnBraile   = findViewById<Button>(R.id.buttonSolicitarBraille)
        val btnAudio    = findViewById<Button>(R.id.buttonSolicitarAudiobook)
        val btnReservar = findViewById<Button>(R.id.buttonReservarLivro)
        val btnSetor    = findViewById<Button>(R.id.buttonSetorLocalizado)

        // Cada botão navega para Termos passando o tipo correto e o livroId
        fun irParaTermos(tipoMidia: String) {
            startActivity(
                Intent(this, TelaRF19SolicitacoesTermosCondicoes::class.java)
                    .putExtra("TIPO_MIDIA", tipoMidia)
                    .putExtra("LIVRO_ID",   livroId)
            )
        }

        btnPdf?.setOnClickListener      { irParaTermos("PDF")       }
        btnBraile?.setOnClickListener   { irParaTermos("Braille")   }
        btnAudio?.setOnClickListener    { irParaTermos("Audiobook") }
        btnReservar?.setOnClickListener { irParaTermos("Reserva")   }

        btnSetor?.setOnClickListener { showPopupSetor() }
    }

    private fun showPopupSetor() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_setor_localizado)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnVoltar = dialog.findViewById<Button>(R.id.buttonVoltarSetor)
        btnVoltar.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }
}
