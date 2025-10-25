# Reservas API

Uma API RESTful para gerenciamento de reservas de imóveis, construída com Spring Boot 3.x e Java 17. Esta aplicação fornece endpoints para autenticação de usuários, gerenciamento de propriedades e sistema de reservas.

## 🚀 Funcionalidades

- Autenticação baseada em JWT
- Cadastro e gerenciamento de usuários
- Gerenciamento de propriedades (Locações)
- Sistema de reservas com verificação de disponibilidade
- Autorização baseada em papéis (roles)
- Validação de entrada
- Tratamento global de exceções

## 🛠️ Tecnologias

- Java 17
- Spring Boot 3.x
- Spring Security
- JWT (JSON Web Tokens)
- MySQL 8.0
- Maven/Gradle
- JPA/Hibernate
- Lombok
- MapStruct
- Bean Validation

## 📋 Pré-requisitos

- Java 17 ou superior
- MySQL 8.0 ou superior
- Maven ou Gradle
- Docker (opcional, para execução em container)

## 🚀 Como Executar

### 1. Configuração do Banco de Dados

#### Usando Docker (Recomendado)
```bash
docker run --name=reservas-db -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=reservas_db -p 3306:3306 -d mysql:8.0
```

#### Configuração Manual
1. Instale o MySQL 8.0+ em sua máquina
2. Crie um banco de dados chamado `reservas_db`
3. Configure o usuário e senha conforme o arquivo `application.yml`

### 2. Configuração da Aplicação

O arquivo `application.yml` já está configurado com as seguintes configurações padrão:

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/reservas_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    username: root
    password: root
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect

jwt:
  secret-key: cdcbe42a8d697568a59297eaa62fb60da018dfb0ef2716b45637fd24736097d5
  expiration-ms: 86400000  # 24 horas
```

### 3. Executando a Aplicação

```bash
# Usando Maven
./mvnw spring-boot:run

# Ou usando Gradle
./gradlew bootRun
```

A aplicação estará disponível em `http://localhost:8080`

## 🔐 Autenticação

A API usa JWT para autenticação. Você precisa obter um token JWT fazendo login e usá-lo nas requisições subsequentes no cabeçalho `Authorization`.

### Registrar um novo usuário
```http
POST /auth/register
Content-Type: application/json

{
  "firstName": "João",
  "lastName": "Silva",
  "email": "joao@example.com",
  "password": "senha123",
  "role": "USER"
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
```

**Resposta de sucesso:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 86400000,
  "user": {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "firstName": "João",
    "lastName": "Silva",
    "email": "joao@example.com",
    "role": "USER"
  }
}
```

## 📚 Documentação da API

### Usuários (Requer autenticação)

#### Obter usuário por ID
```http
GET /user/{id}
```

#### Atualizar usuário
```http
PUT /user/{id}
Content-Type: application/json

{
  "firstName": "João Atualizado",
  "lastName": "Silva",
  "email": "joao.novo@example.com"
}
```

#### Deletar usuário
```http
DELETE /user/{id}
```

### Locações (Imóveis)

#### Listar todas as locações
```http
GET /leases
```

#### Obter locação por ID
```http
GET /leases/{id}
```

#### Listar locações disponíveis
```http
GET /leases/disponibles?startDate=2025-10-24&endDate=2025-10-31
```

#### Criar nova locação (Admin)
```http
POST /leases
Content-Type: application/json

{
  "title": "Casa na Praia",
  "description": "Linda casa de frente para o mar",
  "dailyRate": 300.00,
  "maxPeople": 6,
  "address": "Av. Beira Mar, 1000"
}
```

#### Atualizar locação (Admin)
```http
PUT /leases/{id}
```

#### Deletar locação (Admin)
```http
DELETE /leases/{id}
```

#### Reservar locação
```http
POST /leases/hire-lease/{id}/{userId}?startDate=2025-10-24&endDate=2025-10-31
```

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
│   ├── java/com/reservas/api/
│   │   ├── config/         # Configurações do Spring
│   │   ├── controller/     # Controladores REST
│   │   ├── dto/           # Objetos de transferência de dados
│   │   ├── entities/      # Entidades JPA
│   │   ├── repository/    # Repositórios JPA
│   │   ├── security/      # Configurações de segurança
│   │   └── service/       # Lógica de negócios
│   └── resources/
│       ├── application.yml # Configurações da aplicação
```

## 📄 Licença

Este projeto está licenciado sob a licença MIT - veja o arquivo [LICENSE](LICENSE) para detalhes.

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