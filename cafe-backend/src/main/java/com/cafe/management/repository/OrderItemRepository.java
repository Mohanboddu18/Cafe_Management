package com.cafe.management.repository;

import com.cafe.management.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrderId(Long orderId);

    @Query("SELECT oi.menuItem.name, SUM(oi.quantity) as totalQty FROM OrderItem oi GROUP BY oi.menuItem.id, oi.menuItem.name ORDER BY totalQty DESC")
    List<Object[]> findPopularMenuItems();
}
