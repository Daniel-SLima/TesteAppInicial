# FinTest

Mini app Android de finanças pessoais, desenvolvido sem Android Studio.

## Proposta

Registrar rapidamente o que entrou e saiu do bolso, mantendo uma visão mensal simples e organizada.

## MVP

- [x] Build Android via GitHub Actions
- [x] APK instalável
- [x] Registrar gasto ou ganho
- [x] Informar descrição e valor
- [x] Exibir saldo, entradas e gastos
- [x] Listar movimentações
- [x] Persistir dados localmente com SQLite nativo
- [x] Editar e excluir movimentações
- [x] Visão por mês com navegação anterior/próximo
- [x] Gastos e ganhos fixos mensais
- [ ] Pago / pendente e recebido / a receber
- [ ] Fechamento mensal automático

## Fixos mensais

Ao registrar uma movimentação, marque **Repetir todo mês** e escolha o dia. O FinTest cria a ocorrência do mês automaticamente e, nos meses seguintes, gera uma nova ocorrência quando aquele mês passa a fazer parte do histórico atual.

O botão **Gerenciar fixos mensais** lista as recorrências ativas e permite desativá-las sem apagar as ocorrências que já fazem parte do histórico.

## Persistência e migração

Os dados ficam no SQLite privado do aplicativo. A versão 0.6.0 atualiza o banco de dados preservando os lançamentos já existentes.

## Stack

- Kotlin
- Android SDK
- SQLite nativo
- Gradle
- VS Code + terminal
- Git/GitHub

## Build

O GitHub Actions gera um APK nomeado automaticamente com a versão atual, por exemplo `FinTest-v0.6.0.apk`.
