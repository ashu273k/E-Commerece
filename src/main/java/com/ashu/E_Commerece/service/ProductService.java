package com.ashu.E_Commerece.service;

import com.ashu.E_Commerece.dto.common.PagedResponse;
import com.ashu.E_Commerece.dto.product.ProductRequest;
import com.ashu.E_Commerece.dto.product.ProductResponse;
import com.ashu.E_Commerece.dto.product.ProductSearchCriteria;
import com.ashu.E_Commerece.exception.BadRequestException;
import com.ashu.E_Commerece.exception.ResourceNotFoundException;
import com.ashu.E_Commerece.model.Category;
import com.ashu.E_Commerece.model.Product;
import com.ashu.E_Commerece.repository.CategoryRepository;
import com.ashu.E_Commerece.repository.ProductRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Product catalog management with caching and dynamic search.
 * Uses JPA Specifications for flexible filtering without N+1 queries.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    /**
     * Get all products with pagination.
     */
    @Transactional(readOnly = true)
    public PagedResponse<ProductResponse> getAllProducts(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> products = productRepository.findByActiveTrue(pageable);

        return mapToPagedResponse(products);
    }

    /**
     * Dynamic search using JPA Specification API. Builds query predicates
     * at runtime based on provided criteria for flexible filtering.
     */
    @Transactional(readOnly = true)
    public PagedResponse<ProductResponse> searchProducts(ProductSearchCriteria criteria,
            int page, int size,
            String sortBy, String sortDir) {
        Specification<Product> spec = buildSpecification(criteria);

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> products = productRepository.findAll(spec, pageable);

        return mapToPagedResponse(products);
    }

    // Cached to reduce DB load for product detail pages
    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "#id")
    public ProductResponse getProductById(Long id) {
        Product product = findProductById(id);
        return mapToResponse(product);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ProductResponse> getProductsByCategory(Long categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productRepository.findByCategoryId(categoryId, pageable);
        return mapToPagedResponse(products);
    }

    // Cached home page featured products - invalidated on any product change
    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "'featured'")
    public List<ProductResponse> getFeaturedProducts() {
        Pageable pageable = PageRequest.of(0, 10);
        return productRepository.findByFeaturedTrueAndActiveTrue(pageable)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getTopRatedProducts() {
        return productRepository.findTop10ByActiveTrueOrderByAverageRatingDesc()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getNewestProducts() {
        return productRepository.findTop10ByActiveTrueOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // CacheEvict clears all entries since new products affect list queries
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public ProductResponse createProduct(ProductRequest request) {
        if (request.getSku() != null && productRepository.existsBySku(request.getSku())) {
            throw new BadRequestException("Product with this SKU already exists");
        }

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .discountPrice(request.getDiscountPrice())
                .stockQuantity(request.getStockQuantity())
                .sku(request.getSku())
                .brand(request.getBrand())
                .imageUrls(request.getImageUrls() != null ? request.getImageUrls() : new ArrayList<>())
                .active(request.getActive() != null ? request.getActive() : true)
                .featured(request.getFeatured() != null ? request.getFeatured() : false)
                .build();

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));
            product.setCategory(category);
        }

        product = productRepository.save(product);
        log.info("Product created: {}", product.getName());

        return mapToResponse(product);
    }

    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = findProductById(id);

        // SKU uniqueness check excludes current product
        if (request.getSku() != null && !request.getSku().equals(product.getSku())
                && productRepository.existsBySku(request.getSku())) {
            throw new BadRequestException("Product with this SKU already exists");
        }

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setDiscountPrice(request.getDiscountPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setSku(request.getSku());
        product.setBrand(request.getBrand());

        if (request.getImageUrls() != null) {
            product.setImageUrls(request.getImageUrls());
        }
        if (request.getActive() != null) {
            product.setActive(request.getActive());
        }
        if (request.getFeatured() != null) {
            product.setFeatured(request.getFeatured());
        }

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));
            product.setCategory(category);
        }

        product = productRepository.save(product);
        log.info("Product updated: {}", product.getName());

        return mapToResponse(product);
    }

    // Soft delete preserves order history referential integrity
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public void deleteProduct(Long id) {
        Product product = findProductById(id);
        product.setActive(false);
        productRepository.save(product);
        log.info("Product deactivated: {}", product.getName());
    }

    @Transactional
    public void updateStock(Long productId, int quantity) {
        Product product = findProductById(productId);
        int newStock = product.getStockQuantity() + quantity;
        if (newStock < 0) {
            throw new BadRequestException("Insufficient stock");
        }
        product.setStockQuantity(newStock);
        productRepository.save(product);
    }

    // Called by ReviewService when reviews change - updates denormalized rating
    @Transactional
    public void updateProductRating(Long productId, BigDecimal averageRating, int reviewCount) {
        Product product = findProductById(productId);
        product.setAverageRating(averageRating);
        product.setReviewCount(reviewCount);
        productRepository.save(product);
    }

    private Product findProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
    }

    private Specification<Product> buildSpecification(ProductSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Base filter: only active products shown to customers
            predicates.add(cb.isTrue(root.get("active")));

            if (criteria.getKeyword() != null && !criteria.getKeyword().isEmpty()) {
                String keyword = "%" + criteria.getKeyword().toLowerCase() + "%";
                Predicate namePredicate = cb.like(cb.lower(root.get("name")), keyword);
                Predicate descPredicate = cb.like(cb.lower(root.get("description")), keyword);
                predicates.add(cb.or(namePredicate, descPredicate));
            }

            if (criteria.getCategoryId() != null) {
                predicates.add(cb.equal(root.get("category").get("id"), criteria.getCategoryId()));
            }

            if (criteria.getMinPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), criteria.getMinPrice()));
            }

            if (criteria.getMaxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), criteria.getMaxPrice()));
            }

            if (criteria.getBrand() != null && !criteria.getBrand().isEmpty()) {
                predicates.add(cb.equal(cb.lower(root.get("brand")), criteria.getBrand().toLowerCase()));
            }

            if (criteria.getInStock() != null && criteria.getInStock()) {
                predicates.add(cb.greaterThan(root.get("stockQuantity"), 0));
            }

            if (criteria.getFeatured() != null && criteria.getFeatured()) {
                predicates.add(cb.isTrue(root.get("featured")));
            }

            if (criteria.getMinRating() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("averageRating"), criteria.getMinRating()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .discountPrice(product.getDiscountPrice())
                .effectivePrice(product.getEffectivePrice())
                .stockQuantity(product.getStockQuantity())
                .sku(product.getSku())
                .brand(product.getBrand())
                .imageUrls(product.getImageUrls())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .averageRating(product.getAverageRating())
                .reviewCount(product.getReviewCount())
                .active(product.isActive())
                .featured(product.isFeatured())
                .inStock(product.isInStock())
                .createdAt(product.getCreatedAt())
                .build();
    }

    private PagedResponse<ProductResponse> mapToPagedResponse(Page<Product> page) {
        List<ProductResponse> content = page.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PagedResponse.<ProductResponse>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
}
