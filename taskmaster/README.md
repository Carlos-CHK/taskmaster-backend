 TaskMaster (Spring Boot + Java 17 + Docker)

Prosta implementacja REST API zgodna z tabelą endpointów z dokumentacji (CRUD + status + assignee + soft delete).

 Security: JWT jest egzekwowany przez Spring Security (Filter Chain + JwtAuthenticationFilter). Każde żądanie poza `/api/auth/**` wymaga nagłówka `Authorization: Bearer <token>`.

 Uruchomienie (Docker Compose)
1) Skopiuj `.env.example` jako `.env` i ustaw własne wartości.

2) Uruchom:
bash
docker-compose up --build


Aplikacja: http://localhost:8080

Swagger UI: http://localhost:8080/swagger-ui/index.html

 Endpointy
 Auth
- `POST /api/auth/register` -> 201 Created, 409 jeśli email już istnieje
- `POST /api/auth/login` -> 200 OK lub 401 Unauthorized

 Tasks (wymagają Bearer token)
- `GET /api/tasks` (opcjonalne filtry: `?status=TODO&priority=MEDIUM`)
- `POST /api/tasks` -> 201 Created (walidacja: title wymagany, dueDate nie w przeszłości)
- `GET /api/tasks/{id}`
- `PUT /api/tasks/{id}`
- `PATCH /api/tasks/{id}/status` (zły status -> 400)
- `PATCH /api/tasks/{id}/assignee` (assignee musi istnieć w bazie użytkowników, inaczej 404)
- `DELETE /api/tasks/{id}` (soft delete)

 Przykłady (curl)
bash
 Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"Password123!"}'

 Login (zwraca JWT)
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"Password123!"}'

 Zapisz token (bash)
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"Password123!"}' | jq -r .token)

 Create task
curl -X POST http://localhost:8080/api/tasks \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"Zadanie 1","description":"Opis","priority":"MEDIUM","dueDate":"2026-02-20"}'

 List tasks with filters
curl "http://localhost:8080/api/tasks?status=TODO&priority=MEDIUM" \
  -H "Authorization: Bearer $TOKEN"

 Update status
curl -X PATCH http://localhost:8080/api/tasks/1/status \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"status":"IN_PROGRESS"}'

 Assign (requires registered email)
curl -X PATCH http://localhost:8080/api/tasks/1/assignee \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"assignee":"user@example.com"}'

 Soft delete
curl -X DELETE http://localhost:8080/api/tasks/1 \
  -H "Authorization: Bearer $TOKEN"



 Uruchomienie bez PostgreSQL (profil dev, H2)
Jeśli nie masz Dockera ani lokalnego Postgresa, uruchom aplikację na bazie H2:

bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev


Testy (`mvn test`) używają H2 automatycznie (src/test/resources/application.properties).
