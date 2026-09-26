# Como o UniDo funciona

Este documento explica, arquivo por arquivo, cada classe e função do aplicativo **UniDo**, um gerenciador de tarefas acadêmicas feito em Kotlin com Jetpack Compose, Room e arquitetura MVVM.

---

## 1. Visão geral

### Arquitetura em camadas (MVVM)

```
┌──────────────────────────────────────────────┐
│ UI (Jetpack Compose)                         │
│  TaskListScreen  ·  TaskDetailsScreen        │
│        ▲ observa estado     │ chama ações    │
└────────┼────────────────────┼────────────────┘
         │                    ▼
┌──────────────────────────────────────────────┐
│ ViewModel                                    │
│  TaskViewModel (StateFlow<List<Task>>)       │
└────────▲────────────────────┬────────────────┘
         │ Flow               ▼ suspend
┌──────────────────────────────────────────────┐
│ Repository                                   │
│  TaskRepository                              │
└────────▲────────────────────┬────────────────┘
         │ Flow               ▼ suspend
┌──────────────────────────────────────────────┐
│ Room (SQLite local: unido.db)                │
│  TaskDao · TaskDatabase · Task (entidade)    │
└──────────────────────────────────────────────┘
```

- **UI**: desenha as telas e repassa as ações do usuário (clicar, digitar) para o ViewModel.
- **ViewModel**: guarda o estado da tela e aplica as regras de apresentação, como valores padrão e validação do título.
- **Repository**: é o único ponto de acesso aos dados. A UI e o ViewModel não falam direto com o banco.
- **Room**: é a persistência local. Os dados continuam salvos depois que o app é fechado.

### Estrutura de arquivos

```
app/src/main/java/com/example/unido/
├── UniDoApplication.kt
├── MainActivity.kt
├── data/
│   ├── local/
│   │   ├── Task.kt
│   │   ├── TaskDao.kt
│   │   └── TaskDatabase.kt
│   └── repository/
│       └── TaskRepository.kt
├── viewmodel/
│   └── TaskViewModel.kt
└── ui/
    ├── navigation/UniDoNavHost.kt
    ├── theme/Theme.kt
    ├── list/TaskListScreen.kt
    └── details/TaskDetailsScreen.kt
```

### Ciclo de vida: da abertura do app até a primeira tela

1. O Android cria a `UniDoApplication`, que está registrada no `AndroidManifest.xml`.
2. O Android abre a `MainActivity`, que é a activity LAUNCHER.
3. `MainActivity.onCreate` pega o `TaskRepository` da `UniDoApplication`. Na primeira vez, isso cria o banco.
4. `setContent` aplica o `UniDoTheme`, cria o `TaskViewModel` e chama o `UniDoNavHost`.
5. O `UniDoNavHost` começa na rota `"list"` e mostra a `TaskListScreen`.
6. A `TaskListScreen` passa a observar `viewModel.tasks`. O Room emite a lista atual e a tela é desenhada.

---

## 2. Inicialização

### `UniDoApplication.kt`

```kotlin
class UniDoApplication : Application() {
    val repository: TaskRepository by lazy { ... }
}
```

| Membro | O que faz |
|---|---|
| `class UniDoApplication` | Subclasse de `Application`. O Android cria uma única instância dela quando o processo do app inicia, antes de qualquer tela. Ela é registrada no manifest com `android:name=".UniDoApplication"`. |
| `val repository` | Cria o `TaskRepository` de forma **preguiçosa** (`by lazy`), ou seja, só no primeiro acesso. Ele pega o banco com `TaskDatabase.getInstance(this)` e passa o `taskDao()` para o repositório. Assim o app inteiro compartilha um único repositório. Isso é uma forma simples de *injeção manual de dependências*. |

### `MainActivity.kt`

| Membro | O que faz |
|---|---|
| `class MainActivity : ComponentActivity` | É a única tela nativa do app. Todas as "telas" que o usuário vê são composables desenhados dentro dela. |
| `onCreate(savedInstanceState)` | É chamada quando a activity é criada. Passo a passo: 1) `enableEdgeToEdge()` faz o conteúdo ocupar a tela inteira, por trás das barras do sistema; 2) pega o `repository` da `UniDoApplication`; 3) `setContent { ... }` define a interface em Compose: aplica o `UniDoTheme`, cria o `TaskViewModel` com `viewModel(factory = TaskViewModel.factory(repository))` e mostra o `UniDoNavHost`. |

