# 📚 Library System - REST API Backend

**Autor:** Tomáš Suchanek | **Předmět:** 7OPR3 | **Java:** 21 LTS | **Framework:** Spring Boot 3.5.7

---

## 🚀 Spuštění

### Prerequisites

- Java 21 LTS
- MySQL 8.0+
- Maven 3.6+

### Setup Database

```bash
# Create database
mysql -u root -p
CREATE DATABASE librarydb;
EXIT;

# Run SQL schema
mysql -u root -p librarydb < database.sql
```

### Start Backend Server

```bash
cd library
./mvnw spring-boot:run
# Server běží na http://localhost:8080
```

### Configuration

Edit `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/librarydb
spring.datasource.username=root
spring.datasource.password=rootroot
```

---

## 📖 API Dokumentace

### 🌐 Swagger/OpenAPI Documentation

**Interactive API documentation is available at:**

```
http://localhost:8080/swagger-ui/index.html
```

**OpenAPI Specification:**
- JSON: `http://localhost:8080/v3/api-docs`
- YAML: `http://localhost:8080/v3/api-docs.yaml`

Swagger UI allows you to:
- ✅ Browse all API endpoints
- ✅ View request/response schemas
- ✅ Test endpoints directly in the browser
- ✅ Authenticate with JWT token using the "Authorize" button
- ✅ See real-time examples and validation rules

### 🔐 Authentication

All endpoints except `/api/users/login` and `/api/users/register` require JWT token:

```
Authorization: Bearer <jwt_token>
```

### BOOKS API

| Metoda | Endpoint          | Popis              | Auth |
| ------ | ----------------- | ------------------ | ---- |
| GET    | `/api/books`      | Všechny knihy      | ❌   |
| GET    | `/api/books/{id}` | Kniha podle ID     | ❌   |
| POST   | `/api/books`      | Vytvořit knihu     | ✅   |
| POST   | `/api/books/bulk` | Vytvořit více knih | ✅   |
| PUT    | `/api/books/{id}` | Aktualizovat knihu | ✅   |
| DELETE | `/api/books/{id}` | Smazat knihu       | ✅   |

**Příklad - Vytvořit knihu:**

```bash
curl -X POST http://localhost:8080/api/books \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "title": "1984",
    "author": "George Orwell",
    "publicationYear": 1949,
    "isbn": "978-0451524935",
    "content": "Dystopické dílo"
  }'
```

---

### USERS API

| Metoda | Endpoint              | Popis                  | Auth     |
| ------ | --------------------- | ---------------------- | -------- |
| GET    | `/api/users`          | Všichni uživatelé      | ✅ Admin |
| GET    | `/api/users/{id}`     | Uživatel podle ID      | ✅       |
| POST   | `/api/users`          | Vytvořit uživatele     | ✅ Admin |
| POST   | `/api/users/login`    | Přihlášení             | ❌       |
| POST   | `/api/users/register` | Registrace             | ❌       |
| PUT    | `/api/users/{id}`     | Aktualizovat uživatele | ✅       |
| DELETE | `/api/users/{id}`     | Smazat uživatele       | ✅ Admin |

**Příklad - Registrace:**

```bash
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Jan",
    "surname": "Novák",
    "email": "jan@example.com",
    "address": "Ulice 123",
    "city": "Praha",
    "password": "heslo123"
  }'
```

**Příklad - Login:**

```bash
curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "jan@example.com",
    "password": "heslo123"
  }'
```

Response:

```json
{
  "id": 1,
  "name": "Jan",
  "email": "jan@example.com",
  "role": "ROLE_USER",
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

---

### LOANS API

| Metoda | Endpoint                 | Popis                 | Auth     |
| ------ | ------------------------ | --------------------- | -------- |
| GET    | `/api/loans`             | Všechny výpůjčky      | ✅ Admin |
| GET    | `/api/loans/{id}`        | Výpůjčka podle ID     | ✅       |
| POST   | `/api/loans/borrow`      | Půjčit knihu          | ✅       |
| POST   | `/api/loans/return/{id}` | Vrátit knihu          | ✅       |
| PUT    | `/api/loans/{id}`        | Aktualizovat výpůjčku | ✅       |
| DELETE | `/api/loans/{id}`        | Smazat výpůjčku       | ✅       |

**Příklad - Půjčit knihu:**

```bash
curl -X POST http://localhost:8080/api/loans/borrow \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "userId": 1,
    "bookId": 1
  }'
```

**Příklad - Vrátit knihu:**

```bash
curl -X POST http://localhost:8080/api/loans/return/1 \
  -H "Authorization: Bearer <token>"
