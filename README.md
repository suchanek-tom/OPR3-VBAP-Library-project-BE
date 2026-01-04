# 📚 Library System - REST API Backend

**Author:** Tomáš Suchanek | **Course:** 7OPR3 + 7VBAP | **Java:** 21 LTS | **Framework:** Spring Boot 3.5.7

---

## 🚀 Getting Started

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
# Server runs at http://localhost:8080
```

### Configuration

Edit `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/librarydb
spring.datasource.username=root
spring.datasource.password=rootroot
```

---

## 📖 API Documentation

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

| Method | Endpoint          | Description           | Auth |
| ------ | ----------------- | --------------------- | ---- |
| GET    | `/api/books`      | All books             | ❌   |
| GET    | `/api/books/{id}` | Book by ID            | ❌   |
| POST   | `/api/books`      | Create book           | ✅   |
| POST   | `/api/books/bulk` | Create multiple books | ✅   |
| PUT    | `/api/books/{id}` | Update book           | ✅   |
| DELETE | `/api/books/{id}` | Delete book           | ✅   |

**Example - Create book:**

```bash
curl -X POST http://localhost:8080/api/books \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "title": "1984",
    "author": "George Orwell",
    "publicationYear": 1949,
    "isbn": "978-0451524935",
    "content": "Dystopian novel"
  }'
```

---

### USERS API

| Method | Endpoint              | Description | Auth     |
| ------ | --------------------- | ----------- | -------- |
| GET    | `/api/users`          | All users   | ✅ Admin |
| GET    | `/api/users/{id}`     | User by ID  | ✅       |
| POST   | `/api/users`          | Create user | ✅ Admin |
| POST   | `/api/users/login`    | Login       | ❌       |
| POST   | `/api/users/register` | Register    | ❌       |
| PUT    | `/api/users/{id}`     | Update user | ✅       |
| DELETE | `/api/users/{id}`     | Delete user | ✅ Admin |

**Example - Register:**

```bash
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John",
    "surname": "Doe",
    "email": "john@example.com",
    "address": "Street 123",
    "city": "Prague",
    "password": "password123"
  }'
```

**Example - Login:**

```bash
curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "password123"
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

| Method | Endpoint                 | Description | Auth     |
| ------ | ------------------------ | ----------- | -------- |
| GET    | `/api/loans`             | All loans   | ✅ Admin |
| GET    | `/api/loans/{id}`        | Loan by ID  | ✅       |
| POST   | `/api/loans/borrow`      | Borrow book | ✅       |
| POST   | `/api/loans/return/{id}` | Return book | ✅       |
| PUT    | `/api/loans/{id}`        | Update loan | ✅       |
| DELETE | `/api/loans/{id}`        | Delete loan | ✅       |

**Example - Borrow book:**

```bash
curl -X POST http://localhost:8080/api/loans/borrow \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "userId": 1,
    "bookId": 1
  }'
```

**Example - Return book:**

```bash
curl -X POST http://localhost:8080/api/loans/return/1 \
  -H "Authorization: Bearer <token>"
```

---

## 🛠️ Technologies

- **Java 21 LTS** - Latest LTS version
- **Spring Boot 3.5.7** - Framework
- **Spring Data JPA** - Database access
- **Spring Security** - Authentication & Authorization
- **MySQL 8.0** - Database
- **JWT (jjwt 0.12.3)** - Token-based authentication
- **BCrypt** - Password hashing
- **Lombok** - Boilerplate reduction
- **Maven** - Build management
- **SLF4J** - Logging
- **Springdoc OpenAPI 2.8.4** - API Documentation (Swagger UI)
- **Hibernate Validator** - Input validation
- **Jackson** - JSON serialization

---

## 🔒 Security

✅ **Password Security**

- BCrypt hashing algorithm
- Passwords never stored in plain text
- Password encoding during registration and update

✅ **Authentication & Authorization**

- JWT (JSON Web Token) based authentication
- Token expiration (24 hours)
- Role-based access control (ROLE_USER, ROLE_ADMIN)
- Protected endpoints require valid token

