package com.example.bibliounifornew.adm

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R
import com.google.android.material.button.MaterialButton

class TelaRF33AdicionarMidiasExtras : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf33_adicionar_midia_extras)

        val etPaginas = findViewById<EditText>(R.id.editQuantidadePaginas)
        val etCategoria = findViewById<EditText>(R.id.editCategoriaLivro)
        val etEditora = findViewById<EditText>(R.id.editEditoraLivro)
        val etLinkCapa = findViewById<EditText>(R.id.editLinkImagem)
        val etSinopse = findViewById<EditText>(R.id.editSinopse)
        val btnAvancar = findViewById<MaterialButton>(R.id.btnEditarMaisInformacoes2)

        btnAvancar.setOnClickListener {
            val paginas = etPaginas.text.toString().trim()
            val categoria = etCategoria.text.toString().trim()
            val editora = etEditora.text.toString().trim()
            val sinopse = etSinopse.text.toString().trim()

            // Validação de campos obrigatórios extras
            if (paginas.isEmpty() || categoria.isEmpty() || editora.isEmpty() || sinopse.isEmpty()) {
                Toast.makeText(this, "Preencha todas as informações extras", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, TelaRF33AdicionarMidiaArquivos::class.java)
                startActivity(intent)
            }
        }
    }
}