Como o ViewModel é criado com `viewModel(...)` no nível da activity, **a mesma instância é compartilhada pelas duas telas**. Ela também sobrevive a rotações de tela.

---

## 3. Camada de dados (`data/`)

### `data/local/Task.kt` — entidade

```kotlin
@Entity(tableName = "Tarefa")
data class Task(...)
```

Representa uma tarefa e, ao mesmo tempo, uma linha da tabela no banco.

| Campo | Tipo | Coluna no banco | Significado |
|---|---|---|---|
| `id` | `Int` | `id` | Chave primária. `autoGenerate = true` faz o Room gerar o valor (1, 2, 3...). O padrão `0` quer dizer "ainda não salvo". |
| `title` | `String` | `titulo` | Título da tarefa. É obrigatório. |
| `subject` | `String` | `disciplina` | Disciplina ou matéria. |
| `createdBy` | `String` | `criadoPor` | Autor. Hoje é sempre `"Criado pelo aluno"`. |
| `dateTime` | `String` | `dataHora` | Data e hora do prazo, como **texto livre**. Não há validação de formato. |
| `description` | `String` | `descricao` | Descrição longa. |
| `completed` | `Boolean` | `concluida` | Se a tarefa já foi concluída. O padrão é `false`. |

Os nomes da tabela e das colunas ficaram em português (`@Entity(tableName = ...)` e `@ColumnInfo(name = ...)`) para manter compatível o banco de quem já tinha o app instalado antes da refatoração para inglês.

Por ser uma `data class`, a `Task` ganha `copy(...)` automaticamente. O ViewModel usa isso para criar uma versão modificada, por exemplo `task.copy(completed = true)`.

### `data/local/TaskDao.kt` — acesso ao banco

O DAO (*Data Access Object*) é uma interface. O Room gera a implementação dela em tempo de compilação, via KSP.

| Função | SQL / anotação | Retorno | O que faz |
|---|---|---|---|
| `getAllTasks()` | `SELECT * FROM Tarefa ORDER BY id DESC` | `Flow<List<Task>>` | Retorna todas as tarefas, das mais novas para as mais antigas. Por ser um `Flow`, **emite uma lista nova automaticamente sempre que a tabela muda** (inserção, atualização ou exclusão). É isso que mantém a tela sempre atualizada. |
| `getById(id)` | `SELECT * FROM Tarefa WHERE id = :id LIMIT 1` | `Task?` | Busca uma única tarefa pelo id. Retorna `null` se ela não existir. É `suspend` e roda uma vez, sem observar mudanças. |
| `insert(task)` | `@Insert` | — | Insere uma nova linha. O `id` é gerado pelo banco. |
| `update(task)` | `@Update` | — | Atualiza a linha cujo `id` é igual ao de `task`, sobrescrevendo todas as colunas. |
| `delete(task)` | `@Delete` | — | Remove a linha cujo `id` é igual ao de `task`. |

As funções `suspend` precisam ser chamadas dentro de uma coroutine. O Room as executa fora da thread principal, o que evita travar a interface.

### `data/local/TaskDatabase.kt` — o banco

```kotlin
@Database(entities = [Task::class], version = 1, exportSchema = false)
abstract class TaskDatabase : RoomDatabase()
```

| Membro | O que faz |
|---|---|
| `@Database(...)` | Declara que o banco tem uma tabela (`Task`) e está na versão 1. `exportSchema = false` desliga a exportação do esquema em JSON. |
| `abstract fun taskDao()` | O Room gera a implementação e devolve o DAO. |
| `INSTANCE` | Guarda a única instância do banco. `@Volatile` garante que todas as threads vejam o valor mais recente. |
| `getInstance(context)` | Implementa um **singleton** com *double-checked locking*. Se `INSTANCE` já existe, ela é devolvida. Se não, entra num bloco `synchronized`, confere de novo e cria o banco com `Room.databaseBuilder(..., "unido.db")`. Isso garante que haja um único banco aberto, mesmo com várias threads chamando ao mesmo tempo. O arquivo fica salvo em `/data/data/com.example.unido/databases/unido.db`. |

### `data/repository/TaskRepository.kt`

Faz a intermediação entre o ViewModel e o DAO. Hoje ele só repassa as chamadas, mas é o lugar certo para, no futuro, juntar outras fontes de dados, como uma API.

