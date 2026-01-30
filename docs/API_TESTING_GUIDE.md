# E-Commerce API - Postman Testing Guide

## 🚀 Getting Started

### 1. Start the Application

```bash
./mvnw spring-boot:run
```

Application runs at: `http://localhost:8080`

### 2. Access Points

| Service | URL |
|---------|-----|
| API Base | http://localhost:8080/api |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| H2 Console | http://localhost:8080/h2-console |

---

## 📦 Pre-loaded Mock Data

### Users (Auto-created on startup)

| Role | Email | Password |
|------|-------|----------|
| **Admin** | admin@ecommerce.com | admin123 |
| **User** | user@example.com | user123 |

### Categories

| ID | Name | Parent |
|----|------|--------|
| 1 | Electronics | - |
| 2 | Clothing | - |
| 3 | Books | - |
| 4 | Home & Garden | - |
| 5 | Smartphones | Electronics |
| 6 | Laptops | Electronics |

### Products (13 products)

| Name | Price | Discount | Category | Featured |
|------|-------|----------|----------|----------|
| iPhone 15 Pro | $999.99 | $949.99 | Smartphones | ✅ |
| Samsung Galaxy S24 | $899.99 | - | Smartphones | ✅ |
| Google Pixel 8 | $699.99 | $649.99 | Smartphones | |
| MacBook Pro 16" | $2499.99 | - | Laptops | ✅ |
| Dell XPS 15 | $1599.99 | $1449.99 | Laptops | |
| ThinkPad X1 Carbon | $1799.99 | - | Laptops | |
| Classic T-Shirt | $29.99 | $24.99 | Clothing | |
| Denim Jeans | $79.99 | - | Clothing | ✅ |
| Running Shoes | $129.99 | $99.99 | Clothing | ✅ |
| Clean Code (Book) | $44.99 | $39.99 | Books | ✅ |
| The Pragmatic Programmer | $54.99 | - | Books | |
| Smart LED Bulb | $24.99 | - | Home & Garden | |
| Robot Vacuum | $349.99 | $299.99 | Home & Garden | ✅ |

---

## 🔐 Step 1: Authentication

### 1.1 Login (Get JWT Token)

**Request:**
```
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
    "email": "admin@ecommerce.com",
    "password": "admin123"
}
```

**Response:**
```json
{
    "success": true,
    "message": "Login successful",
    "data": {
        "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
        "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
        "tokenType": "Bearer",
        "expiresIn": 86400,
        "user": {
            "id": 1,
            "email": "admin@ecommerce.com",
            "firstName": "Admin",
            "lastName": "User",
            "role": "ADMIN"
        }
    }
}
```

**⚠️ IMPORTANT:** Copy the `accessToken` value. Use it in all subsequent requests.

### 1.2 Register New User

**Request:**
```
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
    "email": "newuser@example.com",
    "password": "password123",
    "firstName": "John",
    "lastName": "Smith",
    "phone": "+1234567890"
}
```

---

## 🛍️ Step 2: Browse Products (Public - No Auth Required)

### 2.1 Get All Products

```
GET http://localhost:8080/api/products
```

### 2.2 Get Products with Pagination

```
GET http://localhost:8080/api/products?page=0&size=5&sortBy=price&sortDir=asc
```

### 2.3 Search Products

```
GET http://localhost:8080/api/products/search?keyword=iphone&minPrice=500&maxPrice=1500
```

### 2.4 Get Featured Products

```
GET http://localhost:8080/api/products/featured
```

### 2.5 Get Single Product

```
GET http://localhost:8080/api/products/1
```

### 2.6 Get Products by Category

```
GET http://localhost:8080/api/products/category/5
```

---

## 📁 Step 3: Browse Categories (Public)

### 3.1 Get All Categories

```
GET http://localhost:8080/api/categories
```

### 3.2 Get Category Tree (with subcategories)

```
GET http://localhost:8080/api/categories/root
```

---

## 🛒 Step 4: Shopping Cart (Auth Required)

**Headers for all cart requests:**
```
Authorization: Bearer <your-access-token>
Content-Type: application/json
```

### 4.1 Get Cart

```
GET http://localhost:8080/api/cart
Authorization: Bearer <token>
```

### 4.2 Add Item to Cart

```
POST http://localhost:8080/api/cart/items
Authorization: Bearer <token>
Content-Type: application/json

{
    "productId": 1,
    "quantity": 2
}
```

### 4.3 Update Cart Item Quantity

```
PUT http://localhost:8080/api/cart/items/1?quantity=3
Authorization: Bearer <token>
```

### 4.4 Remove Item from Cart

```
DELETE http://localhost:8080/api/cart/items/1
Authorization: Bearer <token>
```

### 4.5 Clear Cart

```
DELETE http://localhost:8080/api/cart
Authorization: Bearer <token>
```

---

## 📦 Step 5: Place Order (Auth Required)

### 5.1 Create Order

```
POST http://localhost:8080/api/orders
Authorization: Bearer <token>
Content-Type: application/json

{
    "shippingAddress": {
        "street": "123 Main Street",
        "city": "New York",
        "state": "NY",
        "zipCode": "10001",
        "country": "USA"
    },
    "billingAddress": {
        "street": "123 Main Street",
        "city": "New York",
        "state": "NY",
        "zipCode": "10001",
        "country": "USA"
    },
    "paymentMethod": "CREDIT_CARD",
    "notes": "Please deliver between 9 AM - 5 PM"
}
```

