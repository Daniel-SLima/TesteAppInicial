# FinTest

Mini app Android de finanças pessoais, desenvolvido sem Android Studio.

## MVP

- [x] APK Android com build automático
- [x] Gastos e ganhos rápidos
- [x] Persistência SQLite
- [x] Editar e excluir
- [x] Navegação mensal
- [x] Fixos mensais
- [x] Pago / pendente e recebido / a receber
- [x] Fechamento mensal automático
- [x] Saldo opcionalmente carregado entre meses

## Fechamento mensal

O FinTest usa o calendário do aparelho como competência. Ao virar o mês, o novo mês passa a ser o mês atual automaticamente e o anterior permanece disponível no histórico como **Mês encerrado automaticamente**.

A opção **Levar saldo realizado para o próximo mês** fica ativada por padrão. Quando ligada, o saldo realizado de todos os meses anteriores aparece como saldo inicial da competência selecionada. Desligar a opção faz cada mês começar em zero sem apagar nenhum lançamento.

Como o fechamento é calculado a partir dos lançamentos reais, editar um mês antigo também corrige os saldos posteriores automaticamente.

## Resumo

O app separa saldo inicial, saldo realizado, saldo previsto, recebido, pago, a receber e a pagar.

## Build

O GitHub Actions gera o APK com a versão no nome, por exemplo `FinTest-v0.10.0.apk`.

## Estado

A versão 0.8.0 completou o escopo funcional definido para o primeiro MVP.

## Categorias

A versão 0.9.0 adiciona categorias às movimentações e aos lançamentos fixos. Registros antigos são migrados automaticamente para **Outros**, preservando todo o histórico.

Categorias iniciais: Alimentação, Transporte, Casa, Lazer, Saúde, Compras, Salário e Outros.

Próximas versões podem usar essas categorias para filtros, gráficos, exportação e backup.


## Versão 0.10.0

- Fixos mensais aparecem também em meses futuros como projeções pendentes.
- O saldo previsto considera essas projeções sem alterar o saldo realizado.
- Busca por nome ou categoria.
- Filtros por tipo, status e categoria.
- Resumo de gastos previstos por categoria.
- Regras de lançamentos fixos podem ser editadas.
- Editar ou desativar um fixo preserva o histórico já realizado e atualiza/remove somente projeções pendentes atuais e futuras.