| Membro | O que faz |
|---|---|
| `val tasks` | É o `Flow<List<Task>>` vindo de `dao.getAllTasks()`. |
| `insert(task)` | Chama `dao.insert`. |
| `update(task)` | Chama `dao.update`. |
| `delete(task)` | Chama `dao.delete`. |
| `getById(id)` | Chama `dao.getById`. |

---

## 4. ViewModel (`viewmodel/TaskViewModel.kt`)

Guarda o estado usado pelas telas e expõe as ações que o usuário pode fazer.

| Membro | O que faz |
|---|---|
| `val tasks: StateFlow<List<Task>>` | Converte o `Flow` do repositório em `StateFlow` com `stateIn(...)`. **Valor inicial**: `emptyList()`, usado até o banco responder. **`SharingStarted.WhileSubscribed(5_000)`**: o banco só é observado enquanto alguma tela estiver coletando. Se ninguém coletar por 5 segundos, a observação para. Os 5 segundos evitam reiniciar a consulta à toa durante uma rotação de tela. |
| `addTask(title, subject, dateTime, description)` | Cria e salva uma nova tarefa. 1) Se `title` estiver em branco, **não faz nada**. 2) Tira os espaços das pontas de todos os campos (`trim()`). 3) Preenche valores padrão para campos vazios: disciplina vira `"Sem disciplina"`, data vira `"Data não informada"` e descrição vira `"Sem descrição"`. 4) Define `createdBy = "Criado pelo aluno"`. 5) Chama `repository.insert` dentro de `viewModelScope.launch`, uma coroutine que é cancelada automaticamente se o ViewModel for destruído. |
| `completeTask(task)` | Marca a tarefa como concluída: salva `task.copy(completed = true)` com `repository.update`. **Não existe a ação inversa** (voltar a tarefa para pendente). |
| `deleteTask(task)` | Exclui a tarefa com `repository.delete`. Não pede confirmação. |
| `suspend fun getById(id)` | Busca uma tarefa pelo id e devolve `Task?`. É `suspend` porque a tela de detalhes a chama dentro de um `LaunchedEffect`, que já é uma coroutine. |
| `companion object factory(repository)` | Cria um `ViewModelProvider.Factory`. É necessário porque o `TaskViewModel` recebe o repositório no construtor, e o Android só sabe criar sozinho ViewModels sem parâmetros. A factory instancia `TaskViewModel(repository)`. |

Nenhuma tela chama a atualização da lista diretamente. Quando `addTask`, `completeTask` ou `deleteTask` alteram o banco, o `Flow` do Room emite a lista nova, o `StateFlow` é atualizado e a tela é redesenhada sozinha.

---

## 5. Interface (`ui/`)

### `ui/theme/Theme.kt`

| Função | O que faz |
|---|---|
| `UniDoTheme(content)` | Aplica o `MaterialTheme` (Material 3, com cores e tipografia padrão) e envolve o conteúdo numa `Surface`, que define a cor de fundo e a cor padrão do texto. Todo o app é desenhado dentro dela. Fica aqui a personalização futura de cores e fontes. |

O arquivo `res/values/themes.xml` define `Theme.UniDo`, o tema **nativo** da activity. Ele controla as barras de status e de navegação brancas e com ícones escuros antes do Compose assumir a tela.

### `ui/navigation/UniDoNavHost.kt`

#### `object Routes` (privado)

| Constante / função | Valor | Uso |
|---|---|---|
| `LIST` | `"list"` | Rota da tela de lista. É a rota inicial. |
| `ARG_ID` | `"id"` | Nome do argumento passado na rota de detalhes. |
| `DETAILS` | `"details/{id}"` | Padrão da rota de detalhes. |
| `NEW_TASK_ID` | `-1` | Id especial que significa "criar nova tarefa". |
| `details(id)` | `"details/$id"` | Monta a rota concreta, como `"details/5"` ou `"details/-1"`. |

#### `UniDoNavHost(viewModel)`

Cria o `NavController` com `rememberNavController()` e declara as duas telas:

1. **`"list"`** mostra a `TaskListScreen`.
   - `onNewTask` navega para `details(-1)`, que abre o formulário de nova tarefa.
   - `onTaskClick(id)` navega para `details(id)`, que abre os detalhes da tarefa.
