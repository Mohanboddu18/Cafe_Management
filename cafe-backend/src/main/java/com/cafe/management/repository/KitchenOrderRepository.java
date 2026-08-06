package com.cafe.management.repository;

import com.cafe.management.entity.KitchenOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KitchenOrderRepository extends JpaRepository<KitchenOrder, Long> {
    Optional<KitchenOrder> findByOrderId(Long orderId);
    List<KitchenOrder> findByKitchenStatusNotIn(List<String> statuses);
}
