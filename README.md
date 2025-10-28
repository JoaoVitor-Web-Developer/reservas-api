# Reservas API

API para gerenciamento de reservas, desenvolvida com Spring Boot e Java 21.

## Pré-requisitos

- Docker e Docker Compose instalados
- Java 21
- Gradle

## Como executar com Docker (Recomendado)

1. **Clone o repositório**
   ```bash
   git clone https://github.com/JoaoVitor-Web-Developer/reservas-api.git
   cd reservas-api

2. **Construa e execute os containers**
   ```bash
   docker-compose up --build
   
## Construindo a aplicação ##

1. **Construa o projeto**
    ```bash
    "./gradlew bootBuild"

2. **Execute a aplicação**
    ```bash
    "./gradlew bootRun"

3. **A API estará disponível em**
   ```bash
   "http://localhost:8080"

## Documentação da API ##

A documentação da API está disponível em:
   ```bash
   "http://localhost:8080/swagger-ui.html"
