# UniDo

**Sua rotina acadêmica organizada.**

Projeto final da trilha Android (Capacita iRede): uma lista de tarefas acadêmicas feita em Kotlin com Jetpack Compose.

## Informações do aluno

| | |
|---|---|
| **Nome completo** | Luise Gomes Lúcio |
| **Matrícula** | 09157712336 |

O planejamento deste projeto (Unidade 6, Capítulo 1) foi entregue em 19/07/2026. Este documento apresenta o aplicativo pronto e compara o resultado com o que foi planejado.

## Justificativa da escolha do tema

A rotina de um universitário se espalha por vários lugares: o prazo de um trabalho está no plano de ensino, a data da prova foi dita em sala e as tarefas pessoais ficam em anotações soltas. Escolhi a lista de tarefas com um recorte acadêmico para juntar tudo isso num único aplicativo, em que cada tarefa tem disciplina, prazo com data e hora e descrição, e em que fica visível o que está pendente, o que atrasou e o que já foi concluído.

No planejamento, a ideia incluía professores criando tarefas a partir dos seus cronogramas para os alunos consultarem. Essa parte ficou fora da entrega (o motivo está em [O que mudou](#o-que-mudou)), mas o tema e o objetivo continuam os mesmos: ajudar o estudante a se planejar ao longo do semestre.

O desafio técnico previsto era o gerenciamento de estado da lista, garantindo que criar, concluir e atualizar tarefas aparecesse na interface automaticamente. Ele foi o centro do projeto e acabou indo além do planejado: a lista é salva num banco de dados local e a interface reage a cada mudança nesse banco.

## Como o aplicativo funciona

O UniDo tem três telas: a lista, os detalhes de uma tarefa e o formulário de criação e edição.

### Tela 1: Minhas tarefas

- **Cabeçalho** com a logo, o nome UniDo e o slogan.
- **Busca** por título ou disciplina, que filtra a lista enquanto o usuário digita.
- **Botão +**, que abre o formulário de nova tarefa.
- **Filtro por status**: Todas, Pendentes, Atrasadas ou Concluídas.
- **Lista de tarefas** em cartões. Cada cartão mostra título, disciplina, quem criou e o prazo, além de um checkbox e uma lixeira.

As tarefas pendentes aparecem primeiro, do prazo mais próximo para o mais distante. Depois vêm as tarefas sem prazo e, por último, as concluídas.

A cor do cartão indica o status:

| Status | Aparência |
|---|---|
| Pendente | Cartão cinza com o prazo |
| Atrasada | Cartão vermelho com "ATRASADA" e o prazo |
| Concluída | Cartão verde com "CONCLUÍDA" |

Uma tarefa passa a atrasada sozinha quando o prazo vence, porque a tela confere o horário a cada minuto.

Ações disponíveis na lista:

- **Marcar ou desmarcar** o checkbox conclui ou reabre a tarefa.
- **Tocar na lixeira** abre uma confirmação antes de excluir.
- **Tocar no cartão** abre os detalhes da tarefa.

### Tela 2: Detalhes da tarefa

Mostra o status em uma faixa colorida, o título, a disciplina, quem criou, o prazo e a descrição completa. A partir dela o usuário pode:

- **Concluir** a tarefa, ou **reabrir** uma tarefa já concluída;
- **Editar** a tarefa pelo ícone de lápis;
- **Voltar** para a lista em "Retornar à lista".

### Formulário: nova tarefa e edição

Tem os campos Título, Disciplina, Data e hora e Descrição. Só o título é obrigatório, e o botão SALVAR fica desabilitado enquanto ele estiver vazio. O prazo é escolhido em um calendário e depois em um relógio de 24 horas, e pode ser removido. O que foi digitado é preservado quando a tela gira.

### Armazenamento

As tarefas ficam salvas no próprio aparelho, em um banco SQLite acessado pelo Room, e continuam lá depois que o aplicativo é fechado. O aplicativo não usa internet nem contas de usuário.

## Planejado e entregue

### Funcionalidades mínimas do enunciado

| Requisito | Situação |
|---|---|
| Adicionar tarefa | Atendido, em uma tela de formulário em vez de um campo na lista |
| Visualizar lista | Atendido, com `LazyColumn` |
| Marcar e desmarcar | Atendido, com `Checkbox` e mudança de cor do cartão |
| Excluir | Atendido, com lixeira e confirmação |
| Navegar para os detalhes passando dados | Atendido, com Navigation Compose |

### O que foi aplicado como planejado

- **Identidade**: nome UniDo, logo, slogan e o título "Minhas tarefas".
- **Lista**: `LazyColumn` com um `Card` por tarefa, `Checkbox`, lixeira e `Text` para título, disciplina, criador e prazo.
- **Botão de filtro** ao lado do botão de adicionar, como no protótipo.
- **Tela de detalhes**: "Retornar à lista", faixa de status, título, disciplina, criador, faixa "Termina em…", caixa de descrição, botão CONCLUIR e o lápis de edição.
- **Navegação**: Navigation Compose com `NavController`, passando o identificador da tarefa na rota `details/{id}` e voltando com `popBackStack()`.

### O que mudou

| Planejado | Entregue | Motivo |
|---|---|---|
| Professores criam tarefas para os alunos | Só o próprio aluno cria tarefas; todas ficam marcadas como "Criado pelo aluno" | Exigiria contas, perfis e um servidor para sincronizar os dados, o que está fora do escopo do módulo |
| Campo de texto na lista para digitar o título da nova tarefa | O campo da lista virou a busca, e o botão + abre um formulário | Uma tarefa do UniDo tem disciplina, prazo e descrição, que não cabem em um único campo |
| `FloatingActionButton` para adicionar | `IconButton` + ao lado da busca | Mantém a posição que o botão tem no protótipo |
| `Scaffold` com `TopAppBar` | Cabeçalho próprio com `Row` e `Column` | Reproduz a logo e o slogan do protótipo, que não se encaixam em uma barra padrão |
| Lista organizada por mês, com seções que abrem e fecham | Lista única ordenada por prazo, com uma faixa "TAREFAS" | Simplificação; a ordem por prazo e o filtro por status cobrem a mesma necessidade |
| Seta ">" em cada cartão | O cartão inteiro é clicável | Deixa mais espaço para o prazo e a lixeira |
| Logo no topo da tela de detalhes | A tela começa em "Retornar à lista" | Dá mais espaço ao conteúdo da tarefa |
| "Criado por … em xx/xx/xxxx, às 00h00" | Só quem criou | A data de criação não é guardada |
| Dois status: pendente e concluída | Três status: pendente, atrasada e concluída | Com prazo de data e hora, destacar o atraso ficou natural e útil |

### Variáveis de estado

| Planejado | Entregue |
|---|---|
| `listaTarefas: List<Tarefa>` com `remember { mutableStateOf(...) }` | `tasks: StateFlow<List<Task>>` no `TaskViewModel`, alimentado por um `Flow` do Room e lido na tela com `collectAsState()`. Assim a lista sobrevive ao fechamento do aplicativo |
| `tituloNovaTarefa: String` | Quatro estados no formulário (`title`, `subject`, `dueAt` e `description`), guardados com `rememberSaveable` |
| `tarefaSelecionada: Tarefa?` | Não existe como variável. O `id` da tarefa vai na rota de navegação, e a tela de detalhes observa essa tarefa direto no banco |

Estados que não estavam no planejamento: `searchQuery` (texto da busca), `statusFilter` (filtro escolhido) e `taskToDelete` (tarefa aguardando confirmação de exclusão).

### Navegação

| Ação | Origem | Destino | Situação |
|---|---|---|---|
| Toque em uma tarefa | Lista | Detalhes (`details/{id}`) | Como planejado |
| "Retornar à lista" | Detalhes | Lista (`popBackStack()`) | Como planejado |
| Botão + | Lista | Formulário (`form/-1`) | Novo |
| Lápis de edição | Detalhes | Formulário (`form/{id}`) | Novo |

## Outras observações

- **Além do escopo do módulo inicial**: persistência com Room, arquitetura em camadas (dados, `ViewModel` e interface), tela de abertura (splash), tema escuro e seletor de data e hora do Material 3.
- **Testes**: há testes unitários para as regras de prazo e de filtro (`./gradlew test`). Ainda não há testes de interface.
- **Limitações**: a exclusão não pode ser desfeita depois de confirmada, e o aplicativo guarda o prazo da tarefa, mas não a data em que ela foi concluída.
- **Próximos passos**: contas de aluno e professor com sincronização, para realizar a ideia original de o professor publicar as tarefas da disciplina; e notificações antes do vencimento do prazo.

## Como executar

1. Abra a pasta do projeto no Android Studio e aguarde a sincronização do Gradle.
2. Execute o módulo `app` em um emulador ou aparelho com Android 7.0 (API 24) ou superior.

Pela linha de comando: `./gradlew installDebug`.

## Tecnologias

Kotlin, Jetpack Compose, Material 3, Navigation Compose, Room, Coroutines e Flow.
