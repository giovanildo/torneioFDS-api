# torneioFDS

API REST para gerenciamento de torneios de futebol virtual (e-sports) entre amigos.

Organiza campeonatos no formato **todos contra todos** (round-robin), com turno e returno, tabela de classificacao com pontos corridos (3/1/0) e registro de placares.

## Stack

| Componente | Tecnologia |
|---|---|
| Linguagem | Java 21 LTS |
| Framework | Spring Boot 3.4 |
| ORM | Spring Data JPA + Hibernate 6 |
| Banco de dados | SQLite (arquivo local) |
| Autenticacao | Spring Security + HTTP Basic + BCrypt |
| Documentacao | Swagger / OpenAPI 3 (springdoc) |
| Build | Maven |

## Requisitos

- **Java 21** ou superior
- **Maven 3.8+**

## Como rodar

```bash
git clone https://github.com/giovanildo/torneioFDS.git
cd torneioFDS
mvn spring-boot:run
```

A aplicacao sobe em `http://localhost:8080`.

Um usuario admin e clubes iniciais sao criados automaticamente na primeira execucao.

| Login | Senha | Role |
|---|---|---|
| admin | admin | ADMIN |

## Documentacao da API

Com a aplicacao rodando, acesse:

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8080/v3/api-docs

## Endpoints

### Autenticacao (publico)

| Metodo | Endpoint | Descricao |
|---|---|---|
| POST | `/api/auth/registrar` | Criar nova conta de jogador |
| POST | `/api/auth/login` | Fazer login |

### Torneios (autenticado)

| Metodo | Endpoint | Descricao |
|---|---|---|
| GET | `/api/torneios` | Listar todos os torneios |
| POST | `/api/torneios` | Criar novo torneio |
| GET | `/api/torneios/{id}` | Buscar torneio por ID |
| DELETE | `/api/torneios/{id}` | Deletar torneio e todos os dados associados |

### Competidores (autenticado)

| Metodo | Endpoint | Descricao |
|---|---|---|
| GET | `/api/torneios/{id}/competidores` | Listar competidores do torneio |
| POST | `/api/torneios/{id}/competidores` | Adicionar competidor (jogador + clube) |
| DELETE | `/api/torneios/{id}/competidores/{cid}` | Remover competidor |

### Partidas (autenticado)

| Metodo | Endpoint | Descricao |
|---|---|---|
| POST | `/api/torneios/{id}/partidas/gerar` | Gerar partidas round-robin (turno + returno) |
| GET | `/api/torneios/{id}/partidas` | Listar partidas do torneio |
| PUT | `/api/torneios/{id}/partidas/{pid}/resultado` | Registrar placar de uma partida |

### Classificacao (autenticado)

| Metodo | Endpoint | Descricao |
|---|---|---|
| GET | `/api/torneios/{id}/classificacao` | Tabela de classificacao com pontos corridos |

### Premios e Sala de Trofeus (autenticado)

| Metodo | Endpoint | Descricao |
|---|---|---|
| POST | `/api/torneios/{id}/premios/gerar` | Gerar premios baseado na classificacao final |
| GET | `/api/torneios/{id}/premios` | Listar premios de um torneio |
| GET | `/api/eatletas/{id}/trofeus` | Sala de trofeus de um jogador |

### Clubes (autenticado)

| Metodo | Endpoint | Descricao |
|---|---|---|
| GET | `/api/clubes` | Listar todos os clubes |
| POST | `/api/clubes` | Cadastrar novo clube |

### Jogadores (autenticado)

| Metodo | Endpoint | Descricao |
|---|---|---|
| GET | `/api/eatletas` | Listar todos os jogadores |

## Exemplos de uso (curl)

### Criar conta

```bash
curl -X POST http://localhost:8080/api/auth/registrar \
  -H "Content-Type: application/json" \
  -d '{"nome": "Giovanildo", "login": "giova", "senha": "123456"}'
```

### Criar torneio

```bash
curl -X POST http://localhost:8080/api/torneios \
  -u admin:admin \
  -H "Content-Type: application/json" \
  -d '{"nome": "Copa FDS 2026", "porqueDoNome": "Torneio de fim de semana"}'
```

### Adicionar competidor

```bash
curl -X POST http://localhost:8080/api/torneios/1/competidores \
  -u admin:admin \
  -H "Content-Type: application/json" \
  -d '{"eAtletaId": 1, "clubeId": 1}'
```

### Gerar partidas

```bash
curl -X POST http://localhost:8080/api/torneios/1/partidas/gerar \
  -u admin:admin
```

### Registrar placar

```bash
curl -X PUT http://localhost:8080/api/torneios/1/partidas/1/resultado \
  -u admin:admin \
  -H "Content-Type: application/json" \
  -d '{"golsAnfitriao": 3, "golsVisitante": 1}'
```

