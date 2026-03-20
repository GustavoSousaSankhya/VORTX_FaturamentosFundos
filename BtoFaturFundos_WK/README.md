

# BtoFaturFundos — Módulo Java Sankhya

Módulo de faturamento de fundos para o cliente **VORTX**, desenvolvido como módulo Java para importação no Sankhya.

---

## Visão Geral

O processo substitui o faturamento manual via planilha por um fluxo integrado dentro do Sankhya. Os dados de faturamento são recebidos na tabela `AD_FATURFUNDOS` (via API ou inserção manual) e, a partir deles, o módulo executa automaticamente:

1. Criação/validação do parceiro (cliente) via ReceitaWS
2. Criação/validação da operação financeira
3. Geração da nota fiscal de serviço
4. Baixa total ou parcial do título financeiro gerado

---

## Tabelas Envolvidas

### `AD_FATURFUNDOS` — Faturamentos (tabela pai)

| Campo | Descrição | Origem |
|---|---|---|
| IDINTEGRACAO | ID único de integração | Sankhya |
| IDFUNDOS | ID externo da VORTX | VORTX |
| CNPJCLIENTE | CNPJ do cliente | VORTX |
| EMAILCLIENTE | E-mail do cliente (opcional) | VORTX |
| CNPJGESTOR | CNPJ do gestor | VORTX |
| EMAILGESTOR | E-mail do gestor (opcional) | VORTX |
| TIPNEG | Tipo de negociação | VORTX |
| CODEMP | Unidade de negócio | VORTX |
| CODTIPOPER | Tipo de operação | VORTX |
| DTREMESSA | Data da remessa | VORTX |
| OBSERVACAO | Observação (opcional) | VORTX |
| CODPROD | Serviço/produto | VORTX |
| PERIODICIDADE | Periodicidade | VORTX |
| VLRUNIT | Valor unitário | VORTX |
| CODOPERFIN | Operação financeira | VORTX |
| TIPOPER | Tipo de operação (texto) | VORTX |
| CONTROLEINTERNO | ID comercial | VORTX |
| DESCR_IF | IF | VORTX |
| OPERACAO | Apelido da operação | VORTX |
| EMISSAO | Número da emissão | VORTX |
| SERIE | Número da série | VORTX |
| NUNOTA | Nro único da nota gerada | **Sankhya** |
| NUMNOTA | Nro da nota gerada | **Sankhya** |
| STATUSFATUR | Status do faturamento (ver tabela abaixo) | **Sankhya** |
| CODUSUFATUR | Usuário que faturou | **Sankhya** |
| DHFATUR | Data/hora do faturamento | **Sankhya** |

**Valores de STATUSFATUR:**

| Valor | Significado |
|---|---|
| 1 | Pendente |
| 2 | Nota gerada |
| 3 | Erro na geração |
| 4 | Baixado parcialmente |
| 5 | Baixado totalmente |
| 6 | Cancelado |
| 7 | Reservado |

---

### `AD_FATURFUNDOSBAIXA` — Baixas (tabela filha)

| Campo | Descrição | Origem |
|---|---|---|
| IDINTEGRACAO | ID de integração (referência ao pai) | VORTX |
| IDBAIXA | ID sequencial da baixa | **Sankhya** |
| IDFUNDOS | ID externo da VORTX | **Sankhya** |
| VLRBAIXA | Valor a ser baixado | VORTX |
| DHINTEGRACAO | Data/hora da integração | VORTX |
| NUNOTA | Nro único da nota | **Sankhya** |
| NUFIN | Nro único do financeiro | **Sankhya** |

---

## Componentes do Módulo

```
br.com.bandeirantes.geranotas.fundos.vortx
│
├── MainBtoFaturFundos.java              — Botão de ação: geração de nota
│
├── model/
│   ├── Nota.java                        — Model dos dados da nota
│   ├── Baixa.java                       — Model dos dados da baixa
│   ├── OperacaoFinanceira.java          — Model da operação financeira
│   └── Parceiro.java                    — Model do parceiro
│
├── criaparceiros/
│   └── EventoCriaParceiros.java         — Evento: cria parceiro via ReceitaWS
│
├── criaoperfin/
│   └── EventoCriaOperacaoFinan.java     — Evento: cria/vincula operação financeira
│
└── baixatitulo/
    └── EventoBaixaFaturFundos.java      — Evento: baixa total ou parcial do título
```

---

## Processo 1 — Criação de Parceiro (`EventoCriaParceiros`)

**Tipo:** Evento Programável — `afterInsert` em `AD_FATURFUNDOS`

**O que faz:**
- Ao inserir um registro em `AD_FATURFUNDOS`, consulta a API da ReceitaWS com o `CNPJCLIENTE`
- Se o parceiro não existir no Sankhya, cria o cadastro com os dados retornados (razão social, município, CEP, UF)
- Cria também um contato ativo para o parceiro com o `EMAILCLIENTE`, marcado como responsável por cobrança e recebimento de nota/boleto
- Se o município não existir no Sankhya, cria o cadastro da cidade e da UF automaticamente

**Pré-condições:**
- `CNPJCLIENTE` preenchido
- `EMAILCLIENTE` preenchido (caso contrário o contato não é criado)
- CNPJ válido e ativo na ReceitaWS

**Observação:** Em caso de rate limit (HTTP 429) na ReceitaWS, o evento aguarda 10 segundos e tenta novamente.

---

## Processo 2 — Criação de Operação Financeira (`EventoCriaOperacaoFinan`)

**Tipo:** Evento Programável — `afterInsert` em `AD_FATURFUNDOS`