```

---

## 🛠️ Technologie

- **Java 21 LTS** - Nejnovější LTS verze
- **Spring Boot 3.5.7** - Framework
- **Spring Data JPA** - Database access
- **Spring Security** - Authentication & Authorization
- **MySQL 8.0** - Databáze
- **JWT (jjwt 0.12.3)** - Token-based authentication
- **BCrypt** - Password hashing
- **Lombok** - Boilerplate reduction
- **Maven** - Build management
- **SLF4J** - Logging
- **Springdoc OpenAPI 2.8.4** - API Documentation (Swagger UI)
- **Hibernate Validator** - Input validation
- **Jackson** - JSON serialization

---

## 🔒 Bezpečnost

✅ **Password Security**

- BCrypt hashovací algoritmus
- Hesla se nekdy neukládají v plain textu
- Password encoding při registraci a update

✅ **Authentication & Authorization**

- JWT (JSON Web Token) based authentication
- Token expiration (24 hodin)
- Role-based access control (ROLE_USER, ROLE_ADMIN)
- Protected endpoints require valid token

✅ **Input Validation**

- Jakarta validation annotations
- Email validation
- Password length validation (min 6 chars)
- All inputs validated before database save

✅ **Error Handling**

- GlobalExceptionHandler s @ExceptionHandler
- Custom ErrorResponse DTO
- Proper HTTP status codes
- Detailed error messages

---

## 📊 Datový Model

### User

```java
- uid: String (PK, UUID)
- name: String (required, 2-100 chars)
- surname: String (required, 2-100 chars)
- email: String (required, unique, valid email)
- address: String (required, 5-255 chars)
- city: String (required, 2-100 chars)
- password: String (required, 6+ chars, BCrypt hashed)
- role: Enum (ROLE_USER, ROLE_ADMIN)
```

### Book

```java
- id: Integer (PK)
- title: String (required)
- author: String (required)
- content: String
- publicationYear: Integer (required, >= 1000)
- isbn: String (required, max 17 chars)
- available: Boolean (default: true)
- authors: Set<Author> (ManyToMany)
```

### Author

```java
- id: Integer (PK)
- name: String (required)
- surname: String (required)
- birthYear: Integer
- books: Set<Book> (ManyToMany)
```

### Loan

```java
- id: Integer (PK)
- user: User (ManyToOne, FK to uid)
- book: Book (ManyToOne, FK to id)
- loanDate: LocalDate
- returnDate: LocalDate (nullable)
- status: String (ACTIVE, RETURNED)
```

### Relationships

```
User 1 ←→ N Loan N ←→ 1 Book
Book N ←→ N Author (M:N relationship via book_author join table)
```

---

## 📝 Logování

Comprehensive logging na všech úrovních:

**Konfiguraci v `application.properties`:**

```properties
logging.level.com.example.library=DEBUG
logging.level.com.example.library.controller=INFO
logging.level.com.example.library.service=INFO
logging.file.name=logs/library.log
```

**Co se loguje:**

- ✅ Všechny HTTP requests (metoda, endpoint, user)
- ✅ Úspěšné operace (create, update, delete)
- ✅ Selhání (not found, validation errors, exceptions)
- ✅ Autentizace (login success, failed attempts)
- ✅ JWT token generation a validation
- ✅ Database operations

Log soubor: `logs/library.log` (10MB rotation, 10-day history)

---

## 🎯 Nové Funkce a Vylepšení

### ✨ Swagger/OpenAPI 3 Integration

Kompletní interaktivní API dokumentace:

- **Swagger UI**: `http://localhost:8080/swagger-ui/index.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`
- **OpenAPI YAML**: `http://localhost:8080/v3/api-docs.yaml`

**Featury:**
- ✅ Automatická generace dokumentace z kódu
- ✅ Interactive API testing přímo v prohlížeči
- ✅ JWT authentication support (Authorize button)
- ✅ Request/response schémata a příklady
- ✅ Validation rules a error handling dokumentace
- ✅ Veřejný přístup bez autentizace pro prohlížení

### 📄 Pagination Support

Všechny list endpointy podporují stránkování:

**Query Parameters:**
- `page` - Číslo stránky (default: 0)
- `size` - Počet položek na stránku (default: 10)
- `sortBy` - Pole pro řazení (default: "id")
- `sortDirection` - Směr řazení: ASC/DESC (default: varies by endpoint)

**Příklad:**
```bash
GET /api/books?page=0&size=20&sortBy=title&sortDirection=ASC
GET /api/users?page=1&size=5&sortBy=email&sortDirection=DESC
GET /api/loans?page=0&size=10&sortBy=loanDate&sortDirection=DESC
```

