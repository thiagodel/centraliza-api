# Centraliza API

API REST desenvolvida com Java e Spring Boot para centralizar consultas a serviços externos. Atualmente, realiza consultas de endereços utilizando a API pública ViaCEP.

O projeto foi desenvolvido com foco em integração de APIs externas, organização em camadas, tratamento de exceções, cache e testes automatizados.

## Tecnologias utilizadas

- Java 17
- Spring Boot 4.1.2
- Spring Web / RestClient
- Maven
- JUnit 5
- Mockito
- Swagger / OpenAPI
- ConcurrentHashMap
- CompletableFuture

## Funcionalidades

- Consulta de endereços através do ViaCEP.
- Validação e normalização de CEP.
- Conversão dos dados externos para DTOs próprios.
- Tratamento global de exceções.
- Cache em memória com expiração de 5 minutos.
- Compartilhamento de consultas simultâneas para evitar chamadas externas duplicadas.
- Documentação interativa com Swagger.
- Testes unitários e testes de controller.

## Como executar o projeto

### Pré-requisitos

- Java 17 ou superior.
- Maven instalado.
- Git.

### Clonar o repositório

```bash
git clone https://github.com/thiagodel/centraliza-api.git
```

### Entrar na pasta

```bash
cd centraliza-api
```

### Executar a aplicação

```bash
mvn spring-boot:run
```

A aplicação estará disponível em:

```text
http://localhost:8080
```

## Endpoint disponível

### Consultar endereço por CEP

**GET** `/api/enderecos/{cep}`

Exemplo:

```http
GET http://localhost:8080/api/enderecos/38010000
```

### Resposta de sucesso — 200 OK

```json
{
  "cep": "38010-000",
  "logradouro": "Exemplo de logradouro",
  "complemento": "",
  "bairro": "Centro",
  "localidade": "Exemplo",
  "uf": "MG",
  "estado": "Minas Gerais"
}
```

### Resposta de erro — 400 Bad Request

```json
{
  "status": 400,
  "erro": "Bad Request",
  "mensagem": "CEP deve conter 8 dígitos"
}
```

### Resposta de erro — 404 Not Found

```json
{
  "status": 404,
  "erro": "Not Found",
  "mensagem": "CEP não encontrado"
}
```

## Cache

A aplicação possui um mecanismo de cache em memória utilizando `ConcurrentHashMap`.

Quando um CEP é consultado pela primeira vez, os dados são obtidos através do ViaCEP e armazenados temporariamente.

Nas próximas consultas ao mesmo CEP, os dados são recuperados diretamente do cache durante o período de validade de **5 minutos**.

O projeto também utiliza `CompletableFuture` para compartilhar o resultado de consultas simultâneas ao mesmo CEP, evitando chamadas externas redundantes.

## Documentação Swagger

Com a aplicação em execução, acesse:

[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

## Executar os testes

Para executar os testes automatizados:

```bash
mvn test
```

Os testes abrangem a camada de serviço, controller e mecanismo de cache.

## Objetivo do projeto

Projeto desenvolvido para praticar e consolidar conhecimentos em desenvolvimento backend com Java e Spring Boot, especialmente no consumo de APIs externas, gerenciamento de cache, concorrência e construção de APIs REST.
