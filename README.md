# Survey Application

A comprehensive survey management system built with Spring Boot (backend) and Vue.js (frontend). The application allows creating, managing, and analyzing surveys with various question types and user participation tracking.

## Features

- User authentication and authorization (JWT-based)
- Survey creation and management
- Multiple question types support
- Answer collection and analytics
- Admin dashboard with advanced statistics
- Elasticsearch integration for powerful searching
- Reactive programming model using Spring WebFlux

## Technology Stack

### Backend

- Java 17
- Spring Boot 3.4.4
- Spring WebFlux (reactive programming)
- Spring Security with JWT authentication
- Spring Data R2DBC (reactive database access)
- PostgreSQL (database)
- Elasticsearch (search capabilities)
- Log4j2 (logging)
- Swagger/OpenAPI (API documentation)

### Frontend

- Vue.js
- TypeScript
- Vite (build tool)

## Prerequisites

- JDK 17 or higher
- Maven 3.6+ or use the provided Maven wrapper
- Docker and Docker Compose
- Node.js 16+ and npm (for frontend development)

## Setup and Running

### Environment Variables

Create a `.env` file in the root directory with the following variables:

```
DB_URL=r2dbc:postgresql://localhost:5432/survey
DB_USERNAME=postgres
DB_PASSWORD=postgres
JWT_SECRET=your_jwt_secret_key_here_should_be_long_and_secure
JWT_EXPIRATION=3600
JWT_REFRESH_EXPIRATION=86400
ELASTICSEARCH_URIS=http://localhost:9200
```

### Using Docker Compose

The easiest way to run all required services:

1. Start the infrastructure services:

```bash
docker compose up -d
```

This will start:
- PostgreSQL database
- Elasticsearch

2. Build and run the application:

```bash
./mvnw clean package
java -jar target/Survey-0.0.1-SNAPSHOT.jar
```

Or use the Maven Spring Boot plugin:

```bash
./mvnw spring-boot:run
```

3. Run the frontend development server:

```bash
cd frontend
npm install
npm run dev
```

The frontend will be available at: http://localhost:5173

## API Endpoints

### Authentication

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/auth/login` | POST | Authenticate user and get JWT tokens |
| `/auth/refresh` | POST | Refresh expired JWT token |
| `/auth/health` | GET | Health check endpoint |

### Survey Management

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/surveys` | POST | Create a new survey |
| `/api/surveys/user` | GET | Get all surveys for the current user |
| `/api/surveys/all` | GET | Get all surveys (public) |
| `/api/surveys/{id}` | GET | Get a specific survey by ID |

### User Management

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/users` | GET | Get all users (paginated) |
| `/api/users` | POST | Register a new user |
| `/api/users/{id}` | GET | Get user by ID |
| `/api/users/username/{username}` | GET | Get user by username |

### Answer Management

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/answers` | POST | Submit answers for a survey |

### Admin Management

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/admin/search` | GET | Search across all entities |
| `/api/admin/search/surveys` | GET | Search surveys |
| `/api/admin/search/questions` | GET | Search questions |
| `/api/admin/search/questions/survey` | GET | Search questions by survey ID |
| `/api/admin/search/questions/type` | GET | Search questions by type |
| `/api/admin/search/choices` | GET | Search choices |
| `/api/admin/search/choices/question` | GET | Search choices by question ID |
| `/api/admin/search/answers/question` | GET | Search answers by question ID |
| `/api/admin/search/answers/user` | GET | Search answers by user ID |
| `/api/admin/search/answers/public` | GET | Search public answers |
| `/api/admin/search/answers/question-user` | GET | Search answers by question and user |
| `/api/admin/elasticsearch/sync` | POST | Synchronize data with Elasticsearch |
| `/api/admin/statistics` | GET | Get system statistics |
| `/api/admin/statistics/question-types` | GET | Get question type statistics |
| `/api/admin/statistics/user-participation` | GET | Get user participation statistics |

## API Documentation

After starting the application, Swagger UI is available at:
http://localhost:8080/swagger-ui/index.html

## Development

### Backend Development

Build the project:
```bash
./mvnw clean install
```

Run tests:
```bash
./mvnw test
```

### Frontend Development

```bash
cd frontend
npm install
npm run dev
```

## License

This project is licensed under the MIT License - see the LICENSE file for details.