package com.ashu.E_Commerece.repository;

import com.ashu.E_Commerece.model.Order;
import com.ashu.E_Commerece.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Order entity operations with analytics queries.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByUserId(Long userId, Pageable pageable);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    Page<Order> findByUserIdAndStatus(Long userId, OrderStatus status, Pageable pageable);

    Optional<Order> findByOrderNumber(String orderNumber);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.id = :id")
    Optional<Order> findByIdWithItems(@Param("id") Long id);

    // Analytics queries
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status NOT IN ('CANCELLED', 'REFUNDED')")
    BigDecimal calculateTotalRevenue();

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate " +
           "AND o.status NOT IN ('CANCELLED', 'REFUNDED')")
    BigDecimal calculateRevenueByDateRange(@Param("startDate") LocalDateTime startDate, 
                                            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate")
    long countOrdersByDateRange(@Param("startDate") LocalDateTime startDate, 
                                 @Param("endDate") LocalDateTime endDate);

    @Query("SELECT o.status, COUNT(o) FROM Order o GROUP BY o.status")
    List<Object[]> countOrdersByStatus();

    @Query("SELECT FUNCTION('DATE', o.createdAt), SUM(o.totalAmount), COUNT(o) FROM Order o " +
           "WHERE o.createdAt BETWEEN :startDate AND :endDate " +
           "AND o.status NOT IN ('CANCELLED', 'REFUNDED') " +
           "GROUP BY FUNCTION('DATE', o.createdAt) ORDER BY FUNCTION('DATE', o.createdAt)")
    List<Object[]> getDailySalesReport(@Param("startDate") LocalDateTime startDate, 
                                        @Param("endDate") LocalDateTime endDate);

    @Query("SELECT FUNCTION('MONTH', o.createdAt), SUM(o.totalAmount), COUNT(o) FROM Order o " +
           "WHERE o.createdAt BETWEEN :startDate AND :endDate " +
           "AND o.status NOT IN ('CANCELLED', 'REFUNDED') " +
           "GROUP BY FUNCTION('MONTH', o.createdAt) ORDER BY FUNCTION('MONTH', o.createdAt)")
    List<Object[]> getMonthlySalesReport(@Param("startDate") LocalDateTime startDate, 
                                          @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(o) FROM Order o")
    long countTotalOrders();

    @Query("SELECT AVG(o.totalAmount) FROM Order o WHERE o.status NOT IN ('CANCELLED', 'REFUNDED')")
    BigDecimal calculateAverageOrderValue();

    List<Order> findTop10ByOrderByCreatedAtDesc();
}
