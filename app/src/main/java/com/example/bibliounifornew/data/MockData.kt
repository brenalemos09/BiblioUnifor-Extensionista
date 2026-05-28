package com.example.bibliounifornew.data

object MockData {
    val livros = listOf(
        EntidadeLivro(
            id = "1",
            title = "O Senhor dos Anéis",
            author = "J.R.R. Tolkien",
            description = "Uma aventura épica em um mundo de fantasia repleto de hobbits, magos e anéis de poder.",
            category = "Fantasia",
            publishDate = "1954"
        ),
        EntidadeLivro(
            id = "2",
            title = "Harry Potter",
            author = "J.K. Rowling",
            description = "A história de um jovem bruxo que descobre seu destino em uma escola de magia.",
            category = "Fantasia",
            publishDate = "1997"
        ),
        EntidadeLivro(
            id = "3",
            title = "Clean Code",
            author = "Robert C. Martin",
            description = "Um guia essencial para escrever código limpo, legível e de fácil manutenção.",
            category = "Tecnologia",
            publishDate = "2008"
        ),
        EntidadeLivro(
            id = "4",
            title = "Dom Casmurro",
            author = "Machado de Assis",
            description = "Um dos maiores clássicos da literatura brasileira, explorando temas de ciúme e ambiguidade.",
            category = "Literatura Brasileira",
            publishDate = "1899"
        ),
        EntidadeLivro(
            id = "5",
            title = "Pequeno Príncipe",
            author = "Antoine de Saint-Exupéry",
            description = "Uma fábula poética sobre amizade, amor e as coisas simples que são invisíveis aos olhos.",
            category = "Infantil",
            publishDate = "1943"
        )
    )
}