✅ **Input Validation**

- Jakarta validation annotations
- Email validation
- Password length validation (min 6 chars)
- All inputs validated before database save

✅ **Error Handling**

- GlobalExceptionHandler with @ExceptionHandler
- Custom ErrorResponse DTO
- Proper HTTP status codes
- Detailed error messages

---

## 📊 Data Model

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

## 📝 Logging

Comprehensive logging at all levels:

**Configuration in `application.properties`:**

```properties
logging.level.com.example.library=DEBUG
logging.level.com.example.library.controller=INFO
logging.level.com.example.library.service=INFO
logging.file.name=logs/library.log
```

**What is logged:**

- ✅ All HTTP requests (method, endpoint, user)
- ✅ Successful operations (create, update, delete)
- ✅ Failures (not found, validation errors, exceptions)
- ✅ Authentication (login success, failed attempts)
- ✅ JWT token generation and validation
- ✅ Database operations

Log file: `logs/library.log` (10MB rotation, 10-day history)

---

## 🎯 New Features and Improvements

### ✨ Swagger/OpenAPI 3 Integration

Complete interactive API documentation:

- **Swagger UI**: `http://localhost:8080/swagger-ui/index.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`
- **OpenAPI YAML**: `http://localhost:8080/v3/api-docs.yaml`

**Features:**

- ✅ Automatic documentation generation from code
- ✅ Interactive API testing directly in browser
- ✅ JWT authentication support (Authorize button)
- ✅ Request/response schemas and examples
- ✅ Validation rules and error handling documentation
- ✅ Public access without authentication for viewing

### 📄 Pagination Support

All list endpoints support pagination:

**Query Parameters:**

- `page` - Page number (default: 0)
- `size` - Items per page (default: 10)
- `sortBy` - Field to sort by (default: "id")
- `sortDirection` - Sort direction: ASC/DESC (default: varies by endpoint)

**Example:**

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

User IDs use UUID instead of Integer:

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

Added Author entity with many-to-many relationship to books:

**Author API:**

- `GET /api/authors` - List all authors (pagination)
- `GET /api/authors/{id}` - Get author by ID
- `POST /api/authors` - Create author
- `PUT /api/authors/{id}` - Update author
- `DELETE /api/authors/{id}` - Delete author

**Book-Author Relationship:**

- Many-to-many relationship (book can have multiple authors, author multiple books)
- Join table: `book_author`
- API supports assigning authors to books

### 📊 Comprehensive Logging

Extended logging of all operations:

- **Request/Response logging** - All HTTP requests
- **Authentication events** - Login attempts, JWT validation
- **Database operations** - CRUD operations with details
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

SQL scripts for easy database management:

- **`reset_database.sql`** - Complete reset with test data
  - Drops all tables (correct order due to FK)
  - Recreates schema with UUID
  - Inserts test users, books, authors, loans
- **`drop_tables.sql`** - Only drops all tables

**Usage:**

```bash
mysql -u root -p < reset_database.sql
```

### ✅ Validation & Error Handling

Extended validation and error handling:

- **Input Validation**: Jakarta validation annotations
- **Custom Validators**: Email, ISBN, date ranges
- **Global Exception Handler**: Centralized error handling
- **HTTP Status Codes**: Correct codes (200, 201, 400, 401, 403, 404, 409, 500)
- **Error Response DTO**: Consistent error response format
- **Validation Messages**: Localized error messages

---

## 🧪 Testing Endpoints

### With Bruno/Postman

Import collection from `OPR3-Library/` folder:

- `users/Login user.bru` - Login
- `users/Create new user.bru` - Create user
- `books/Get all books.bru` - Get books
- `books/Post one book.bru` - Create book
- `loans/Borrow a book.bru` - Borrow book
- `loans/Return a loan.bru` - Return book

**Or use Swagger UI** for direct testing in browser:

```
http://localhost:8080/swagger-ui/index.html
```

---

## 🏗️ Multi-Layer Architecture

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

**Status:** ✅ Done | **Date:** January 4, 2026 | **Swagger:** [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
