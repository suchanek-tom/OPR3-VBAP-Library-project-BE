# 📚 Library System - REST API

**Autor:** Tomáš Suchanek | **Předmět:** 7OPR3 | **Java:** 21 LTS | **Framework:** Spring Boot 3.5.7

---

## 🚀 Spuštění

```bash
./mvnw spring-boot:run
# Server běží na http://localhost:8080
```

---

## 📖 API Requesty

### BOOKS API

| Metoda | Endpoint | Popis |
|--------|----------|-------|
| GET | `/api/books` | Všechny knihy |
| GET | `/api/books/{id}` | Kniha podle ID |
| POST | `/api/books` | Vytvořit knihu |
| POST | `/api/books/bulk` | Vytvořit více knih |
| PUT | `/api/books/{id}` | Aktualizovat knihu |
| DELETE | `/api/books/{id}` | Smazat knihu |

**Příklad - Vytvořit knihu:**
```bash
curl -X POST http://localhost:8080/api/books \
  -H "Content-Type: application/json" \
  -d '{
    "title": "1984",
    "author": "George Orwell",
    "publicationYear": 1949,
    "isbn": "978-0451524935",
    "content": "Dystopické dílo",
    "available": true
  }'
```

---

### USERS API

| Metoda | Endpoint | Popis |
|--------|----------|-------|
| GET | `/api/users` | Všichni uživatelé |
| GET | `/api/users/{id}` | Uživatel podle ID |
| POST | `/api/users` | Vytvořit uživatele |
| POST | `/api/users/login` | Přihlášení |
| PUT | `/api/users/{id}` | Aktualizovat uživatele |
| DELETE | `/api/users/{id}` | Smazat uživatele |

**Příklad - Vytvořit uživatele:**
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Jan",
    "surname": "Novák",
    "email": "jan@example.com",
    "address": "Ulice 123",
    "city": "Praha",
    "password": "heslo123",
    "role": "ROLE_USER"
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

---

### LOANS API

| Metoda | Endpoint | Popis |
|--------|----------|-------|
| GET | `/api/loans` | Všechny výpůjčky |
| GET | `/api/loans/{id}` | Výpůjčka podle ID |
| POST | `/api/loans/borrow` | Půjčit knihu |
| POST | `/api/loans/return/{id}` | Vrátit knihu |
| PUT | `/api/loans/{id}` | Aktualizovat výpůjčku |
| DELETE | `/api/loans/{id}` | Smazat výpůjčku |

**Příklad - Půjčit knihu:**
```bash
curl -X POST http://localhost:8080/api/loans/borrow \
  -H "Content-Type: application/json" \
  -d '{
    "user": { "id": 1 },
    "book": { "id": 1 }
  }'
```

**Příklad - Vrátit knihu:**
```bash
curl -X POST http://localhost:8080/api/loans/return/1
```

---

## 💾 H2 Database Console

**URL:** http://localhost:8080/h2-console  
**JDBC URL:** `jdbc:h2:mem:librarydb`  
**Username:** `sa`

---

## 📊 Technologie

- Java 21 LTS
- Spring Boot 3.5.7
- Spring Data JPA
- H2 Database
- Lombok
- Maven

---

**Status:** ✅ Hotovo | **Datum:** 8. listopadu 2025
