package com.ashu.E_Commerece;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * E-Commerce Application - Production-grade REST API Backend
 * 
 * Features:
 * - JWT Authentication & Authorization
 * - Role-based Access Control (Admin/User)
 * - Product Management with Categories
 * - Shopping Cart & Orders
 * - Product Reviews & Ratings
 * - File Upload
 * - Mock Email & Payment Services
 * - API Rate Limiting
 * - Caching with Caffeine
 * - Swagger/OpenAPI Documentation
 * 
 * Default Credentials:
 * - Admin: admin@ecommerce.com / admin123
 * - User: user@example.com / user123
 * 
 * Access Swagger UI at: http://localhost:8080/swagger-ui.html
 * Access H2 Console at: http://localhost:8080/h2-console
 */
@SpringBootApplication
public class ECommereceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ECommereceApplication.class, args);
	}

}
