# Entrou / Saiu

Mini app Android de finanças pessoais, desenvolvido sem Android Studio.

## Proposta

Registrar rapidamente o que entrou e saiu do bolso, mantendo uma visão mensal simples e organizada.

## MVP

- [x] Build Android via GitHub Actions
- [x] APK instalável
- [x] Registrar gasto ou ganho
- [x] Informar descrição e valor
- [x] Exibir saldo, entradas e gastos
- [x] Listar movimentações da sessão
- [ ] Persistir dados localmente
- [ ] Editar e excluir movimentações
- [ ] Visão por mês
- [ ] Gastos e ganhos recorrentes
- [ ] Pago / pendente e recebido / a receber
- [ ] Fechamento mensal automático

## Stack

- Kotlin
- Android SDK
- Gradle
- VS Code + terminal
- Git/GitHub

## Fluxo

A branch `main` representa a base estável. A branch `develop` recebe versões já validadas e funcionalidades maiores são desenvolvidas em branches próprias.

## Build

O GitHub Actions gera um APK debug automaticamente. O build local continua disponível com Gradle.
