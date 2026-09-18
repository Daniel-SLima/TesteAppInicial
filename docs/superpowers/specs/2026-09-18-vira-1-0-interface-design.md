# Vira 1.0 — Redesign de Interface e Identidade

## Objetivo

Transformar o FinTest em **Vira**, mantendo todas as funcionalidades financeiras atuais e reorganizando a experiência em quatro áreas claras: **Início, Movimentações, Planejamento e Ajustes**.

Nome de marca no aplicativo: **Vira**.  
Nome público/marketing e eventual nome de loja: **Vira Finanças**.

O redesign deve transmitir uma mistura de **financeiro pessoal calmo + fintech brasileira moderna**: profissional, humano, legível e com personalidade própria, sem aparência genérica de dashboard ou kit de UI produzido por IA.

## Restrições de continuidade

- O `applicationId` permanece **`com.danielslima.testeappinicial`** durante o redesign para preservar atualização do app e banco local.
- A assinatura permanente atual é mantida.
- SQLite e o formato de dados existentes continuam válidos.
- Nenhuma funcionalidade atual pode desaparecer durante a migração visual.
- O app continua local-first e offline.
- O redesign não adiciona dependência de servidor.
- Compatibilidade mínima permanece em Android API 26.
- Tema claro e escuro continuam suportados.

## Marca

### Nome

**Vira** é o nome principal exibido dentro do aplicativo.

**Vira Finanças** será usado quando for útil explicar o produto fora do app, como:
- título futuro em loja;
- divulgação;
- página web;
- materiais de apresentação.

### Ideia central

“Vira” comunica movimento sem urgência: virar o mês, virar hábitos, virar uma fase financeira e enxergar o próximo passo.

A marca não deve depender de símbolos óbvios como cifrão, moeda, banco ou cartão como elemento principal.

### Símbolo recomendado

O símbolo principal será um **V em movimento**, construído como duas curvas arredondadas que convergem e voltam a abrir. A geometria deve sugerir simultaneamente:
- virada;
- fluxo;
- equilíbrio;
- continuidade.

O símbolo precisa funcionar em:
- 24 dp na navegação;
- ícone do launcher;
- splash;
- monocromático;
- tema claro e escuro.

## Sistema visual

### Paleta base

- Fundo quente: `#F5F1E9`
- Superfície clara: `#FFFCF7`
- Verde profundo da marca: `#153F37`
- Verde/teal funcional: `#2B7A6B`
- Verde suave: `#DDEBE5`
- Coral de saída/atenção: `#E36C5C`
- Coral suave: `#F6DDD6`
- Dourado de apoio: `#D9A441`
- Texto principal: `#1C2B29`
- Texto secundário: `#6C7875`
- Bordas: `#E7E1D8`

Cores devem ter função. Verde não é decoração genérica: representa marca, entrada e segurança. Coral indica saída ou alerta. Dourado é pontual.

### Tipografia

Interface: sans-serif moderna, limpa, com alta legibilidade. A implementação inicial pode usar fontes seguras do Android; uma fonte própria poderá ser empacotada apenas depois de validação de licença e tamanho.

Títulos editoriais podem ter contraste sutil em relação ao corpo, mas a UI não deve misturar muitas famílias tipográficas.

### Espaçamento

Base de 4 dp com preferência por múltiplos de 8 dp.
- Margem lateral principal: 18–20 dp
- Gap pequeno: 8 dp
- Gap de bloco: 12–16 dp
- Separação de seções: 20–24 dp
- Área mínima de toque: 48 dp

### Formas

- Cards principais: raio 20–24 dp
- Cards secundários: raio 16–18 dp
- Chips: cápsula
- Botões: 14–18 dp
- Sombras discretas; hierarquia deve vir principalmente de espaço, contraste e tipografia.

## Sistema de ícones próprio

Os ícones principais do Vira não devem copiar diretamente Material Icons, Feather, Heroicons ou outro pacote.

### Regras

- Grid: 24 × 24
- Stroke visual: aproximadamente 1.75–2 dp
- Pontas e junções arredondadas
- Formas simples e legíveis a 20–24 dp
- Poucos detalhes internos
- Mesma proporção ótica entre todos os símbolos

### Primeira família

1. Início
2. Movimentações
3. Planejamento
4. Ajustes
5. Gasto
6. Ganho
7. Fixo mensal
8. Parcelamento
9. Orçamento
10. Categoria
11. Backup
12. Restaurar
13. Exportar
14. Segurança
15. Tema
16. Notificações

Os quatro ícones da navegação inferior serão desenhados como Android Vector Drawables próprios.

