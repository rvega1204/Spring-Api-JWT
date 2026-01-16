# Spring Boot REST API Starter

A production-ready REST API built with Spring Boot 3.4, featuring JWT authentication, MySQL database, and comprehensive unit testing.

## 🚀 Features

- **JWT Authentication** - Stateless authentication with access tokens
- **RESTful API** - CRUD operations for Products and Users
- **Security** - BCrypt password encryption, JWT filter chain
- **Database** - MySQL with JPA/Hibernate
- **Testing** - Unit tests with JUnit 5 and Mockito (no database dependency)
- **DTO Mapping** - MapStruct for entity-DTO conversions

## 🛠️ Technologies

| Technology      | Version | Purpose                        |
| --------------- | ------- | ------------------------------ |
| Java            | 17+     | Programming language           |
| Spring Boot     | 3.4.x   | Application framework          |
| Spring Security | 6.x     | Authentication & authorization |
| MySQL           | 8.0+    | Relational database            |
| JWT             | 0.11.5  | JSON Web Tokens                |
| MapStruct       | Latest  | Object mapping                 |
| JUnit 5         | 5.10+   | Unit testing framework         |
| Mockito         | 5.x     | Mocking framework              |
| Lombok          | Latest  | Boilerplate reduction          |
| Maven           | 3.8+    | Build tool                     |

## 📁 Project Structure

```
src/
├── main/
│   ├── java/com/rvg/store/
│   │   ├── config/
│   │   │   ├── SecurityConfig.java          # JWT security configuration
│   │   │   └── GlobalExceptionHandler.java  # Global error handling
│   │   ├── controllers/
│   │   │   ├── AuthController.java          # Login/Register endpoints
│   │   │   ├── ProductController.java       # Product CRUD
│   │   │   └── UserController.java          # User management
│   │   ├── dtos/
│   │   │   ├── AuthResponse.java            # Login/register response
│   │   │   ├── LoginRequest.java            # Login credentials
│   │   │   ├── RegisterUserRequest.java     # Registration data
│   │   │   ├── ProductDto.java              # Product data transfer
│   │   │   ├── UserDto.java                 # User data transfer
│   │   │   ├── UpdateUserRequest.java       # User update data
│   │   │   └── ChangePasswordRequest.java   # Password change data
│   │   ├── entities/
│   │   │   ├── User.java                    # User entity (JPA)
│   │   │   ├── Product.java                 # Product entity
│   │   │   └── Category.java                # Category entity
│   │   ├── mappers/
│   │   │   ├── UserMapper.java              # User DTO-Entity mapper
│   │   │   └── ProductMapper.java           # Product DTO-Entity mapper
│   │   ├── repositories/
│   │   │   ├── UserRepository.java          # User data access
│   │   │   ├── ProductRepository.java       # Product data access
│   │   │   └── CategoryRepository.java      # Category data access
│   │   ├── security/
│   │   │   ├── JwtService.java              # JWT token generation/validation
│   │   │   ├── JwtAuthenticationFilter.java # JWT request filter
│   │   │   └── CustomUserDetailsService.java # Load user for authentication
│   │   └── StoreApplication.java            # Main application class
│   └── resources/
│       └── application.properties           # Configuration
└── test/
    └── java/com/rvg/store/controllers/
        ├── AuthControllerTest.java          # Auth endpoint tests
        ├── ProductControllerTest.java       # Product endpoint tests
        └── UserControllerTest.java          # User endpoint tests
```

## 🔐 Security Architecture

### JWT Authentication Flow

1. **Login/Register** → Returns JWT token (24h expiration)
2. **Subsequent Requests** → Include token in `Authorization: Bearer {token}` header
3. **JWT Filter** → Validates token and authenticates user
4. **Access Control** → `/auth/**` public, all other endpoints require authentication

### Key Security Components

- **JwtService** - Generates and validates JWT tokens using HMAC SHA-256
- **JwtAuthenticationFilter** - Intercepts requests and validates tokens
- **SecurityConfig** - Configures stateless session management and authentication provider
- **BCryptPasswordEncoder** - Hashes passwords before storing

## 🧪 Testing Strategy

Following industry best practices, this project tests only code with business logic:

### ✅ What We Test

**Controllers** - All endpoints with mocked dependencies (3 test classes, 30+ tests)

- `AuthControllerTest` - Login, register, authentication flows
- `ProductControllerTest` - CRUD operations, filtering, validation
- `UserControllerTest` - User management, password changes, sorting

**Test Characteristics:**

- No database connection (100% mocked repositories)
- Fast execution (0.1-0.3 seconds per test)
- `@SpringBootTest` + `@AutoConfigureMockMvc(addFilters = false)`
- `@MockitoBean` for all dependencies

### ❌ What We Don't Test

Per testing best practices:

- DTOs (simple getters/setters have no logic)
- Entities (JPA-managed POJOs)
- MapStruct Mappers (auto-generated code, already tested by library)
- Basic CRUD operations without business logic

## 🚦 API Endpoints

### Authentication

| Method | Endpoint         | Description                    | Auth Required |
| ------ | ---------------- | ------------------------------ | ------------- |
| POST   | `/auth/register` | Create new user account        | ❌            |
| POST   | `/auth/login`    | Authenticate and get JWT token | ❌            |

**Example Login Request:**

```json
POST /auth/login
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response:**

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "email": "user@example.com",
  "name": "John Doe"
}
```

### Products