### 5.2 Get My Orders

```
GET http://localhost:8080/api/orders
Authorization: Bearer <token>
```

### 5.3 Get Order Details

```
GET http://localhost:8080/api/orders/1
Authorization: Bearer <token>
```

### 5.4 Cancel Order

```
POST http://localhost:8080/api/orders/1/cancel
Authorization: Bearer <token>
```

---

## 💳 Step 6: Payment (Auth Required)

### 6.1 Process Payment (Mock)

```
POST http://localhost:8080/api/payments/process
Authorization: Bearer <token>
Content-Type: application/json

{
    "orderId": 1,
    "amount": 1899.98,
    "paymentMethod": "CREDIT_CARD",
    "cardNumber": "4111111111111111",
    "expiryMonth": "12",
    "expiryYear": "2028",
    "cvv": "123"
}
```

---

## ⭐ Step 7: Reviews (Auth Required for Create/Update)

### 7.1 Get Product Reviews (Public)

```
GET http://localhost:8080/api/products/1/reviews
```

### 7.2 Add Review

```
POST http://localhost:8080/api/products/1/reviews
Authorization: Bearer <token>
Content-Type: application/json

{
    "rating": 5,
    "title": "Excellent product!",
    "comment": "Best smartphone I've ever used. Camera quality is amazing!"
}
```

---

## 👤 Step 8: User Profile (Auth Required)

### 8.1 Get Profile

```
GET http://localhost:8080/api/users/profile
Authorization: Bearer <token>
```

### 8.2 Update Profile

```
PUT http://localhost:8080/api/users/profile
Authorization: Bearer <token>
Content-Type: application/json

{
    "firstName": "John",
    "lastName": "Updated",
    "phone": "+9876543210",
    "address": {
        "street": "456 New Street",
        "city": "Los Angeles",
        "state": "CA",
        "zipCode": "90001",
        "country": "USA"
    }
}
```

### 8.3 Change Password

```
PUT http://localhost:8080/api/users/password
Authorization: Bearer <token>
Content-Type: application/json

{
    "currentPassword": "admin123",
    "newPassword": "newpassword123",
    "confirmPassword": "newpassword123"
}
```

---

## 🔧 Step 9: Admin Operations (Admin Role Required)

Login as admin first: `admin@ecommerce.com` / `admin123`

### 9.1 Create Product

```
POST http://localhost:8080/api/products
Authorization: Bearer <admin-token>
Content-Type: application/json

{
    "name": "New Product",
    "description": "Product description here",
    "price": 199.99,
    "discountPrice": 179.99,
    "stockQuantity": 100,
    "sku": "NEW-PRD-001",
    "brand": "MyBrand",
    "categoryId": 1,
    "featured": true,
    "imageUrls": ["https://example.com/image1.jpg"]
}
```

### 9.2 Update Product

```
PUT http://localhost:8080/api/products/1
Authorization: Bearer <admin-token>
Content-Type: application/json

{
    "name": "iPhone 15 Pro Max",
    "price": 1099.99,
    "stockQuantity": 100
}
```

### 9.3 Delete Product

```
DELETE http://localhost:8080/api/products/1
Authorization: Bearer <admin-token>
```

### 9.4 Update Order Status

```
PUT http://localhost:8080/api/orders/1/status
Authorization: Bearer <admin-token>
Content-Type: application/json

{
    "status": "SHIPPED",
    "notes": "Package shipped via FedEx"
}
```

Order statuses: `PENDING`, `CONFIRMED`, `SHIPPED`, `DELIVERED`, `CANCELLED`, `REFUNDED`

### 9.5 Get All Orders (Admin)

```
GET http://localhost:8080/api/orders/all?status=PENDING
Authorization: Bearer <admin-token>
```

---

## 📊 Step 10: Analytics (Admin Only)

### 10.1 Sales Analytics

```
GET http://localhost:8080/api/analytics/sales
Authorization: Bearer <admin-token>
```

### 10.2 Product Analytics

```
GET http://localhost:8080/api/analytics/products
Authorization: Bearer <admin-token>
```

### 10.3 User Analytics

```
GET http://localhost:8080/api/analytics/users
Authorization: Bearer <admin-token>
```

---

## 📁 Step 11: File Upload

### 11.1 Upload File

```
POST http://localhost:8080/api/files/upload
Authorization: Bearer <token>
Content-Type: multipart/form-data

file: [select file]
```

### 11.2 Download File

```
GET http://localhost:8080/api/files/{filename}
```

---

## 🧪 Complete Testing Flow

1. **Login** → Get token
2. **Browse Products** → View catalog
3. **Add to Cart** → Add items
4. **Create Order** → Checkout
5. **Process Payment** → Pay
6. **Check Order Status** → Track
7. **Add Review** → Rate product

---

## 📋 Postman Setup

1. Create new Collection: "E-Commerce API"
2. Add variable: `baseUrl` = `http://localhost:8080`
3. Add variable: `token` = (your JWT token)
4. For authenticated requests, add Header:
   - Key: `Authorization`
   - Value: `Bearer {{token}}`

---

*Document created: 2026-01-30*
