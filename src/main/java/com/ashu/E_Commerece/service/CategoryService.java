package com.ashu.E_Commerece.service;

import com.ashu.E_Commerece.dto.category.CategoryRequest;
import com.ashu.E_Commerece.dto.category.CategoryResponse;
import com.ashu.E_Commerece.exception.BadRequestException;
import com.ashu.E_Commerece.exception.ResourceNotFoundException;
import com.ashu.E_Commerece.model.Category;
import com.ashu.E_Commerece.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for category operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * Get all categories.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "categories", key = "'all'")
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get root categories (no parent).
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "categories", key = "'root'")
    public List<CategoryResponse> getRootCategories() {
        return categoryRepository.findRootCategories().stream()
                .map(this::mapToResponseWithSubcategories)
                .collect(Collectors.toList());
    }

    /**
     * Get category by ID.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "categories", key = "#id")
    public CategoryResponse getCategoryById(Long id) {
        Category category = findCategoryById(id);
        return mapToResponseWithSubcategories(category);
    }

    /**
     * Get subcategories of a category.
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getSubcategories(Long parentId) {
        return categoryRepository.findByParentId(parentId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Create a new category.
     */
    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new BadRequestException("Category with this name already exists");
        }

        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .build();

        if (request.getParentId() != null) {
            Category parent = findCategoryById(request.getParentId());
            category.setParent(parent);
        }

        category = categoryRepository.save(category);
        log.info("Category created: {}", category.getName());

        return mapToResponse(category);
    }

    /**
     * Update a category.
     */
    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = findCategoryById(id);

        // Check if new name conflicts with existing category
        if (!category.getName().equals(request.getName()) && 
            categoryRepository.existsByName(request.getName())) {
            throw new BadRequestException("Category with this name already exists");
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setImageUrl(request.getImageUrl());

        if (request.getParentId() != null) {
            if (request.getParentId().equals(id)) {
                throw new BadRequestException("Category cannot be its own parent");
            }
            Category parent = findCategoryById(request.getParentId());
            category.setParent(parent);
        } else {
            category.setParent(null);
        }

        category = categoryRepository.save(category);
        log.info("Category updated: {}", category.getName());

        return mapToResponse(category);
    }

    /**
     * Delete a category.
     */
    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public void deleteCategory(Long id) {
        Category category = findCategoryById(id);
        
        if (!category.getProducts().isEmpty()) {
            throw new BadRequestException("Cannot delete category with associated products");
        }

        categoryRepository.delete(category);
        log.info("Category deleted: {}", category.getName());
    }

    private Category findCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
    }

    private CategoryResponse mapToResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .imageUrl(category.getImageUrl())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .parentName(category.getParent() != null ? category.getParent().getName() : null)
                .productCount(category.getProducts() != null ? category.getProducts().size() : 0)
                .build();
    }

    private CategoryResponse mapToResponseWithSubcategories(Category category) {
        List<CategoryResponse> subcategories = category.getSubcategories().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .imageUrl(category.getImageUrl())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .parentName(category.getParent() != null ? category.getParent().getName() : null)
                .subcategories(subcategories)
                .productCount(category.getProducts() != null ? category.getProducts().size() : 0)
                .build();
    }
}
