# 🏨 API de Reservas

API RESTful para gerenciamento de reservas e locações, desenvolvida com Spring Boot 3.3.4 e Java 21. A aplicação oferece autenticação segura, gerenciamento de usuários e sistema de locações com verificação de disponibilidade.

## 🚀 Funcionalidades

- Autenticação baseada em JWT (JSON Web Tokens)
- Cadastro e gerenciamento de usuários (USER/ADMIN)
- Gerenciamento de locações (Leases)
- Verificação de disponibilidade de locações por período
- Autorização baseada em papéis (roles)
- Validação de entrada com Bean Validation
- Documentação interativa com Swagger/OpenAPI
- Testes de integração com Testcontainers

## 🛠️ Tecnologias

- **Java 21**
- **Spring Boot 3.3.4**
  - Spring Security
  - Spring Data JPA
  - Spring Validation
- **JWT** para autenticação
- **MySQL 8.1.0** (com suporte a Docker)
- **Gradle** (com suporte a Maven)
- **Lombok** para redução de código boilerplate
- **SpringDoc OpenAPI** para documentação
- **Testcontainers** para testes de integração

## 📋 Pré-requisitos

- Java 21 ou superior
- MySQL 8.1.0 ou superior (ou Docker para executar o container)
- Gradle 8.x
- Docker e Docker Compose (opcional, mas recomendado)

## 🚀 Como Executar

### 1. Usando Docker (Recomendado)

O projeto inclui um arquivo `docker-compose.yml` que configura automaticamente o MySQL 8.1.0:

```bash
# Iniciar os containers
docker-compose up -d

# Parar os containers
docker-compose down
```

O banco de dados estará disponível em:
- Host: localhost
- Porta: 3306
- Banco: reservas_db
- Usuário: root
- Senha: root

### 2. Executando a Aplicação

```bash
# Usando Gradle
./gradlew bootRun

# Ou construindo o JAR
./gradlew build
java -jar build/libs/reservas-api-0.0.1-SNAPSHOT.jar
```

A aplicação estará disponível em `http://localhost:8080`

## 🔐 Autenticação

A API usa JWT para autenticação. Você precisa obter um token JWT fazendo login e usá-lo nas requisições subsequentes no cabeçalho `Authorization`.

### Registrar um novo usuário
```http
POST /auth/register
Content-Type: application/json

{
  "name": "João Silva",
  "email": "joao@example.com",
  "password": "senha123",
  "cpf": "123.456.789-00",
  "phone": "11999999999"
}

# Resposta (201 Created)
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "João Silva",
  "email": "joao@example.com",
  "phone": "11999999999",
  "cpf": "123.456.789-00",
  "createdAt": "2025-10-26"
}
```

### Fazer login
```http
POST /auth/login
Content-Type: application/json

{
  "email": "joao@example.com",
  "password": "senha123"
}

# Resposta (200 OK)
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c",
  "expiresIn": 86400000
}
```

## 📚 Documentação da API

A documentação interativa da API está disponível através do Swagger UI em:
```
http://localhost:8080/swagger-ui.html
```

## 🏗️ Estrutura do Projeto

```
src/
├── main/
│   ├── java/com/reservas/api/
│   │   ├── config/       # Configurações da aplicação
│   │   ├── controller/   # Controladores da API
│   │   ├── entities/     # Entidades JPA
│   │   ├── repository/   # Repositórios Spring Data
│   │   ├── security/     # Configurações de segurança
│   │   └── service/      # Lógica de negócios
│   └── resources/        # Arquivos de configuração
└── test/                 # Testes
```

## 🔄 Endpoints Principais

### Autenticação (`/auth`)
- `POST /auth/register` - Registrar novo usuário (role USER por padrão)
- `POST /auth/login` - Realizar login e obter token JWT

### Usuários (`/user`)

#### Buscar usuário por ID
```http
GET /user/550e8400-e29b-41d4-a716-446655440000
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

# Resposta (200 OK)
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "João Silva",
  "email": "joao@example.com",
  "phone": "11999999999",
  "cpf": "123.456.789-00",
  "createdAt": "2025-10-26"
}
```

#### Atualizar usuário
```http
PUT /user/550e8400-e29b-41d4-a716-446655440000
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "name": "João Silva Atualizado",
  "email": "joao.novo@example.com",
  "phone": "11988888888"
}

# Resposta (200 OK)
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "João Silva Atualizado",
  "email": "joao.novo@example.com",
  "phone": "11988888888",
  "cpf": "123.456.789-00",
  "createdAt": "2025-10-26"
}
```

#### Deletar usuário
```http
DELETE /user/550e8400-e29b-41d4-a716-446655440000
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

# Resposta (204 No Content)
```

### Locações (`/leases`)

#### Listar locações disponíveis
```http
GET /leases/disponibles?startDate=2025-10-24&endDate=2025-10-31
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

# Resposta (200 OK)
[
  {
    "id": "6ba7b810-9dad-11d1-80b4-00c04fd430c8",
    "name": "Sala de Reunião 1",
    "type": "MEETING_ROOM",
    "description": "Sala climatizada com capacidade para 10 pessoas",
    "hourValue": 100.00,
    "maxTime": 8,
    "minTime": 1,
    "createdAt": "2025-01-15"
  },
  {
    "id": "6ba7b811-9dad-11d1-80b4-00c04fd430c9",
    "name": "Auditório Principal",
    "type": "AUDITORIUM",
    "description": "Auditório com capacidade para 100 pessoas",
    "hourValue": 500.00,
    "maxTime": 12,
    "minTime": 2,
    "createdAt": "2025-02-20"
  }
]
```