2. **`"details/{id}"`** mostra a `TaskDetailsScreen`.
   - Lê o argumento `id` como `Int` (`NavType.IntType`). Se não houver argumento, usa `-1`.
   - `onBack` chama `navController.popBackStack()` e volta para a lista.

O mesmo `viewModel` é passado para as duas telas.

### `ui/list/TaskListScreen.kt` — tela inicial

#### `TaskListScreen(viewModel, onNewTask, onTaskClick)`

**Estado:**
- `tasks`: `viewModel.tasks.collectAsState()` transforma o `StateFlow` em estado do Compose. A tela se redesenha quando a lista muda.
- `searchQuery`: o texto digitado no campo de busca, guardado com `remember { mutableStateOf("") }`.
- `filteredTasks`: as tarefas cujo **título ou disciplina** contêm `searchQuery`, sem diferenciar maiúsculas de minúsculas. Com o campo vazio, todas aparecem.

**Layout, de cima para baixo:**
1. `Header()`, com o logo e o slogan.
2. O título "Minhas tarefas".
3. Uma linha com:
   - **Campo de texto**, que funciona como **busca** e atualiza `searchQuery` a cada tecla. O placeholder diz "Nova tarefa", mas digitar ali **não cria** tarefa: só filtra a lista.
   - **Botão "+"** (`Icons.Default.Add`), que chama `onNewTask()`.
   - **Botão de filtro** (`Icons.Default.FilterList`), que é apenas visual: o `onClick` está vazio.
4. Uma faixa cinza com o texto "TAREFAS".
5. **Conteúdo:**
   - Se `filteredTasks` estiver vazia, mostra "Nenhuma tarefa cadastrada." e o botão "Adicionar primeira tarefa", que chama `onNewTask()`. Essa mensagem também aparece quando a busca não encontra nada.
   - Caso contrário, mostra uma `LazyColumn` (lista com rolagem que só desenha os itens visíveis) com um `TaskCard` por tarefa. `key = { it.id }` ajuda o Compose a identificar cada item quando a lista muda.

#### `Header()` (privada)

Mostra o logo `R.drawable.ic_unido_logo` com 76dp. `tint = Color.Unspecified` preserva as cores originais do vetor. Ao lado ficam os textos "UniDo", "Sua rotina acadêmica" e "organizada.".

#### `TaskCard(task, onTaskClick, viewModel)` (privada)

É o cartão de uma tarefa na lista.

| Elemento | Comportamento |
|---|---|
| Cartão inteiro (`clickable`) | Chama `onTaskClick(task.id)` e abre os detalhes. |
| `Checkbox` | Fica marcado se `task.completed`. Marcar chama `viewModel.completeTask(task)`. **Desmarcar não faz nada** (`if (it)`), porque não existe "reabrir tarefa". |
| Coluna do meio | Título em negrito (1 linha no máximo), disciplina e autor. |
| Canto direito | Mostra "CONCLUÍDA" se a tarefa já foi concluída. Se não, mostra `dateTime`. |
| Ícone de lixeira | Chama `viewModel.deleteTask(task)`. A exclusão é imediata e sem confirmação. |

### `ui/details/TaskDetailsScreen.kt` — cadastro e detalhes

Uma única tela com **dois modos**, escolhidos pelo `taskId`:

| `taskId` | Modo |
|---|---|
| `<= 0` (normalmente `-1`) | **Formulário de nova tarefa** |
| `> 0` | **Visualização** de uma tarefa existente (somente leitura) |

#### `TaskDetailsScreen(taskId, viewModel, onBack)`

**Estado:**
- `task: Task?`: a tarefa carregada do banco. Começa como `null`.
- `title`, `subject`, `dateTime`, `description`: os textos do formulário.

**Carregamento, com `LaunchedEffect(taskId)`:** roda uma vez quando a tela abre (e de novo se o `taskId` mudar). Se `taskId > 0`, chama `viewModel.getById(taskId)`, guarda o resultado em `task` e copia os campos para as variáveis do formulário.

**Topo, igual nos dois modos:** um botão de seta ("Voltar"), que chama `onBack()`, e o texto "Retornar à lista".

**Modo 1: nova tarefa (`taskId <= 0`)**
1. Título "Nova tarefa".
2. Quatro `FormField`: Título, Disciplina, Data e hora (texto livre) e Descrição (multilinha).
3. O botão **SALVAR**:
   - fica **desabilitado enquanto o título estiver vazio**;
   - ao clicar, chama `viewModel.addTask(title, subject, dateTime, description)` e depois `onBack()`, voltando para a lista, onde a tarefa nova já aparece no topo.

