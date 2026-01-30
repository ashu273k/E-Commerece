package com.ashu.E_Commerece.config;

import com.ashu.E_Commerece.model.*;
import com.ashu.E_Commerece.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            initializeData();
        }
    }

    private void initializeData() {
        log.info("Initializing mock data...");

        // Create Admin User
        User admin = User.builder()
                .email("admin@ecommerce.com")
                .password(passwordEncoder.encode("admin123"))
                .firstName("Admin")
                .lastName("User")
                .role(Role.ADMIN)
                .phone("+1234567890")
                .address(Address.builder()
                        .street("123 Admin Street").city("New York")
                        .state("NY").zipCode("10001").country("USA").build())
                .build();
        userRepository.save(admin);

        // Create Regular User
        User user = User.builder()
                .email("user@example.com")
                .password(passwordEncoder.encode("user123"))
                .firstName("John")
                .lastName("Doe")
                .role(Role.USER)
                .phone("+1987654321")
                .build();
        userRepository.save(user);

        log.info("Created users: admin@ecommerce.com (admin123), user@example.com (user123)");

        // Create Categories
        Category electronics = createCategory("Electronics", "Electronic devices and gadgets", null);
        Category clothing = createCategory("Clothing", "Fashion and apparel", null);
        Category books = createCategory("Books", "Books and literature", null);
        Category home = createCategory("Home & Garden", "Home decor and garden supplies", null);

        Category phones = createCategory("Smartphones", "Mobile phones", electronics);
        Category laptops = createCategory("Laptops", "Notebook computers", electronics);

        log.info("Created {} categories", categoryRepository.count());

        // Create Products
        createProduct("iPhone 15 Pro", "Latest Apple smartphone with A17 chip",
                new BigDecimal("999.99"), new BigDecimal("949.99"), 50, "APL-IPH15P", "Apple", phones, true);
        createProduct("Samsung Galaxy S24", "Flagship Android smartphone",
                new BigDecimal("899.99"), null, 75, "SAM-S24", "Samsung", phones, true);
        createProduct("Google Pixel 8", "Pure Android experience",
                new BigDecimal("699.99"), new BigDecimal("649.99"), 30, "GGL-PX8", "Google", phones, false);

        createProduct("MacBook Pro 16\"", "M3 Pro chip, 18GB RAM",
                new BigDecimal("2499.99"), null, 25, "APL-MBP16", "Apple", laptops, true);
        createProduct("Dell XPS 15", "Intel Core i7, 16GB RAM",
                new BigDecimal("1599.99"), new BigDecimal("1449.99"), 40, "DEL-XPS15", "Dell", laptops, false);
        createProduct("ThinkPad X1 Carbon", "Business ultrabook",
                new BigDecimal("1799.99"), null, 20, "LEN-X1C", "Lenovo", laptops, false);

        createProduct("Classic T-Shirt", "100% cotton, comfortable fit",
                new BigDecimal("29.99"), new BigDecimal("24.99"), 200, "CLT-TSH001", "Nike", clothing, false);
        createProduct("Denim Jeans", "Slim fit, dark wash",
                new BigDecimal("79.99"), null, 150, "CLT-JNS001", "Levi's", clothing, true);
        createProduct("Running Shoes", "Lightweight, breathable",
                new BigDecimal("129.99"), new BigDecimal("99.99"), 80, "CLT-SHO001", "Adidas", clothing, true);

        createProduct("Clean Code", "A Handbook of Agile Software Craftsmanship",
                new BigDecimal("44.99"), new BigDecimal("39.99"), 100, "BK-CC001", "Prentice Hall", books, true);
        createProduct("The Pragmatic Programmer", "Your Journey to Mastery",
                new BigDecimal("54.99"), null, 60, "BK-PP001", "Addison-Wesley", books, false);

        createProduct("Smart LED Bulb", "WiFi enabled, color changing",
                new BigDecimal("24.99"), null, 300, "HOM-LED01", "Philips", home, false);
        createProduct("Robot Vacuum", "Self-charging, smart mapping",
                new BigDecimal("349.99"), new BigDecimal("299.99"), 45, "HOM-RVC01", "iRobot", home, true);

        log.info("Created {} products", productRepository.count());
        log.info("Mock data initialization complete!");
    }

    private Category createCategory(String name, String description, Category parent) {
        Category category = Category.builder()
                .name(name).description(description).parent(parent).build();
        return categoryRepository.save(category);
    }

    private void createProduct(String name, String description, BigDecimal price, BigDecimal discountPrice,
                               int stock, String sku, String brand, Category category, boolean featured) {
        Product product = Product.builder()
                .name(name).description(description).price(price).discountPrice(discountPrice)
                .stockQuantity(stock).sku(sku).brand(brand).category(category)
                .featured(featured).active(true)
                .imageUrls(List.of("https://via.placeholder.com/400x400?text=" + name.replace(" ", "+")))
                .averageRating(BigDecimal.ZERO).reviewCount(0).build();
        productRepository.save(product);
    }
}
