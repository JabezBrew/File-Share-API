# File Share 📂

A robust file-sharing server built with Spring Boot that provides secure file upload and download capabilities.

## Features 🌟

- File upload and download via REST API endpoints
- File storage with collision handling using unique identifiers
- File type validation and restrictions
- Storage space management
- Duplicate file detection
- File metadata tracking in database

## Tech Stack 💻

- Java 17
- Spring Boot 3.2.0
- Spring Data JPA
- H2 Database
- MySQL Database
- Gradle
- JUnit for testing

## API Endpoints 🛣️

### Upload File
```http
POST /api/v1/upload
Content-Type: multipart/form-data
```

### Download File
```http
GET /api/v1/download/{fileId}
```

### Get Storage Info
```http
GET /api/v1/info
```

## Configuration ⚙️

Application properties (`application.properties`):
```properties
server.port=8888
uploads.dir=../uploads
spring.datasource.url=jdbc:mysql://localhost:3306/fileshare_db
spring.jpa.hibernate.ddl-auto=update
```

## Security Features 🔒

- File type validation (only allows txt, jpg, png)
- Storage space limits
- File size restrictions
- Protection against duplicate uploads

## Build and Run 🚀

```bash
# Build the project
./gradlew build

# Run the application
./gradlew bootRun
```

## Response Examples 📝

### Successful Upload
```http
HTTP/1.1 201 Created
Location: http://localhost:8888/api/v1/download/{fileId}
```

### Storage Info
```json
{
  "total_files": 3,
  "total_bytes": 194325
}
```

## Error Handling ⚠️

- 404: File not found
- 415: Unsupported media type
- 413: Payload too large
