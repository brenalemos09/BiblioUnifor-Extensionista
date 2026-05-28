package com.example.bibliounifornew.features.usuario.livro

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.example.bibliounifornew.R
import android.os.Handler
import android.os.Looper

class TelaRF13VerMaisLivro : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf13_telavermaislivro)

        val livroId = intent.getStringExtra("LIVRO_ID")
        if (livroId.isNullOrEmpty()) {
            Toast.makeText(this, "ID do livro não encontrado.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        carregarDadosDetalhadosMock(livroId)
    }

    private fun carregarDadosDetalhadosMock(id: String) {
        // Simulação de delay de rede
        Handler(Looper.getMainLooper()).postDelayed({
            // Mock de dados baseado no ID
            val sem = "Sem informação"
            
            // Dados estáticos para o protótipo
            val titulo = when(id) {
                "1" -> "O Senhor dos Anéis"
                "2" -> "Cálculo Volume 1"
                else -> "Livro Exemplo Prototype"
            }
            
            val autor = when(id) {
                "1" -> "J.R.R. Tolkien"
                "2" -> "James Stewart"
                else -> "Autor Desconhecido"
            }

            val desc = "Esta é uma descrição detalhada simulada para o protótipo acadêmico do livro $titulo. O sistema agora opera de forma local para demonstração de requisitos de interface."
            val cover = "" // Coil usará o placeholder

            findViewById<TextView>(R.id.textTituloLivroInfo)?.text = titulo
            findViewById<TextView>(R.id.textAutorLivroInfo)?.text  = autor
            findViewById<TextView>(R.id.textDescricaoLivro)?.text  = desc

            val imgCapa = findViewById<ImageView>(R.id.imageLivroInfo)
            imgCapa?.load(R.drawable.osda) { 
                placeholder(R.drawable.osda)
                error(R.drawable.osda) 
            }

            // Preenchimento de campos técnicos com dados mock fixos
            findViewById<TextView>(R.id.textValorLingua)?.text = "Português"
            findViewById<TextView>(R.id.textValorEditora)?.text = "Editora Acadêmica"
            findViewById<TextView>(R.id.textValorDimensao)?.text = "23 x 16 x 3 cm"
            findViewById<TextView>(R.id.textValorIsbn10)?.text = "8535914846"
            findViewById<TextView>(R.id.textValorIsbn13)?.text = "978-8535914849"
            findViewById<TextView>(R.id.textValorAsin)?.text = "B00XJY"
            findViewById<TextView>(R.id.textValorData)?.text = "01/01/2023"
            findViewById<TextView>(R.id.textValorPdf)?.text = "Sim"
            findViewById<TextView>(R.id.textValorBraille)?.text = "Não"
            findViewById<TextView>(R.id.textValorAudiobook)?.text = "Sim"
            findViewById<TextView>(R.id.textValorPaginas)?.text = "450"
            findViewById<TextView>(R.id.textValorSetor)?.text = "A1 - Literatura"

        }, 500)
    }
}
