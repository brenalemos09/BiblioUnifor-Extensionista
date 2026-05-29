package com.example.bibliounifornew.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class LivroRepository(
    private val livroDao  : LivroDao,
    private val firestore : FirebaseFirestore
) {

    fun buscarTodosLivros(): Flow<List<EntidadeLivro>> = livroDao.buscarTodosLivros()

    /**
     * Sincroniza os primeiros 50 livros do Firestore com o cache Room local.
     *
     * Garantias contra Jank (Issue #14):
     *   1. Toda a operação roda em Dispatchers.IO — nunca bloqueia a UI Thread.
     *   2. .limit(50) impede o download de 150+ documentos de uma vez.
     *   3. Cada documento é convertido em try-catch individual:
     *      um doc corrompido não mata os outros 49 (falha silenciosa + Log.e).
     *   4. Log.e com e + e.printStackTrace() expõe qual campo causa o crash
     *      de parsing para facilitar depuração.
     */
    suspend fun sincronizarLivrosDoFirestore() = withContext(Dispatchers.IO) {
        try {
            Log.d("LivroRepository", "Iniciando sincronização (limit=50)…")

            val snapshot = firestore.collection("livros")
                .limit(50)   // Issue #14: sem limit, 150+ docs causam Jank/GC pressure
                .get()
                .await()

            Log.d("LivroRepository", "Documentos recebidos: ${snapshot.size()}")

            var sincronizados = 0
            var corrompidos   = 0

            for (documento in snapshot.documents) {
                try {
                    // Mapeia com try-catch individual — doc corrompido não cancela os outros
                    val estoque = (documento.getLong("estoque")
                        ?: documento.getLong("quantidade")
                        ?: documento.getLong("stock")
                        ?: 0L).toInt()

                    val livro = EntidadeLivro(
                        id            = documento.id,
                        title         = documento.getString("title")       ?: documento.getString("titulo")        ?: "",
                        author        = documento.getString("author")      ?: documento.getString("autor")         ?: "",
                        description   = documento.getString("description") ?: documento.getString("descricao")     ?: "",
                        category      = documento.getString("category")    ?: documento.getString("categoria")     ?: "",
                        coverUrl      = documento.getString("coverUrl")    ?: "",
                        stockQuantity = estoque,
                        rating        = documento.getDouble("rating")?.toFloat() ?: 0f,
                        language      = documento.getString("language")    ?: documento.getString("lingua")        ?: "",
                        publisher     = documento.getString("publisher")   ?: documento.getString("editora")       ?: "",
                        dimensions    = documento.getString("dimensions")  ?: documento.getString("dimensoes")     ?: "",
                        isbn10        = documento.getString("isbn10")      ?: documento.getString("isbn_10")       ?: "",
                        isbn13        = documento.getString("isbn13")      ?: documento.getString("isbn_13")       ?: "",
                        asin          = documento.getString("asin")        ?: documento.getString("ASIN")          ?: "",
                        publishDate   = documento.getString("publishDate") ?: documento.getString("dataPublicacao") ?: "",
                        totalPages    = (documento.getLong("totalPages")   ?: documento.getLong("paginas") ?: 0L).toInt(),
                        hasPdf        = documento.getBoolean("hasPdf")     ?: documento.getBoolean("temPdf")       ?: false,
                        hasBraille    = documento.getBoolean("hasBraille") ?: documento.getBoolean("temBraille")   ?: false,
                        hasAudiobook  = documento.getBoolean("hasAudiobook") ?: documento.getBoolean("temAudiobook") ?: false,
                        linkPdf       = documento.getString("linkPdf")     ?: "",
                        linkAudiobook = documento.getString("linkAudiobook") ?: "",
                        librarySector = documento.getString("librarySector") ?: documento.getString("setor")       ?: "",
                        isAvailable   = estoque > 0,
                        isFavorite    = documento.getBoolean("isFavorite") ?: false
                    )
                    livroDao.inserirLivro(livro)
                    sincronizados++

                } catch (docEx: Exception) {
                    // Documento corrompido ou com tipo inesperado num campo — loga e ignora
                    corrompidos++
                    Log.e("LivroRepository",
                        "Documento corrompido ignorado [id=${documento.id}]: ${docEx.message}")
                    docEx.printStackTrace()
                }
            }

            Log.d("LivroRepository",
                "Sincronização concluída: $sincronizados OK | $corrompidos corrompidos ignorados.")

        } catch (e: Exception) {
            // Falha de rede ou permissão — expõe causa completa para debug
            Log.e("LivroRepository", "Erro ao sincronizar: ${e.message}", e)
            e.printStackTrace()
        }
    }

    suspend fun buscarLivroPorId(id: String): EntidadeLivro? = livroDao.buscarLivroPorId(id)

    fun pesquisarLivrosLocais(query: String): Flow<List<EntidadeLivro>> =
        livroDao.pesquisarLivros("%$query%")
}
