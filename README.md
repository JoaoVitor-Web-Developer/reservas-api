# Reservas API

Uma API RESTful para gerenciamento de reservas de imóveis, construída com Spring Boot 3.x e Java 17. Esta aplicação fornece endpoints para autenticação de usuários, gerenciamento de propriedades e sistema de reservas.

## 🚀 Funcionalidades

- Autenticação baseada em JWT
- Cadastro e gerenciamento de usuários
- Gerenciamento de propriedades (Locações)
- Sistema de reservas
- Autorização baseada em papéis (roles)
- Validação de entrada
- Tratamento global de exceções

## 🛠️ Pré-requisitos

- Java 21
- Gradle 8.0+
- mysql 8.1
- Docker

## Instruções de Configuração

### 1. Configuração do Banco de Dados

#### Opção 1: Usando Docker (Recomendado)
```bash
docker run --name reservas-db -e POSTGRES_DB=reservas -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -p 5432:5432 -d postgres:14
```

#### Opção 2: Configuração Manual
1. Crie um banco de dados PostgreSQL chamado `reservas`
2. Atualize as configurações do banco de dados em `application.properties` ou `application.yml`

### 2. Configuração da Aplicação

Crie um arquivo `application.yml` em `src/main/resources/` com o seguinte conteúdo:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/reservas
    username: postgres
    password: postgres
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true

jwt:
  secret-key: cdcbe42a8d697568a59297eaa62fb60da018dfb0ef2716b45637fd24736097d5
  expiration-ms: 86400000  # 24 horas
```

### 3. Construir e Executar

```bash
# Construir a aplicação
./gradlew build

# Executar a aplicação
./gradlew bootRun
```

A aplicação estará disponível em `http://localhost:8080`

## Autenticação

### Registrar um novo usuário
```
POST /auth/register
Content-Type: application/json

{
  "name": "João",
  "email": "joao@exemplo.com",
  "password": "senhaSegura123",
  "cpf": "12345678900",
  "phone": "11999999999"
}
```

### Login
```
POST /auth/login
Content-Type: application/json

{
  "email": "joao@exemplo.com",
  "password": "senhaSegura123"
}
```

A resposta do login incluirá um token JWT que deve ser usado no cabeçalho `Authorization` para requisições autenticadas:
```
Authorization: Bearer <seu-token-jwt>
```

## Endpoints da API

### Usuários
- `GET /user` - Listar todos os usuários (apenas admin)
- `GET /user/{id}` - Obter usuário por ID
- `PUT /user/{id}` - Atualizar usuário
- `DELETE /user/{id}` - Excluir usuário (apenas se não houver reservas ativas)

### Locações (Propriedades)
- `GET /leases` - Listar todas as propriedades disponíveis
- `GET /leases/{id}` - Obter propriedade por ID
- `POST /leases` - Criar nova propriedade (apenas admin)
- `PUT /leases/{id}` - Atualizar propriedade (apenas admin)
- `DELETE /leases/{id}` - Excluir propriedade (apenas admin, somente sem reservas)
- `GET /leases/disponiveis?dataInicio={data}&dataFim={data}` - Buscar propriedades disponíveis por período

### Reservas
- `GET /reservations` - Listar todas as reservas
- `GET /reservations/{id}` - Obter reserva por ID
- `POST /reservations` - Criar nova reserva
- `PUT /reservations/{id}` - Atualizar reserva
- `DELETE /reservations/{id}` - Cancelar reserva

## Segurança

- Autenticação baseada em JWT
- Senhas criptografadas com BCrypt
- Controle de acesso baseado em papéis (roles)
- Validação de entrada
- Configuração CORS para o frontend (padrão: http://localhost:3000)

## Testes

Execute os testes com:
```bash
./gradlew test
```

## Docker

Construa e execute com Docker:

```bash
# Construir a aplicação
./gradlew build

# Construir a imagem Docker
docker build -t reservas-api .

# Executar com Docker Compose
docker-compose up -d
```