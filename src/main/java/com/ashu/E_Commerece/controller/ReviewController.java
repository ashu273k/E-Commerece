package com.ashu.E_Commerece.controller;

import com.ashu.E_Commerece.dto.common.ApiResponse;
import com.ashu.E_Commerece.dto.common.PagedResponse;
import com.ashu.E_Commerece.dto.review.ReviewRequest;
import com.ashu.E_Commerece.dto.review.ReviewResponse;
import com.ashu.E_Commerece.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Product review APIs")
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/api/products/{productId}/reviews")
    @Operation(summary = "Get reviews for a product")
    public ResponseEntity<ApiResponse<PagedResponse<ReviewResponse>>> getProductReviews(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(reviewService.getProductReviews(productId, page, size)));
    }

    @GetMapping("/api/reviews/my")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get current user's reviews")
    public ResponseEntity<ApiResponse<PagedResponse<ReviewResponse>>> getUserReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(reviewService.getUserReviews(page, size)));
    }

    @PostMapping("/api/products/{productId}/reviews")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create a review for a product")
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @PathVariable Long productId, @Valid @RequestBody ReviewRequest request) {
        ReviewResponse response = reviewService.createReview(productId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Review created", response));
    }

    @PutMapping("/api/reviews/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update a review")
    public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(
            @PathVariable Long id, @Valid @RequestBody ReviewRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Review updated", reviewService.updateReview(id, request)));
    }

    @DeleteMapping("/api/reviews/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete a review")
    public ResponseEntity<ApiResponse<Void>> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.ok(ApiResponse.success("Review deleted", null));
    }
}
