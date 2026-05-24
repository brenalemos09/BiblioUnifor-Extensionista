# Prompt — Claude Code | Projeto BiblioUnifor (Android Kotlin)

## Papel

Atue como Desenvolvedor Android Sênior (Kotlin). Você vai implementar uma tarefa
pendente e revisar integridade de arquivos já refatorados neste projeto Android.
Siga o padrão arquitetural já estabelecido: Repository Pattern, Firebase Auth +
Cloud Firestore, RecyclerView + Adapter, sem ViewModel onde não há necessidade.

---

## Contexto do Projeto

- **Diretório:** `D:\UNIFOR\CienciaComputacao\2026.01_Semestre03\T197-12_DesenvPlatMoveis\BiblioUniforGrupo4\BiblioUnifor_DEV_AB`
- **Pacote base:** `com.example.bibliounifornew`
- **SDK min/target:** Android (Kotlin), AndroidX
- **Dependências-chave:**
  - Firebase BoM 33.10.0 (Auth + Firestore)
  - Material Components 1.12.0
  - RecyclerView + ConstraintLayout
  - Coil 2.7.0 (carregamento de imagens)
  - Room 2.8.4 com KSP

### Estrutura de packages relevante

```
app/src/main/java/com/example/bibliounifornew/
├── data/
│   ├── AuthRepository.kt
│   ├── UsuarioRepository.kt      ← repositório central, já refatorado
│   ├── LivroRepository.kt
│   └── Notificacao.kt            ← data class criada nesta sessão
└── usuario/
    ├── AmigoAdapter.kt           ← criado nesta sessão
    ├── NotificacaoAdapter.kt     ← criado nesta sessão
    ├── TelaRF09Configuracao.kt   ← refatorado
    ├── TelaRF12TelaDoLivro.kt    ← crash fix aplicado
    ├── TelaRF16ListaDesejosActivity.kt ← refatorado
    ├── TelaRF17Amigos.kt         ← refatorado
    ├── TelaRF20Notificacoes.kt   ← refatorado
    └── TelaRF21Historico.kt      ← parcialmente refatorado (ver tarefa abaixo)
```

### Coleções Firestore estabelecidas

| Collection | Documento | Descrição |
|---|---|---|
| `usuarios` | `{uid}` | Perfil do usuário (nome, usuario, biografia) |
| `biblioteca_usuarios` | `{uid}_{livroId}` | Livraria pessoal |
| `historico_usuarios` | `{uid}_{livroId}` | Histórico de leitura |
| `lista_desejos` | `{uid}_{livroId}` | Lista de desejos |

### Métodos já existentes em `UsuarioRepository`

```kotlin
// Listener em tempo real — retorna ListenerRegistration (chamar .remove() em onDestroy)
fun observarPerfilUsuario(uid: String, onChange: (Map<String, Any>?) -> Unit): ListenerRegistration

// Salva campos parciais com SetOptions.merge()
fun salvarPerfilCompleto(uid: String, campos: Map<String, Any>, onComplete: (Boolean, String?) -> Unit)

// Deleta documento em historico_usuarios/{uid}_{livroId}
fun removerDoHistorico(uid: String, livroId: String, onComplete: (Boolean) -> Unit)

// Salva em lista_desejos/{uid}_{livroId}
fun salvarListaDesejos(uid: String, livroId: String, dados: Map<String, Any>, onComplete: (Boolean, String?) -> Unit)

// Deleta de lista_desejos/{uid}_{livroId}
fun removerDaListaDesejos(uid: String, livroId: String, onComplete: (Boolean) -> Unit)
```

---

## O que já foi feito (NÃO refaça)

| RF | Arquivo | Status |
|---|---|---|
| RF20 | `telarf20_notificacoes.xml` + `TelaRF20Notificacoes.kt` + `NotificacaoAdapter.kt` + `item_notificacao.xml` + `Notificacao.kt` | ✅ Concluído |
| RF09 | `telarf09_configuracao.xml` + `TelaRF09Configuracao.kt` | ✅ Concluído |
| RF15→RF12 | `TelaRF12TelaDoLivro.kt` (crash fix) | ✅ Concluído |
| RF16 | `telarf16_lista_desejos.xml` + `TelaRF16ListaDesejosActivity.kt` | ✅ Concluído |
| RF17 | `telarf17_amigos.xml` + `AmigoAdapter.kt` + `TelaRF17Amigos.kt` | ✅ Concluído |
| RF21 (parcial) | `TelaRF21Historico.kt` — botões já chamam `removerDoHistorico()` | ✅ Parcial |

---

## Tarefa Pendente — RF21: Migrar Histórico para RecyclerView

### Contexto

`TelaRF21Historico.kt` e `telarf21_historico.xml` ainda usam **cards estáticos** hardcoded
no XML (2 livros fixos). Isso não escala. A tela precisa ser migrada para RecyclerView
consultando o Firestore em tempo real.

