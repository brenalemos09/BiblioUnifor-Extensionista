package com.example.bibliounifornew.data

import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

class BibliotecaOnlineRepository {

    private val firestore = FirebaseFirestore.getInstance()

    // Inicializa o serviço do Google Books
    private val googleBooksService = Retrofit.Builder()
        .baseUrl("https://www.googleapis.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(GoogleBooksService::class.java)

    // Busca online e salva no Firestore
    suspend fun buscarEImportarLivro(termoDeBusca: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        try {
            // A sua chave VIP da Google
            val minhaChaveApi = "AIzaSyAEojGm94sofbQ2ZRnlPjVPiKtrQKeqDE4"

            val resposta = googleBooksService.buscarLivros(termoDeBusca, minhaChaveApi)

            // A MÁGICA MUDA AQUI: Pega os 5 primeiros resultados (ou menos, se a API retornar poucos)
            val listaLivrosApi = resposta.items?.take(5)

            if (!listaLivrosApi.isNullOrEmpty()) {
                var livrosProcessados = 0
                val totalLivros = listaLivrosApi.size

                // Faz um loop por cada um dos 5 livros
                listaLivrosApi.forEach { livroApi ->
                    val info = livroApi.volumeInfo
                    val tituloEncontrado = info.title ?: "Título Desconhecido"

                    // ESCUDO ANTI-DUPLICATAS individual para CADA livro
                    firestore.collection("livros")
                        .whereEqualTo("titulo", tituloEncontrado)
                        .get()
                        .addOnSuccessListener { querySnapshot ->

                            if (querySnapshot.isEmpty) {
                                // O livro NÃO existe, vamos salvar!
                                val isbnEncontrado = info.industryIdentifiers?.find { it.type == "ISBN_13" }?.identifier
                                    ?: info.industryIdentifiers?.firstOrNull()?.identifier
                                    ?: "Não informado"

                                val dadosLivro = hashMapOf(
                                    "titulo" to tituloEncontrado,
                                    "autor" to (info.authors?.joinToString(", ") ?: "Autor Desconhecido"),
                                    "genero" to (info.categories?.joinToString(", ") ?: "Gênero Desconhecido"),
                                    "isbn" to isbnEncontrado,
                                    "descricao" to (info.description ?: "Sem descrição disponível."),
                                    "coverUrl" to (info.imageLinks?.thumbnail?.replace("http://", "https://") ?: "")
                                )

                                firestore.collection("livros")
                                    .add(dadosLivro)
                                    .addOnCompleteListener {
                                        livrosProcessados++
                                        // Só recarrega a tela quando o último livro dos 5 terminar de salvar
                                        if (livrosProcessados == totalLivros) onSuccess()
                                    }
                            } else {
                                // O livro JÁ EXISTE, ignora e avança a contagem
                                livrosProcessados++
                                if (livrosProcessados == totalLivros) onSuccess()
                            }
                        }
                        .addOnFailureListener {
                            // Se der erro na checagem de um, avança a contagem para não travar os outros
                            livrosProcessados++
                            if (livrosProcessados == totalLivros) onSuccess()
                        }
                }
            } else {
                onFailure(Exception("Nenhum livro localizado para a busca informada."))
            }
        } catch (e: Exception) {
            onFailure(e)
        }
    }
}