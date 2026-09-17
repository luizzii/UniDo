# UniDo

Aplicativo acadêmico simples de gerenciamento de tarefas, desenvolvido para a atividade final de Android Intermediário.

## Requisitos atendidos

- Kotlin
- Jetpack Compose
- Navigation Compose
- Room Database
- Arquitetura MVVM
- Persistência local
- Duas telas principais: lista e cadastro/detalhes
- Cadastro, listagem, consulta, conclusão e exclusão de tarefas

## Estrutura

- `data/`: entidade Room, DAO, banco e Repository
- `viewmodel/`: lógica de apresentação
- `ui/`: telas Jetpack Compose
- `MainActivity.kt`: inicialização e navegação

## Como executar

1. Abra a pasta `UniDo` no Android Studio.
2. Aguarde a sincronização do Gradle.
3. Se solicitado, aceite a instalação das dependências.
4. Execute em um emulador ou dispositivo Android.

O projeto utiliza Java 17 para a compilação.

## Observação

As interfaces foram simplificadas para manter o escopo compatível com os requisitos mínimos da atividade. O filtro visual da tela inicial também funciona como busca por título ou disciplina.
