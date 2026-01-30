package com.ashu.E_Commerece.service;

import com.ashu.E_Commerece.dto.common.PagedResponse;
import com.ashu.E_Commerece.dto.review.ReviewRequest;
import com.ashu.E_Commerece.dto.review.ReviewResponse;
import com.ashu.E_Commerece.exception.BadRequestException;
import com.ashu.E_Commerece.exception.ResourceNotFoundException;
import com.ashu.E_Commerece.model.Product;
import com.ashu.E_Commerece.model.Review;
import com.ashu.E_Commerece.model.User;
import com.ashu.E_Commerece.repository.ProductRepository;
import com.ashu.E_Commerece.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for review operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final UserService userService;

    /**
     * Get reviews for a product.
     */
    @Transactional(readOnly = true)
    public PagedResponse<ReviewResponse> getProductReviews(Long productId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Review> reviews = reviewRepository.findByProductId(productId, pageable);
        return mapToPagedResponse(reviews);
    }

    /**
     * Get user's reviews.
     */
    @Transactional(readOnly = true)
    public PagedResponse<ReviewResponse> getUserReviews(int page, int size) {
        User user = userService.getCurrentUser();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Review> reviews = reviewRepository.findByUserId(user.getId(), pageable);
        return mapToPagedResponse(reviews);
    }

    /**
     * Create a review for a product.
     */
    @Transactional
    public ReviewResponse createReview(Long productId, ReviewRequest request) {
        User user = userService.getCurrentUser();
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        // Check if user already reviewed this product
        if (reviewRepository.existsByUserIdAndProductId(user.getId(), productId)) {
            throw new BadRequestException("You have already reviewed this product");
        }

        Review review = Review.builder()
                .user(user)
                .product(product)
                .rating(request.getRating())
                .title(request.getTitle())
                .comment(request.getComment())
                .build();

        review = reviewRepository.save(review);
        log.info("Review created for product {} by user {}", product.getName(), user.getEmail());

        // Update product rating
        updateProductRating(productId);

        return mapToResponse(review);
    }

    /**
     * Update a review.
     */
    @Transactional
    public ReviewResponse updateReview(Long reviewId, ReviewRequest request) {
        User user = userService.getCurrentUser();
        
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        // Users can only update their own reviews
        if (!review.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Review", "id", reviewId);
        }

        review.setRating(request.getRating());
        review.setTitle(request.getTitle());
        review.setComment(request.getComment());

        review = reviewRepository.save(review);
        log.info("Review updated: {}", reviewId);

        // Update product rating
        updateProductRating(review.getProduct().getId());

        return mapToResponse(review);
    }

    /**
     * Delete a review.
     */
    @Transactional
    public void deleteReview(Long reviewId) {
        User user = userService.getCurrentUser();
        
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        // Users can only delete their own reviews (or admins)
        if (!review.getUser().getId().equals(user.getId()) && 
            user.getRole() != com.ashu.E_Commerece.model.Role.ADMIN) {
            throw new ResourceNotFoundException("Review", "id", reviewId);
        }

        Long productId = review.getProduct().getId();
        reviewRepository.delete(review);
        log.info("Review deleted: {}", reviewId);

        // Update product rating
        updateProductRating(productId);
    }

    private void updateProductRating(Long productId) {
        Double avgRating = reviewRepository.calculateAverageRatingByProductId(productId);
        long reviewCount = reviewRepository.countByProductId(productId);

        BigDecimal rating = avgRating != null 
            ? BigDecimal.valueOf(avgRating).setScale(2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;

        productService.updateProductRating(productId, rating, (int) reviewCount);
    }

    private ReviewResponse mapToResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .userId(review.getUser().getId())
                .userName(review.getUser().getFirstName() + " " + review.getUser().getLastName())
                .productId(review.getProduct().getId())
                .productName(review.getProduct().getName())
                .rating(review.getRating())
                .title(review.getTitle())
                .comment(review.getComment())
                .verified(review.isVerified())
                .createdAt(review.getCreatedAt())
                .build();
    }

    private PagedResponse<ReviewResponse> mapToPagedResponse(Page<Review> page) {
        List<ReviewResponse> content = page.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PagedResponse.<ReviewResponse>builder()
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
