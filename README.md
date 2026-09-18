# FinTest

Mini app Android de finanças pessoais, desenvolvido sem Android Studio.

## MVP

- [x] Build Android via GitHub Actions
- [x] APK instalável
- [x] Registrar gasto ou ganho
- [x] Persistir dados localmente
- [x] Editar e excluir
- [x] Visão mensal
- [x] Gastos e ganhos fixos
- [x] Pago / pendente e recebido / a receber
- [ ] Fechamento mensal automático

## Status financeiro

Movimentações rápidas são registradas como realizadas. Ocorrências criadas por fixos mensais começam como pendentes.

Ao tocar em uma movimentação, o status pode ser alterado entre **Pago/Pendente** para gastos ou **Recebido/A receber** para ganhos.

O resumo mensal separa:

- saldo realizado;
- saldo previsto após pendências;
- valores recebidos e pagos;
- valores a receber e a pagar.

## Migração

A versão 0.7.0 adiciona o status ao banco SQLite preservando os lançamentos anteriores. Movimentações já existentes são migradas como realizadas.

## Build

O GitHub Actions gera o APK com a versão no nome, por exemplo `FinTest-v0.7.0.apk`.