**Modo 2: tarefa existente, ainda carregando (`task == null`)**
- Mostra "Carregando...". Se o id não existir no banco, a tela fica nesse estado.

**Modo 3: tarefa existente, já carregada**
1. Uma faixa com o status: "Atividade concluída" ou "Atividade pendente".
2. Título, disciplina e autor.
3. Uma faixa com o prazo: "Terminou em ..." (concluída) ou "Termina em ..." (pendente).
4. A seção "Descrição", numa caixa cinza arredondada com no mínimo 6 linhas.
5. Na parte de baixo:
   - se a tarefa estiver pendente, aparece o botão **CONCLUIR**, que chama `viewModel.completeTask(current)`;
   - se estiver concluída, aparece um ícone grande de check.

Não é possível **editar** uma tarefa existente. As variáveis do formulário são preenchidas no carregamento, mas só o modo "nova tarefa" as usa.

#### `FormField(label, value, singleLine, onValueChange)` (privada)

É um campo de texto reutilizável (`OutlinedTextField`), com largura total e espaço embaixo. Com `singleLine = true`, fica com 1 linha. Com `false`, começa com no mínimo 4 linhas, que é o caso da Descrição.

---

## 6. Fluxos completos

### Criar uma tarefa
```
Lista → "+" → onNewTask() → navega para "details/-1"
      → TaskDetailsScreen (modo formulário) → usuário preenche → SALVAR
      → viewModel.addTask(...) → repository.insert → dao.insert (INSERT no SQLite)
      → onBack() volta para a lista
      → Room emite nova lista → StateFlow atualiza → a tarefa aparece no topo
```

### Ver detalhes
```
Lista → clique no cartão → onTaskClick(id) → "details/{id}"
      → LaunchedEffect → viewModel.getById(id) → dao.getById (SELECT)
      → task preenchida → a tela mostra os dados
```

### Concluir uma tarefa
```
Checkbox na lista  ─┐
Botão CONCLUIR     ─┴→ viewModel.completeTask(task)
      → repository.update(task.copy(completed = true)) → UPDATE no SQLite
      → Room emite nova lista → o cartão mostra "CONCLUÍDA" e o checkbox marcado
```

### Excluir uma tarefa
```
Lixeira no cartão → viewModel.deleteTask(task) → DELETE no SQLite
      → Room emite nova lista → o cartão some
```

### Buscar
```
Digitar no campo → searchQuery muda → filteredTasks é recalculada
      → a lista mostra só as tarefas com o texto no título ou na disciplina
```

---

## 7. Limitações conhecidas

| Comportamento | Onde |
|---|---|
| Na tela de detalhes, clicar em **CONCLUIR** salva no banco, mas a tela **não se atualiza**: `task` foi carregada uma única vez pelo `LaunchedEffect`, então o status continua "pendente" até o usuário sair e voltar. | `TaskDetailsScreen` |
| Não é possível **editar** uma tarefa existente. | `TaskDetailsScreen` |
| Não é possível **desmarcar** uma tarefa concluída. | `TaskCard`, `TaskViewModel` |
| A exclusão não pede confirmação. | `TaskCard` |
| O botão de filtro não faz nada. A filtragem é feita pelo campo de busca, cujo placeholder diz "Nova tarefa". | `TaskListScreen` |
| A data e a hora são texto livre, sem validação nem ordenação por prazo. | `Task.dateTime` |
| O autor é sempre "Criado pelo aluno", porque o app não tem login. | `TaskViewModel.addTask` |
| Um id inexistente deixa a tela de detalhes presa em "Carregando...". | `TaskDetailsScreen` |

---

## 8. Tecnologias usadas

| Tecnologia | Papel no app |
|---|---|
| **Kotlin** | Linguagem do app. |
| **Jetpack Compose + Material 3** | Interface declarativa (telas, componentes, tema). |
| **Navigation Compose** | Navegação entre as telas por rotas em texto. |
| **Room** (+ KSP) | Banco SQLite local, com o código do DAO gerado em tempo de compilação. |
| **Coroutines / Flow** | Operações assíncronas no banco e atualização reativa da interface. |
| **ViewModel (Lifecycle)** | Guarda o estado, que sobrevive a rotações de tela. |

As versões ficam centralizadas em `gradle/libs.versions.toml`.
