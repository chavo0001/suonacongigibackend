# Suona con Gigi - Backend

REST backend for the **Suona con Gigi** platform, a project dedicated to managing a music community: users, musical profiles, live events, forums, artists, genres, instruments, and content moderation.

The application exposes JWT-protected JSON APIs, documented with Swagger/OpenAPI and designed to be consumed by a local frontend running on `http://localhost:4200`.

## Stack

- Java 21
- Spring Boot 3.5.14
- Spring Web
- Spring Security
- Spring Data JPA / Hibernate
- MySQL
- JWT with JJWT
- Bean Validation
- Lombok
- Thymeleaf for email templates
- Spring Mail
- Springdoc OpenAPI / Swagger UI
- Maven

## Main Features

- User registration, login, and JWT token verification
- User management and personal music profile
- Application roles: `USER` and `ADMIN`
- Event creation, browsing, registration, and moderation
- Event likes
- Forum with categories, threads, posts, and search functionality
- Music catalog management: artists, genres, and instruments
- Banned words moderation
- Email templates and verification email sending
- Standardized API responses using DTOs
- Centralized error handling
- Interactive Swagger documentation

## Requirements

- JDK 21
- Maven 3.9+
- MySQL 8+
- Local database named `suonacongigi`

## Configuration

The application uses external configuration for sensitive credentials. Before starting the application, create the database and define at least the following environment variables:

```env
DB_USERNAME=your_mysql_username
DB_PASSWORD=your_mysql_password
JWT_SECRET=a_long_and_secure_secret_key
```

Configured database URL:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/suonacongigi?useSSL=false&serverTimezone=Europe/Rome&allowPublicKeyRetrieval=true
```

The backend runs on port `8080` with the following context path:

```text
/suonacongigi
```

Local base URL:

```text
http://localhost:8080/suonacongigi
```

## Local Startup

Clone the repository, enter the backend folder, and install dependencies:

```bash
mvn clean install
```

Start the application:

```bash
mvn spring-boot:run
```

In PowerShell, you can set environment variables for the current session like this:

```powershell
$env:DB_USERNAME="root"
$env:DB_PASSWORD="password"
$env:JWT_SECRET="replace-this-string-with-a-long-secret"
mvn spring-boot:run
```

## Build

To create the executable JAR:

```bash
mvn clean package
```

Run the JAR:

```bash
java -jar target/suona-gigi-0.0.1-SNAPSHOT.jar
```

## API Documentation

Swagger UI is available after startup at:

```text
http://localhost:8080/suonacongigi/swagger-ui.html
```

OpenAPI JSON:

```text
http://localhost:8080/suonacongigi/v3/api-docs
```

To use protected endpoints through Swagger, log in and insert the JWT token in the **Authorize** button using the Bearer scheme.

## Main Endpoints

All endpoints are prefixed with:

```text
http://localhost:8080/suonacongigi
```

| Area | Endpoint | Description |
| --- | --- | --- |
| Auth | `/api/auth/register` | User registration |
| Auth | `/api/auth/login` | Login and JWT generation |
| Auth | `/api/auth/verify` | Token verification |
| Users | `/api/users/me` | Read and update personal profile |
| Users | `/api/users` | User list, admin only |
| Events | `/api/events` | Event list and creation |
| Events | `/api/events/{id}` | Event details, update, and deletion |
| Events | `/api/events/{id}/register` | Event registration or cancellation |
| Events | `/api/events/{id}/likes` | Read, add, and remove likes |
| Forum | `/api/forum/categories` | Forum categories |
| Forum | `/api/forum/categories/{id}/threads` | Threads of a category |
| Forum | `/api/forum/threads` | Thread creation |
| Forum | `/api/forum/threads/{id}` | Thread details or deletion |
| Forum | `/api/forum/threads/{id}/posts` | Post creation |
| Forum | `/api/forum/search` | Forum search |
| Catalogs | `/api/artists` | Artists |
| Catalogs | `/api/genres` | Music genres |
| Catalogs | `/api/instruments` | Musical instruments |
| Moderation | `/api/users/me/censura` | Management of censored words/preferences |
| Email | `/api/email/verification` | Verification email templates |

Public `GET` endpoints include events, artists, genres, and instruments. Write operations require authentication, while some actions are restricted to the `ADMIN` role.

## Database and Seed Data

The property:

```properties
app.db.seed-on-start=false
```

controls the seeding behavior. In development environments it can be set to `true` to recreate and populate the database with demo data. In production it should remain disabled.

## Security

- Stateless JWT authentication
- Passwords encrypted with BCrypt
- Role management through Spring Security
- Sensitive endpoints protected with `@PreAuthorize`
- CORS enabled for `http://localhost:4200`
- Stack traces hidden from error responses

## Project Structure

```text
src/main/java/it/generation/suonacongigi
|-- bootstrap      # Data seeding and initialization
|-- config         # Security, OpenAPI, Thymeleaf, and initialization configs
|-- controller     # REST controllers
|-- dto            # Request/response DTOs
|-- model          # JPA entities
|-- repository     # Spring Data repositories
|-- security       # JWT filters, utilities, and UserDetailsService
|-- service        # Business logic
`-- util           # Shared utilities
```

## Tests

Run tests:

```bash
mvn test
```

## Deployment Notes

Before publishing or deploying:

- configure `DB_USERNAME`, `DB_PASSWORD`, and `JWT_SECRET` on the server
- use a long and private `JWT_SECRET`
- disable automatic seeding
- configure real SMTP credentials outside the source code
- update CORS origins according to the production frontend domain