| Method | Endpoint                 | Description        | Auth Required |
| ------ | ------------------------ | ------------------ | ------------- |
| GET    | `/products`              | List all products  | ✅            |
| GET    | `/products?categoryId=1` | Filter by category | ✅            |
| GET    | `/products/{id}`         | Get product by ID  | ✅            |
| POST   | `/products`              | Create new product | ✅            |
| PUT    | `/products/{id}`         | Update product     | ✅            |
| DELETE | `/products/{id}`         | Delete product     | ✅            |

### Users

| Method | Endpoint                      | Description             | Auth Required |
| ------ | ----------------------------- | ----------------------- | ------------- |
| GET    | `/users`                      | List all users          | ✅            |
| GET    | `/users?sort=email`           | Sort users (name/email) | ✅            |
| GET    | `/users/{id}`                 | Get user by ID          | ✅            |
| POST   | `/users`                      | Create new user         | ✅            |
| PUT    | `/users/{id}`                 | Update user             | ✅            |
| DELETE | `/users/{id}`                 | Delete user             | ✅            |
| POST   | `/users/{id}/change-password` | Change user password    | ✅            |

## ⚙️ Configuration

### application.properties

```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/store_db
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# JWT Configuration
jwt.secret=your-secret-key-here-make-it-long-and-secure-at-least-256-bits
jwt.expiration=86400000
```

### Required Environment Variables

- `DB_USERNAME` - MySQL username
- `DB_PASSWORD` - MySQL password
- `JWT_SECRET` - Secret key for JWT signing (minimum 256 bits)

## 🏃 Running the Application

### Prerequisites

- Java 17 or higher
- MySQL 8.0+
- Maven 3.8+

### Steps

1. **Clone the repository**

```bash
git clone https://github.com/rvega1204/Spring-Api-JWT.git
```

2. **Configure database**

```bash
mysql -u root -p
CREATE DATABASE store_db;
```

Update `application.properties` with your database credentials

3. **Run the application**

```bash
./mvnw spring-boot:run
```

4. **Test with Postman/cURL**

```bash
# Register
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Test User","email":"test@test.com","password":"password123"}'

# Login and get token
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"password123"}'

# Use token to access protected endpoint
curl http://localhost:8080/products \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## 🧪 Running Tests

```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=ProductControllerTest

# Run with coverage report
./mvnw clean test jacoco:report
```

**Test Results:**

- AuthControllerTest: 3 tests
- ProductControllerTest: 11 tests
- UserControllerTest: 16 tests
- Total: 30+ tests, all passing ✅

## 📚 Key Design Patterns

- **DTO Pattern** - Separate internal entities from API contracts
- **Repository Pattern** - Data access abstraction with Spring Data JPA
- **Dependency Injection** - Constructor injection with Lombok's `@RequiredArgsConstructor`
- **Filter Pattern** - JWT authentication filter in security chain
- **Builder Pattern** - Entity construction with Lombok's `@Builder`

## 🤝 Contributing

We welcome contributions! Here are areas where you can help:

### 🔧 Suggested Improvements

#### High Priority

- [ ] **Refresh Tokens** - Implement refresh token mechanism for seamless token renewal
- [ ] **Role-Based Access Control (RBAC)** - Add USER/ADMIN roles with different permissions
- [ ] **Pagination** - Add pagination support to GET `/products` and `/users` endpoints
- [ ] **API Documentation** - Integrate Swagger/OpenAPI for interactive API docs
- [ ] **Rate Limiting** - Prevent brute force attacks on login endpoint

#### Medium Priority

- [ ] **Integration Tests** - Add integration tests with Testcontainers for MySQL
- [ ] **Email Verification** - Send verification email on registration
- [ ] **Password Reset** - Implement forgot password functionality
- [ ] **Audit Logging** - Track who created/modified entities and when
- [ ] **Global Exception Handling** - Improve error responses with custom exception classes

#### Nice to Have

- [ ] **Search & Filtering** - Advanced product search with multiple criteria
- [ ] **Image Upload** - Support product images with cloud storage (S3/Azure)
- [ ] **Soft Delete** - Mark entities as deleted instead of removing from database
- [ ] **Caching** - Add Redis cache for frequently accessed data
- [ ] **Docker Compose** - Container setup for easy local development
- [ ] **CI/CD Pipeline** - GitHub Actions for automated testing and deployment

### How to Contribute

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Write tests for your changes
4. Ensure all tests pass (`./mvnw test`)
5. Commit your changes (`git commit -m 'Add amazing feature'`)
6. Push to the branch (`git push origin feature/amazing-feature`)
7. Open a Pull Request

### Code Style Guidelines

- Follow existing code conventions
- Write tests for new features (controllers only, following our testing strategy)
- Use meaningful commit messages
- Update README if adding new features
- Ensure `./mvnw test` passes before submitting PR

## 📄 License

This project is licensed under the MIT License: Copyright (c) 2026 Ricardo Vega

**Educational Use:** This project is ideal for learning Spring Boot, REST APIs, JWT authentication, and testing best practices. Feel free to use it for tutorials, courses, or as a starting point for your own projects.

## 🙏 Acknowledgments

- Spring Boot documentation and community
- [JWT.io](https://jwt.io) for JWT resources
- Testing best practices from the Spring community
- MapStruct for simplified object mapping

## 📧 Contact & Support

- Create an issue for bug reports or feature requests
- Check existing issues before creating new ones
- Star ⭐ this repo if you find it helpful!

---

Built with ❤️ using Spring Boot