### Ver classificacao

```bash
curl http://localhost:8080/api/torneios/1/classificacao \
  -u admin:admin
```

### Gerar premios

```bash
curl -X POST http://localhost:8080/api/torneios/1/premios/gerar \
  -u admin:admin
```

### Ver sala de trofeus de um jogador

```bash
curl http://localhost:8080/api/eatletas/1/trofeus \
  -u admin:admin
```

### Deletar torneio

```bash
curl -X DELETE http://localhost:8080/api/torneios/1 \
  -u admin:admin
```

## Modelo de dados

```
EAtleta (jogador)  ----> participa de ----> Torneio
Clube              ----> representa   ----> EAtleta no Torneio (via Competidor)
Competidor         ----> joga em      ----> Partida (via CompetidorEmCampo)
Partida            ----> compoe       ----> Torneio (round-robin, turno + returno)
Classificacao      ----> resultado    ----> tabela de pontos (3/1/0)
Premio             ----> reconhece   ----> conquista de um EAtleta em um Torneio
```

### Entidades

| Entidade | Descricao |
|---|---|
| `EAtleta` | Jogador de videogame (tambem e o usuario do sistema) |
| `Clube` | Clube de futebol que o jogador representa no torneio |
| `Torneio` | Campeonato com nome, descricao e data |
| `Competidor` | Vinculo entre EAtleta + Clube dentro de um Torneio |
| `Partida` | Jogo entre dois competidores, com rodada e status |
| `CompetidorEmCampo` | Participacao de um competidor em uma partida (gols, mando de campo) |
| `Classificacao` | POJO calculado com pontos, vitorias, empates, derrotas, saldo de gols |
| `Premio` | Premiacao de um jogador em um torneio (titulo, artilheiro, coca-cola, etc) |
| `TipoPremio` | Enum: CAMPEAO, VICE_CAMPEAO, TERCEIRO_LUGAR, ARTILHEIRO, MENOS_VAZADA, COCA_COLA, IBIS |

### Regras de classificacao

- Vitoria: 3 pontos
- Empate: 1 ponto
- Derrota: 0 pontos
- Desempate: pontos > vitorias > saldo de gols

### Premios automaticos (gerados ao final do torneio)

| Premio | Criterio |
|---|---|
| Campeao | 1o lugar na classificacao |
| Vice-campeao | 2o lugar |
| Terceiro lugar | 3o lugar (se houver 3+ competidores) |
| Artilheiro | Mais gols pro |
| Menos Vazada | Menos gols contra |
| Coca-Cola (Desonra) | Ultimo lugar — deve pagar uma coca-cola como prenda |
| Premio Ibis | Antipremio maximo — concedido ao atingir 12 Coca-Colas na carreira |

## Estrutura do projeto

```
src/main/java/com/giovanildo/torneiofds/
├── TorneioFdsApplication.java
├── config/
│   ├── SecurityConfig.java
│   ├── GlobalExceptionHandler.java
│   └── DataInitializer.java
├── controller/
│   ├── AuthController.java
│   ├── TorneioController.java
│   ├── PremioController.java
│   ├── ClubeController.java
│   └── EAtletaController.java
├── dto/
│   ├── LoginRequest.java
│   ├── RegistroRequest.java
│   ├── TorneioRequest.java / TorneioResponse.java
│   ├── CompetidorRequest.java / CompetidorResponse.java
│   ├── ClubeRequest.java / ClubeResponse.java
│   ├── ResultadoRequest.java
│   ├── PartidaResponse.java
│   ├── EAtletaResponse.java
│   ├── PremioResponse.java
│   └── SalaDeTrofeusResponse.java
├── model/
│   ├── EAtleta.java
│   ├── Clube.java
│   ├── Torneio.java
│   ├── Competidor.java
│   ├── Partida.java
│   ├── CompetidorEmCampo.java
│   ├── Classificacao.java
│   ├── Premio.java
│   └── TipoPremio.java
├── repository/
│   ├── EAtletaRepository.java
│   ├── ClubeRepository.java
│   ├── TorneioRepository.java
│   ├── CompetidorRepository.java
│   ├── PartidaRepository.java
│   ├── CompetidorEmCampoRepository.java
│   └── PremioRepository.java
└── service/
    ├── TorneioService.java
    ├── PremioService.java
    ├── EAtletaService.java
    ├── EAtletaDetailsService.java
    └── ClubeService.java
```

## Origem

Este projeto e uma evolucao do [lombras-jsf](https://github.com/giovanildo/lombras-jsf), originalmente construido com Java 8, JSF 2.2, PrimeFaces e Hibernate 5.4. O dominio e o algoritmo round-robin foram portados para uma stack moderna com Spring Boot 3 e API REST.

## Licenca

MIT