#### Criar nova locação (apenas ADMIN)
```http
POST /leases
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "name": "Sala de Treinamento",
  "type": "TRAINING_ROOM",
  "description": "Sala equipada para treinamentos",
  "hourValue": 150.00,
  "maxTime": 8,
  "minTime": 1
}

# Resposta (201 Created)
{
  "id": "6ba7b812-9dad-11d1-80b4-00c04fd430d0",
  "name": "Sala de Treinamento",
  "type": "TRAINING_ROOM",
  "description": "Sala equipada para treinamentos",
  "hourValue": 150.00,
  "maxTime": 8,
  "minTime": 1,
  "createdAt": "2025-10-26"
}
```

#### Listar todas as locações
```http
GET /leases
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

# Resposta (200 OK)
[
  {
    "id": "6ba7b810-9dad-11d1-80b4-00c04fd430c8",
    "name": "Sala de Reunião 1",
    "type": "MEETING_ROOM",
    "description": "Sala climatizada com capacidade para 10 pessoas",
    "hourValue": 100.00,
    "maxTime": 8,
    "minTime": 1,
    "createdAt": "2025-01-15"
  },
  {
    "id": "6ba7b811-9dad-11d1-80b4-00c04fd430c9",
    "name": "Auditório Principal",
    "type": "AUDITORIUM",
    "description": "Auditório com capacidade para 100 pessoas",
    "hourValue": 500.00,
    "maxTime": 12,
    "minTime": 2,
    "createdAt": "2025-02-20"
  }
]
```

#### Buscar locação por ID
```http
GET /leases/6ba7b810-9dad-11d1-80b4-00c04fd430c8
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

# Resposta (200 OK)
{
  "id": "6ba7b810-9dad-11d1-80b4-00c04fd430c8",
  "name": "Sala de Reunião 1",
  "type": "MEETING_ROOM",
  "description": "Sala climatizada com capacidade para 10 pessoas",
  "hourValue": 100.00,
  "maxTime": 8,
  "minTime": 1,
  "createdAt": "2025-01-15"
}
```

#### Atualizar locação (apenas ADMIN)
```http
PUT /leases/6ba7b810-9dad-11d1-80b4-00c04fd430c8
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "name": "Sala de Reunião 1 - Atualizada",
  "description": "Sala climatizada com capacidade para 12 pessoas",
  "hourValue": 120.00
}

# Resposta (200 OK)
{
  "id": "6ba7b810-9dad-11d1-80b4-00c04fd430c8",
  "name": "Sala de Reunião 1 - Atualizada",
  "type": "MEETING_ROOM",
  "description": "Sala climatizada com capacidade para 12 pessoas",
  "hourValue": 120.00,
  "maxTime": 8,
  "minTime": 1,
  "createdAt": "2025-01-15"
}
```

#### Deletar locação (apenas ADMIN)
```http
DELETE /leases/6ba7b810-9dad-11d1-80b4-00c04fd430c8
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

# Resposta (204 No Content)
```

## 🔒 Segurança

- Autenticação baseada em JWT
- Senhas armazenadas com BCrypt
- Proteção contra CSRF desabilitada para APIs REST
- CORS configurado para permitir requisições de origens diferentes
- Níveis de acesso baseados em roles (USER/ADMIN)

## 🧪 Testes

Para executar os testes:

```bash
./gradlew test
```

Os testes utilizam Testcontainers para criar um ambiente isolado com MySQL em container.

## 🐳 Executando com Docker

### Construir a imagem
```bash
docker build -t reservas-api .
```

### Executar o container
```bash
docker-compose up -d
```

## 🔒 Segurança

- Todas as rotas, exceto `/auth/**`, requerem autenticação via token JWT
- O token deve ser enviado no cabeçalho `Authorization: Bearer <token>`
- O token expira em 24 horas por padrão (configurável em `application.yml`)

## 🛠️ Estrutura do Projeto

```
src/
├── main/
│   │   ├── config/         # Configurações do Spring
│   │   ├── controller/     # Controladores REST
│   │   ├── dto/           # Objetos de transferência de dados
│   │   ├── entities/      # Entidades JPA
│   │   ├── repository/    # Repositórios JPA
│   │   ├── security/      # Configurações de segurança
│   │   └── service/       # Lógica de negócios
│   └── resources/
│       ├── application.yml # Configurações da aplicação

## Licença

Este projeto está licenciado sob a licença MIT - veja o arquivo [LICENSE](LICENSE) para detalhes.

## Contribuição

Contribuições são bem-vindas! Sinta-se à vontade para abrir issues e enviar pull requests.
- `GET /user` - Listar todos os usuários (apenas admin)
- `GET /user/{id}` - Obter usuário por ID
- `PUT /user/{id}` - Atualizar usuário
- `DELETE /user/{id}` - Excluir usuário (apenas se não houver reservas ativas)
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