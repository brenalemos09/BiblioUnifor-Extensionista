package com.example.bibliounifornew.features.adm.gerenciamento

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R
import com.google.android.material.button.MaterialButton

class TelaRF33AdicionarMidiasExtras : AppCompatActivity() {

    private var livroId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf33_adicionar_midia_extras)

        // Recebe o ID do documento criado no Passo 1
        livroId = intent.getStringExtra("LIVRO_ID") ?: ""

        val etPaginas   = findViewById<EditText>(R.id.editQuantidadePaginas)
        val etCategoria = findViewById<EditText>(R.id.editCategoriaLivro)
        val etEditora   = findViewById<EditText>(R.id.editEditoraLivro)
        val etLinkCapa  = findViewById<EditText>(R.id.editLinkImagem)
        val etSinopse   = findViewById<EditText>(R.id.editSinopse)
        val btnAvancar  = findViewById<MaterialButton>(R.id.btnEditarMaisInformacoes2)

        btnAvancar.setOnClickListener {
            val paginas   = etPaginas.text.toString().trim()
            val categoria = etCategoria.text.toString().trim()
            val editora   = etEditora.text.toString().trim()
            val sinopse   = etSinopse.text.toString().trim()
            val linkCapa  = etLinkCapa.text.toString().trim()   // opcional

            if (paginas.isEmpty() || categoria.isEmpty() || editora.isEmpty() || sinopse.isEmpty()) {
                Toast.makeText(this, getString(R.string.erro_preencha_infos_extras), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnAvancar.isEnabled = false

            // Protótipo: Simulação de sucesso
            btnAvancar.postDelayed({
                btnAvancar.isEnabled = true
                avancarParaArquivos()
            }, 500)
        }
    }

    private fun avancarParaArquivos() {
        val intent = Intent(this, TelaRF33AdicionarMidiaArquivos::class.java)
        intent.putExtra("LIVRO_ID", livroId)   // BUG-C2 FIX: propaga o ID para o Passo 3
        startActivity(intent)
    }
}