## Arquitetura das telas

### 1. Início

Objetivo: responder “como está meu mês?” em segundos.

Conteúdo:
- marca Vira;
- mês selecionado;
- saldo realizado;
- saldo previsto;
- a receber;
- a pagar;
- ações `+ Gasto` e `+ Ganho`;
- próximos vencimentos;
- mini resumo do orçamento.

Não ficam nesta tela:
- backup;
- CSV;
- proteção;
- gerenciamento completo de fixos;
- filtros avançados.

### 2. Movimentações

Objetivo: concentrar histórico e operação detalhada.

Conteúdo:
- mês;
- busca;
- filtros;
- lista por data;
- entradas e saídas;
- fixos;
- parcelas;
- status.

Ação de novo lançamento abre um fluxo dedicado, em vez de manter formulário permanente na home.

### 3. Planejamento

Objetivo: concentrar decisões futuras.

Conteúdo:
- orçamento por categoria;
- fixos;
- parcelamentos;
- meses futuros;
- próximos recebimentos;
- próximos pagamentos;
- posteriormente notificações, atrasados e metas.

### 4. Ajustes

Objetivo: remover ferramentas técnicas da experiência financeira principal.

Conteúdo:
- tema;
- proteção do app;
- janela de reautenticação de 5 minutos;
- levar saldo entre meses;
- categorias;
- notificações;
- backup/restauração;
- exportação CSV;
- versão e informações do produto.

## Navegação

Barra inferior persistente com quatro itens:
- Início
- Movimentações
- Planejamento
- Ajustes

A barra deve permanecer estável durante o uso normal.

Fluxos secundários como editar movimentação, novo lançamento, editar fixo e orçamento podem abrir como tela/modal focado e retornar à área de origem.

## Novo lançamento

O formulário atual será substituído por fluxo dedicado:

1. Tipo: gasto ou ganho.
2. Nome.
3. Valor.
4. Categoria.
5. Natureza:
   - único;
   - parcelado;
   - fixo mensal.
6. Campos específicos aparecem somente quando necessários.
7. Salvar.

O fluxo precisa continuar rápido para um lançamento simples.

## Migração técnica

O `MainActivity.kt` atual tem mais de 1.800 linhas e concentra responsabilidades demais.

A migração será incremental, evitando reescrever o domínio financeiro.

Estrutura alvo:

```
MainActivity
└── AppShell
    ├── HomeScreen
    ├── MovementsScreen
    ├── PlanningScreen
    └── SettingsScreen
```

Cada tela terá:
- um XML próprio;
- uma classe/controlador focado;
- acesso explícito aos dados necessários.

A camada de banco existente será preservada. Separações adicionais de repositório poderão acontecer apenas quando reduzirem risco ou duplicação.

## Fases

### 1.0 Alpha 01
- renomear UI para Vira;
- criar identidade base;
- criar símbolo vetorial inicial;
- criar navegação inferior;
- nova tela Início;
- manter acesso às funcionalidades existentes.

### 1.0 Alpha 02
- nova tela Movimentações;
- novo fluxo de lançamento;
- busca/filtros migrados;
- edição/exclusão migradas.

### 1.0 Alpha 03
- nova tela Planejamento;
- fixos;
- parcelas;
- orçamentos;
- projeções futuras.

### 1.0 Alpha 04
- nova tela Ajustes;
- tema;
- segurança;
- backup;
- restauração;
- CSV;
- preferências.

### Beta
- categorias personalizadas;
- notificações;
- vencimentos e atrasados;
- comparação entre meses;
- revisão de acessibilidade;
- refinamento dark mode.

### 1.0.0
- nenhuma regressão funcional;
- atualização por cima da versão assinada anterior;
- banco preservado;
- navegação e identidade consistentes;
- build release assinado e testado em aparelho real.

## Critérios de qualidade

- Nenhuma tela principal deve parecer uma coleção de cards sem hierarquia.
- Informação financeira prioritária deve ser compreendida sem rolagem excessiva.
- Ferramentas raras não competem com ações diárias.
- Estados de ganho/gasto/pendente não dependem apenas de cor.
- Textos devem funcionar com aumento de fonte.
- Controles interativos devem ter ao menos 48 dp.
- Tema escuro deve preservar hierarquia, não apenas inverter cores.
- Ícones próprios devem permanecer legíveis em tamanhos pequenos.

## Blueprint

O blueprint visual e funcional das quatro áreas está sendo mantido no board Miro **Vira 1.0 — Blueprint e Identidade**.

A implementação Android deve seguir este documento e o board como referências complementares.
