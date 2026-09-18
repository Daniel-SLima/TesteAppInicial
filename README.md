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
- [ ] Gastos e ganhos recorrentes
- [ ] Pago / pendente e recebido / a receber
- [ ] Fechamento mensal automático

## Visão mensal

O cabeçalho permite navegar entre os meses. O saldo, as entradas, os gastos e a lista de movimentações são recalculados para o mês selecionado.

## Persistência

As movimentações ficam armazenadas no banco SQLite privado do aplicativo. Fechar o app ou reiniciar o aparelho não apaga os registros. A desinstalação do aplicativo remove os dados locais.

## Stack

- Kotlin
- Android SDK
- SQLite nativo
- Gradle
- VS Code + terminal
- Git/GitHub

## Fluxo

A branch `main` representa a base estável. A branch `develop` recebe versões já validadas e funcionalidades maiores são desenvolvidas em branches próprias.

## Build

O GitHub Actions gera um APK nomeado automaticamente com a versão atual, por exemplo `FinTest-v0.5.0.apk`.
