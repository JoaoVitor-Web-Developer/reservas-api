# Reservas API

API para gerenciamento de reservas, desenvolvida com Spring Boot e Java 21.

## Pré-requisitos

- Docker e Docker Compose instalados
- Java 21
- Gradle

## Como executar com Docker (Recomendado)

1. Clone o repositório
    ```bash
    git clone https://github.com/JoaoVitor-Web-Developer/reservas-api.git
    cd reservas-api
   
2. Construa e execute os containers
    ```bash 
    docker compose up --build
   
🔹 O Docker irá:

🔹 Baixar e iniciar o banco MySQL

🔹 Compilar o backend com Gradle

🔹 Subir a API Spring Boot automaticamente

3. Acesse da API
   ```bash
   http://localhost:8080

## Documentação da API ##

A documentação da API está disponível em:
   ```bash
   http://localhost:8080/swagger-ui.html