**O que faz:**
- Ao inserir um registro em `AD_FATURFUNDOS`, verifica se o campo `CODOPERFIN` está vazio
- Se estiver vazio, e os campos obrigatórios estiverem preenchidos, busca se já existe uma operação financeira com as mesmas características
- Caso não exista, cria a operação financeira e vincula o código no campo `CODOPERFIN` do registro

**Campos obrigatórios para criação da operação financeira:**

| Campo | Descrição |
|---|---|
| PERIODICIDADE | Periodicidade da operação |
| TIPOPER | Tipo de operação (texto) |
| CONTROLEINTERNO | ID comercial |
| OPERACAO | Apelido da operação |
| EMISSAO | Número da emissão |
| SERIE | Número da série |

**Criações automáticas auxiliares:**
- **Emissor** (`AD_EMISSOPER`): criado automaticamente se não existir
- **Série** (`AD_SERIEOPER`): criada automaticamente se não existir

---

## Processo 3 — Geração de Nota (`MainBtoFaturFundos`)

**Tipo:** Botão de Ação (`AcaoRotinaJava`) na tela de `AD_FATURFUNDOS`

**O que faz:**
- O usuário seleciona uma ou mais linhas na tela de faturamentos e aciona o botão
- Para cada linha selecionada que ainda não possui nota (`NUNOTA` vazio), executa o fluxo completo de geração

**Fluxo de execução:**

```
1. validaNotas()         — Valida todos os campos obrigatórios
2. montaNota()           — Constrói o model Nota com os dados da linha
3. gerarCabecalho()      — Cria o cabeçalho da nota via CACHelper
4. incluirItem()         — Adiciona o item (serviço) via CACHelper
5. calcularImpostos()    — Calcula ISS, IRF e demais impostos
6. confirmarNota()       — Confirma/libera a nota (modo silencioso)
7. setCamposFaturamento() — Atualiza NUNOTA, NUMNOTA, CODUSUFATUR, DHFATUR
```

**Campos obrigatórios para geração:**

| Campo | Mensagem de erro se vazio |
|---|---|
| CNPJCLIENTE | "CNPJ do cliente está em branco!" |
| CODEMP | "Unidade de Negócio está em branco!" |
| CODTIPOPER | "Tipo de Operação está em branco!" |
| TIPNEG | "Tipo de Negociação está em branco!" |
| CODPROD | "Serviço (Produto) está em branco!" |
| VLRUNIT | "Valor Unitário está em branco!" |
| DTREMESSA | "Data de Remessa está em branco!" |
| CODOPERFIN | "Operação Financeira está em branco!" |

**Validações de negócio:**
- Se a nota já foi gerada (`NUNOTA` preenchido), a linha é ignorada silenciosamente
- Se o parceiro não for encontrado pelo CNPJ em `TGFPAR`, lança exceção com mensagem ao usuário

**Campos preenchidos após geração:**

| Campo | Valor |
|---|---|
| NUNOTA | Número único da nota gerada |
| NUMNOTA | Número da nota gerada |
| CODUSUFATUR | Código do usuário que acionou o botão |
| DHFATUR | Data/hora da execução |

---

## Processo 4 — Baixa de Título (`EventoBaixaFaturFundos`)

**Tipo:** Evento Programável — `afterInsert` em `AD_FATURFUNDOSBAIXA`

**O que faz:**
- Ao inserir um registro em `AD_FATURFUNDOSBAIXA`, localiza o faturamento pai pelo `IDINTEGRACAO`
- Busca o título financeiro (`TGFFIN`) gerado pela nota do faturamento pai
- Executa a baixa do título pelo valor informado em `VLRBAIXA`
- Decide automaticamente se a baixa é **total** ou **parcial** comparando `VLRBAIXA` com `VLRDESDOB`

**Baixa Total:**
- Ocorre quando `VLRBAIXA >= VLRDESDOB` (valor igual ou maior que o título)
- O título é baixado pelo valor original (`VLRDESDOB`)

**Baixa Parcial:**
- Ocorre quando `VLRBAIXA < VLRDESDOB`
- O título é baixado pelo valor informado
- O saldo restante (`VLRDESDOB - VLRBAIXA`) é mantido como pendência com a mesma data de vencimento original

**Pré-condições:**
- O faturamento pai (`AD_FATURFUNDOS`) deve existir com o mesmo `IDINTEGRACAO`
- A nota já deve ter sido gerada (`NUNOTA` preenchido no pai)
- Deve existir um título financeiro ativo (`RECDESP = 1`) em `TGFFIN` para a nota

**Parâmetro do sistema necessário:**

| Chave | Campo | Descrição |
|---|---|---|
| `TOPBAIXA` | INTEIRO | Código do tipo de operação utilizado nas baixas automáticas |

---

## Geração do Artifact

Para gerar o `.jar` do módulo no IntelliJ:

1. **File → Project Structure → Artifacts**
2. Verificar se o artifact do tipo **JAR** está configurado apontando para a classe principal
3. **Build → Build Artifacts → Build**
4. O arquivo `.jar` será gerado na pasta `out/artifacts/`

Para importar no Sankhya:
- Acesse **Configurações → Módulos Java**
- Faça upload do `.jar` gerado
- Registre as classes nos respectivos pontos de extensão (Botão de Ação e Evento Programável)

---

## Registros no Sankhya

| Classe | Tipo de Extensão | Tabela/Entidade |
|---|---|---|
| `MainBtoFaturFundos` | Botão de Ação | AD_FATURFUNDOS |
| `EventoCriaParceiros` | Evento Programável | AD_FATURFUNDOS |
| `EventoCriaOperacaoFinan` | Evento Programável | AD_FATURFUNDOS |
| `EventoBaixaFaturFundos` | Evento Programável | AD_FATURFUNDOSBAIXA |