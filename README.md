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


## Assinatura permanente

A partir da versão 0.10.1 o projeto está preparado para APKs release assinados sempre com a mesma chave. Isso permite instalar novas versões por cima da anterior sem apagar o banco local.

A chave privada não fica no repositório. O GitHub Actions usa quatro Secrets: `FINTEST_KEYSTORE_BASE64`, `FINTEST_KEYSTORE_PASSWORD`, `FINTEST_KEY_ALIAS` e `FINTEST_KEY_PASSWORD`.

Fingerprint SHA-256 esperado do certificado público:

`84:6D:68:33:75:0F:2F:51:1F:79:36:C7:A3:81:DE:1F:5A:B2:8C:53:40:02:13:AC:8D:61:44:AE:8F:DF:17:57`

Sem os Secrets, o workflow continua verde, mas gera um APK explicitamente marcado como `DEBUG-NAO-ATUALIZA`, que não deve ser usado como base de atualização.


## Versão 0.11.0

- Visualização horizontal dos gastos por categoria.
- Total mensal de gastos previsto no painel de categorias.
- Exportação do mês selecionado para CSV.
- CSV compatível com Excel/Google Sheets usando separador por ponto e vírgula e valores monetários em formato brasileiro.
- Exporta data, tipo, status, categoria, descrição, valor e indicação de lançamento fixo.
