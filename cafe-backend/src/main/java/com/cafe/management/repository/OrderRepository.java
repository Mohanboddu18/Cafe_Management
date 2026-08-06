package com.cafe.management.repository;

import com.cafe.management.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNumber(String orderNumber);
    List<Order> findByTableIdOrderByOrderTimeDesc(Long tableId);
    List<Order> findByStatus(String status);
    List<Order> findByStatusIn(List<String> statuses);

    @Query("SELECT o FROM Order o WHERE o.table.id = :tableId AND o.status NOT IN ('COMPLETED', 'CANCELLED', 'PAID') ORDER BY o.orderTime DESC")
    List<Order> findActiveOrdersByTableId(@Param("tableId") Long tableId);

    @Query("SELECT SUM(o.netAmount) FROM Order o WHERE o.status IN ('COMPLETED', 'SERVED') AND o.orderTime >= :startDate AND o.orderTime <= :endDate")
    BigDecimal calculateSalesBetweenDates(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderTime >= :startDate AND o.orderTime <= :endDate")
    Long countOrdersBetweenDates(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
