# E-Commerce Backend API

A production-grade E-Commerce REST API built with Spring Boot, featuring JWT authentication, role-based access control, and comprehensive e-commerce functionality.

## 🚀 Quick Start

### Prerequisites
- Java 21
- Maven 3.8+

### Run the Application
```bash
mvn spring-boot:run
```

The application will start at `http://localhost:8080`

### Access Points
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/api-docs
- **H2 Console**: http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:mem:ecommercedb`)

## 🔐 Default Credentials

| Role | Email | Password |
|------|-------|----------|
| Admin | admin@ecommerce.com | admin123 |
| User | user@example.com | user123 |

## 📦 Features

### Core Features
- ✅ User Registration & Authentication (JWT)
- ✅ Role-based Access Control (Admin/User)
- ✅ Product Management with Categories
- ✅ Shopping Cart
- ✅ Order Management
- ✅ Product Reviews & Ratings

### Advanced Features
- ✅ Pagination & Sorting
- ✅ Advanced Product Search & Filtering
- ✅ Caching (Caffeine)
- ✅ API Rate Limiting (Bucket4j)
- ✅ File Upload
- ✅ Email Notifications (Mock)
- ✅ Payment Processing (Mock)
- ✅ Analytics APIs
- ✅ Global Exception Handling
- ✅ Input Validation
- ✅ Swagger Documentation

## 🏗️ Architecture

```
src/main/java/com/ashu/E_Commerece/
├── config/          # Configuration classes
├── controller/      # REST controllers
├── dto/             # Data Transfer Objects
├── exception/       # Exception handling
├── model/           # JPA entities
├── repository/      # Data repositories
├── security/        # JWT & Security
├── service/         # Business logic
└── util/            # Utilities
```

## 📡 API Endpoints

### Authentication
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/auth/register | Register new user |
| POST | /api/auth/login | Login & get JWT |
| POST | /api/auth/refresh | Refresh token |

### Products (Public GET, Admin CRUD)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/products | List products (paginated) |
| GET | /api/products/search | Search with filters |
| GET | /api/products/{id} | Get product details |
| GET | /api/products/featured | Featured products |
| POST | /api/products | Create product (Admin) |
| PUT | /api/products/{id} | Update product (Admin) |
| DELETE | /api/products/{id} | Delete product (Admin) |

### Categories
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/categories | List all categories |
| GET | /api/categories/root | Root categories with tree |
| POST | /api/categories | Create (Admin) |

### Cart (Authenticated)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/cart | Get cart |
| POST | /api/cart/items | Add item |
| PUT | /api/cart/items/{id} | Update quantity |
| DELETE | /api/cart/items/{id} | Remove item |

### Orders (Authenticated)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/orders | Create order |
| GET | /api/orders | User's orders |
| GET | /api/orders/{id} | Order details |
| POST | /api/orders/{id}/cancel | Cancel order |

### Reviews
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/products/{id}/reviews | Product reviews |
| POST | /api/products/{id}/reviews | Add review |

### Analytics (Admin)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/analytics/sales | Sales analytics |
| GET | /api/analytics/products | Product analytics |
| GET | /api/analytics/users | User analytics |

### Files
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/files/upload | Upload file |
| GET | /api/files/{filename} | Download file |

### Payments (Mock)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/payments/process | Process payment |
| GET | /api/payments/{id} | Payment status |

## 🔧 Technology Stack

| Component | Technology |
|-----------|------------|
| Framework | Spring Boot 3.2 |
| Database | H2 (In-Memory) |
| Security | Spring Security + JWT |
| Caching | Caffeine |
| Rate Limiting | Bucket4j |
| Documentation | SpringDoc OpenAPI |
| Validation | Jakarta Validation |
| Build Tool | Maven |

## 📝 Mock Services

This project uses mock implementations for:
- **Email Service**: Logs emails to console
- **Payment Service**: Simulates payment processing

These can be replaced with real implementations for production.

## 🧪 Testing with Swagger

1. Open http://localhost:8080/swagger-ui.html
2. Register or use default credentials
3. Login to get JWT token
4. Click "Authorize" and enter: `Bearer <your-token>`
5. Test any endpoint

## 📊 Sample Data

On startup, the application creates:
- 2 Users (1 Admin, 1 Regular)
- 6 Categories (with hierarchy)
- 13 Products (with various categories)
