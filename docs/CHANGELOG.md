# Changelog

Registro de mudancas do torneioFDS (API + Web).

---

## 2026-04-04

### Ordenacao de torneios por ID decrescente

Torneios agora sao listados do mais recente para o mais antigo.

**API:**
- `TorneioRepository`: novo metodo `findAllByOrderByIdDesc()`
- `TorneioService.listarTodos()`: usa ordenacao decrescente por ID

### Redirect apos criar torneio

Ao criar um torneio, o usuario e redirecionado diretamente para a tela de detalhes do torneio criado, em vez de voltar para a lista.

**Web:**
- `TorneioController.salvar()`: captura retorno da API e redireciona para `/torneios/{id}`

### Melhorias no fluxo de torneio e novo premio Escapou da Coca-Cola

Conjunto de melhorias na experiencia de uso do torneio: abas condicionais, botoes +/- para gols, partidas sequenciais com bloqueio, aba de resumo ao final e novo premio.

**API:**
- Novo valor no enum `TipoPremio`: `ESCAPOU_DA_COCA_COLA` (penultimo lugar)
- `PremioService.gerarPremios` agora cria premio "Escapou da Coca-Cola" para o penultimo colocado (se 3+ competidores)
- `PremioResponse` agora inclui campo `nomeEAtleta`
- `SalaDeTrofeusResponse` agora inclui lista `escapouDaCocaCola`
- `PremioController` (API) filtra e retorna o novo tipo na sala de trofeus

**Web:**
- DTOs `PremioResponse` e `SalaDeTrofeusResponse` espelhados com novos campos
- Abas "Partidas" e "Classificacao" desabilitadas quando nao ha partidas geradas (Bootstrap `disabled`)
- Nova aba "Resumo" visivel quando todas as partidas estao encerradas — mostra Campeao, Vice, Coca-Cola e Escapou da Coca-Cola
- Botoes +/- substituem inputs de gols (sem setas do input number)
- Bloqueio sequencial de partidas: apenas a primeira pendente habilitada por padrao
- Botao "Desbloquear/Bloquear" manual em cada partida para override
- Ao confirmar ultima partida, redireciona automaticamente para aba Resumo
- Novo template `trofeu/resumo.html` com 4 cards de premios
- Nova secao "Escapou da Coca-Cola" na sala de trofeus (`sala.html`)
- `PartidaController`, `ClassificacaoController`, `TorneioController` passam flag `todasEncerradas`
- `PremioController` (Web) novo endpoint `GET /torneios/{id}/resumo`

---

### Premios automaticos ao final do torneio

Premios agora sao gerados automaticamente quando o ultimo resultado e registrado. Nao e mais necessario acionar manualmente.

**API:**
- `TorneioService.registrarResultado` verifica se todas as partidas estao encerradas e chama `PremioService.gerarPremios` automaticamente
- `PremioService` injetado via setter com `@Lazy` para resolver dependencia circular com `TorneioService`
- Removido endpoint `POST /api/torneios/{id}/premios/gerar`

**Web:**
- Removido botao "Gerar Premios" da tela de classificacao
- Removido metodo `gerarPremios` do `TorneioApiClient`
- Removido endpoint `POST /torneios/{id}/premios/gerar` do `PremioController`

### Bloqueio de geracao duplicada de partidas

Impede gerar partidas quando o torneio ja tem partidas geradas. Tambem esconde opcoes de edicao de competidores quando o torneio ja esta em andamento.

**API:**
- `TorneioService.gerarPartidas` lanca excecao se ja existem partidas para o torneio

**Web:**
- Formulario "Adicionar Competidor" escondido quando ja existem partidas
- Botao "Remover" de cada competidor escondido quando ja existem partidas
- Botao "Gerar Partidas" escondido quando ja existem partidas
- Tela de detalhes agora recebe a lista de partidas do controller

### Tela inicial: Sala de Trofeus do jogador logado

Apos login, o usuario e redirecionado para sua propria sala de trofeus.

**Web:**
- Login salva `eAtletaId` na sessao HTTP
- `/` redireciona para `/jogadores/{eAtletaId}/trofeus`
- Login redireciona para `/` (que vai para a sala de trofeus)
- Navbar: logo "torneioFDS" aponta para `/`
- Navbar: novo link "Meus Trofeus" como primeiro item do menu

### Seed de dados ficticios

`DataInitializer` expandido com 13 torneios ficticios para demonstracao.

**API:**
- 4 jogadores criados: Giovanildo (giova), Tiago (tiago), Rafael (rafael), Bruno (bruno) — senha: `123456`
- 13 torneios com clubes variados e resultados gerados com seed deterministico
- Bruno manipulado para perder mais (acumula coca-colas e atinge Premio Ibis)
- Giovanildo manipulado para ganhar mais (acumula titulos)
- Removido metodo privado `criarTorneioCompleto` que ficou sem uso

---

## 2026-04-03

### Sistema de premios e sala de trofeus

**API:**
- Novas entidades: `Premio`, `TipoPremio`
- Novo servico: `PremioService` com geracao automatica de premios
- Novo controller: `PremioController` com endpoints de premios e sala de trofeus
- Novos DTOs: `PremioResponse`, `SalaDeTrofeusResponse`
- Novo repositorio: `PremioRepository` com queries JPQL explicitas
- `TorneioService.deletar` agora remove premios antes dos demais dados

**Web:**
- Nova tela: sala de trofeus (`/jogadores/{id}/trofeus`)
- Novo controller: `PremioController`
- Novos DTOs: `PremioResponse`, `SalaDeTrofeusResponse`
- Botao "Trofeus" na lista de jogadores
- Botao "Gerar Premios" na classificacao

### Endpoint de deletar torneio

**API:**
- `DELETE /api/torneios/{id}` com JPQL bulk delete em cascata
- Ordem: Premio > CompetidorEmCampo > Partida > Competidor > Torneio

**Web:**
- Botao de deletar na lista de torneios com confirmacao

---

## 2026-04-02

### Correcao: PremioRepository com @Query explicito

Spring Data nao consegue resolver o atributo `eAtleta` (minuscula + maiuscula) por method name derivation. Corrigido usando `@Query` JPQL explicito.

### Correcao: Lombok 1.18.44

Fixada versao do Lombok para compatibilidade com JDK 25.

### Suporte a H2 com Spring Profiles

Adicionado perfil `h2` como alternativa ao SQLite.

### README e documentacao

Adicionados READMEs detalhados para API e Web com endpoints, arquitetura e exemplos.

---

## 2026-04-01

### Projeto inicial

- API REST Spring Boot 3.4.4 com SQLite
- Entidades: EAtleta, Clube, Torneio, Competidor, CompetidorEmCampo, Partida, Classificacao
- Algoritmo round-robin (turno + returno) portado do lombras-jsf
- Spring Security com HTTP Basic + BCrypt
- Swagger/OpenAPI
- Frontend Thymeleaf + Bootstrap 5 na porta 8081
