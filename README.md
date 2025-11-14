# Post Batch Manager - Code Test Solution

A Spring Boot application that fetches posts from JSONPlaceholder API using CompletableFuture and stores them in an SQLite database with pagination support.

## Quick Start

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- Internet connection

### Build and Run

```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## API Endpoints

### 1. Batch Insert Posts
```bash
curl -X POST http://localhost:8080/api/posts/batch_insert \
  -H "Content-Type: application/json" \
  -d '{"postNumber": 24}'
```

### 2. Fetch Records (Paginated)
```bash
# Get first page (default: 10 records)
curl http://localhost:8080/api/posts/fetch_record

# Get specific page with custom size
curl http://localhost:8080/api/posts/fetch_record?page=1&size=20
```

### 3. Health Check
```bash
curl http://localhost:8080/api/posts/health
```

## Features

- ✅ Batch insert using CompletableFuture for concurrent API calls
- ✅ WebClient for modern HTTP client
- ✅ SQLite database for persistence
- ✅ Paginated record retrieval
- ✅ Comprehensive error handling and validation
- ✅ Detailed logging

## Technologies

- Spring Boot 3.1.5
- Spring WebFlux (WebClient)
- Spring Data JPA
- SQLite JDBC 3.47.2.0
- Hibernate
- Lombok
- Java 17

## Project Structure

```
post-batch-manager/
├── src/main/java/com/aspacelife/postbatch/
│   ├── PostBatchApplication.java
│   ├── controller/PostController.java
│   ├── service/
│   │   ├── PostService.java
│   │   └── impl/PostServiceImpl.java
│   ├── repository/PostRepository.java
│   ├── model/
│   │   ├── Post.java
│   │   └── BatchInsertRequest.java
│   ├── dto/PageResponse.java
│   ├── config/DatabaseConfig.java
│   └── exception/
│       ├── GlobalExceptionHandler.java
│       └── ResourceNotFoundException.java
├── src/main/resources/
│   └── application.properties
└── pom.xml
```

## Author
Oladeji Oluwafemi

## Submission
Aspacelife Technology Limited - Code Test