**Response Format:**
```json
{
  "content": [...],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "sort": {...}
  },
  "totalElements": 50,
  "totalPages": 5,
  "last": false,
  "first": true,
  "numberOfElements": 10
}
```

### 🔐 Modern Spring Security Configuration

- ✅ **SecurityFilterChain** - Modern approach (no deprecated WebSecurityConfigurerAdapter)
- ✅ **JWT Authentication Filter** - Token-based auth with automatic validation
- ✅ **Public Swagger Endpoints** - Documentation accessible without login
- ✅ **CSRF Disabled** - Appropriate for REST API
- ✅ **CORS Configuration** - Support for frontend integration
- ✅ **Role-Based Access Control** - ROLE_ADMIN, ROLE_USER

### 🆔 UUID for User IDs

Uživatelské ID používá UUID místo Integer:

- **Type**: String (UUID format)
- **Generation**: `@GeneratedValue(strategy = GenerationType.UUID)`
- **Benefits**: 
  - Better scalability
  - Distributed system friendly
  - No sequential ID guessing
  - Global uniqueness

**API Impact:**
- User endpoints use UUID: `/api/users/{uuid}`
- Login/Register returns UUID in response
- JWT token contains UUID as subject
- Loan operations use UUID for user references

### 📚 Author Entity & M:N Relationship

Přidána entita Author s many-to-many vztahem ke knihám:

**Author API:**
- `GET /api/authors` - List all authors (pagination)
- `GET /api/authors/{id}` - Get author by ID
- `POST /api/authors` - Create author
- `PUT /api/authors/{id}` - Update author
- `DELETE /api/authors/{id}` - Delete author

**Book-Author Relationship:**
- Many-to-many vztah (kniha může mít více autorů, autor více knih)
- Join table: `book_author`
- API podporuje přiřazování autorů ke knihám

### 📊 Comprehensive Logging

Rozšířené logování všech operací:

- **Request/Response logging** - Všechny HTTP requesty
- **Authentication events** - Login attempts, JWT validation
- **Database operations** - CRUD operations s detaily
- **Error tracking** - Exception stack traces
- **Performance metrics** - Query execution times

**Log Levels:**
```properties
com.example.library=DEBUG
com.example.library.controller=INFO
com.example.library.service=INFO
com.example.library.security=DEBUG
```

**Log File:**
- Path: `logs/library.log`
- Rotation: 10MB per file
- History: 10 days
- Format: `[timestamp] [thread] [level] [logger] - message`

### 🗄️ Database Reset Scripts

SQL skripty pro snadnou správu databáze:

- **`reset_database.sql`** - Kompletní reset s test daty
  - Drops all tables (správné pořadí kvůli FK)
  - Recreates schema with UUID
  - Inserts test users, books, authors, loans
  
- **`drop_tables.sql`** - Pouze drop všech tabulek

**Usage:**
```bash
mysql -u root -p < reset_database.sql
```

### ✅ Validation & Error Handling

Rozšířené validace a error handling:

- **Input Validation**: Jakarta validation annotations
- **Custom Validators**: Email, ISBN, date ranges
- **Global Exception Handler**: Centralizované error handling
- **HTTP Status Codes**: Správné kódy (200, 201, 400, 401, 403, 404, 409, 500)
- **Error Response DTO**: Konzistentní formát error responses
- **Validation Messages**: Lokalizované chybové hlášky

---

## 🧪 Testing Endpoints

### s Bruno/Postman

Import kolekce z `OPR3-Library/` složky:

- `users/Login user.bru` - Login
- `users/Create new user.bru` - Create user
- `books/Get all books.bru` - Get books
- `books/Post one book.bru` - Create book
- `loans/Borrow a book.bru` - Borrow book
- `loans/Return a loan.bru` - Return book

**Nebo použij Swagger UI** pro přímé testování v prohlížeči:
```
http://localhost:8080/swagger-ui/index.html
```

---

## 🏗️ Vícevrstvá Architektura

```
Controller Layer
     ↓
Service Layer
     ↓
Repository Layer
     ↓
Database Layer
```

- **Controllers** - HTTP endpoints, request handling
- **Services** - Business logic, validation
- **Repositories** - Database queries (Spring Data JPA)
- **Models** - Entity definitions with JPA annotations
- **DTOs** - Data Transfer Objects (LoginRequest, LoginResponse)
- **Utils** - JWT utility, helpers
- **Config** - Spring Security, CORS, Bean configuration

---

## ⚙️ CORS Configuration

```java
Allowed Origins: http://localhost:5173
Allowed Methods: GET, POST, PUT, DELETE, OPTIONS
Allowed Headers: *
Max Age: 3600 seconds
```

---

**Status:** ✅ Hotovo | **Datum:** 4. ledna 2026 | **Swagger:** [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