### O que fazer

#### 1. Criar `data class ItemHistorico`

Pode ser declarada no topo de `HistoricoAdapter.kt` ou em arquivo separado em `data/`.

```kotlin
data class ItemHistorico(
    val livroId    : String = "",
    val titulo     : String = "",
    val autor      : String = "",
    val dataLido   : Long   = 0L   // timestamp para ordenar por mais recente
)
```

#### 2. Criar `HistoricoAdapter.kt` em `usuario/`

- Usar `item_historico.xml` (verifique se já existe; se não existir, crie baseando-se
  no estilo de `item_notificacao.xml` — card com cornerRadius 24dp, elevation 3dp)
- IDs esperados no item layout: `txtTituloHistorico`, `txtAutorHistorico`,
  `txtDataHistorico`, `btnRemoverHistorico` (MaterialButton)
- Implementar `fun removerItem(position: Int)`:
  ```kotlin
  fun removerItem(position: Int) {
      lista.removeAt(position)
      notifyItemRemoved(position)
      notifyItemRangeChanged(position, lista.size)
  }
  ```
- O clique no `btnRemoverHistorico` deve disparar um callback `onRemover: (ItemHistorico, Int) -> Unit`
  passado no construtor do adapter — **não** tratar lógica Firestore dentro do adapter.

#### 3. Refatorar `telarf21_historico.xml`

- Remover todos os cards estáticos (`cardLivro1Historico`, `cardStatus1Historico`,
  `cardLivro2Historico`, `cardStatus2Historico`) e seus botões
- Manter o header existente (sem alterações)
- Adicionar `RecyclerView` com `android:id="@+id/recyclerViewHistorico"`,
  `android:layout_height="0dp"`, constraints: `top_toBottomOf` do header,
  `bottom_toBottomOf="parent"`, `paddingBottom="100dp"`, `clipToPadding="false"`

#### 4. Refatorar `TelaRF21Historico.kt`

```kotlin
// Dependências a adicionar
private val usuarioRepository = UsuarioRepository()
private val db                = FirebaseFirestore.getInstance()
private lateinit var adapter  : HistoricoAdapter
private val listaHistorico    = mutableListOf<ItemHistorico>()
private var usuarioId         : String = ""

// Query no Firestore com ordenação por data decrescente
db.collection("historico_usuarios")
    .whereEqualTo("usuarioId", usuarioId)
    .orderBy("adicionadoEm", Query.Direction.DESCENDING)
    .get()
    .addOnSuccessListener { result -> ... }

// Callback do adapter: ao confirmar remoção no popup, chamar:
usuarioRepository.removerDoHistorico(usuarioId, item.livroId) { sucesso ->
    if (sucesso) {
        adapter.removerItem(position)   // notifyItemRemoved já está dentro
    } else {
        Toast.makeText(this, "Falha ao remover. Tente novamente.", Toast.LENGTH_SHORT).show()
    }
}
```

- Manter o popup de confirmação (`showPopupRemover`) igual ao existente, mas agora
  chamado a partir do callback do adapter com o `ItemHistorico` e o `position` corretos.
- Remover as referências antigas a `btnRemoverHistorico`, `buttonRemoverHistorico2`,
  `cardLivro1Historico`, `cardStatus1Historico`, `cardLivro2Historico`, `cardStatus2Historico`.

> **ATENÇÃO:** A query `.whereEqualTo(...).orderBy(...)` exige índice composto no Firestore.
> Se o índice não existir, o Firestore vai lançar `FirebaseFirestoreException` com um link
> no Logcat para criá-lo automaticamente. Trate essa exceção no `addOnFailureListener`
> com Toast explicativo: `"Histórico indisponível. Configure o índice no Firestore Console."`.

---

## Verificações finais após implementar

1. Confirme que `item_historico.xml` existe em `res/layout/` com os IDs corretos
2. Confirme que `HistoricoAdapter` está no pacote `com.example.bibliounifornew.usuario`
3. Confirme que `TelaRF21Historico` não tem mais referências a IDs que não existem no XML
4. Confirme que não há `ListenerRegistration` aberto sem `.remove()` em `onDestroy()`
   (neste caso a query é one-shot `.get()`, então não há listener contínuo — OK)
5. Compile o projeto e resolva quaisquer erros de importação

---

## Padrões que DEVEM ser seguidos

- Nenhuma lógica de negócio dentro de Adapters — apenas binding de dados e eventos UI
- Firestore IDs de documentos seguem o padrão `{uid}_{livroId}` (snake_case, sem espaços)
- Nunca usar `usuarioFantasmaId` ou IDs hardcoded — sempre `authRepository.getUsuarioAtual()?.uid`
- `ListenerRegistration` deve ser armazenado em propriedade e cancelado em `onDestroy()`
- Usar `SetOptions.merge()` em writes parciais — nunca `.set()` puro (sobrescreve o documento)
