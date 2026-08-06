package com.cafe.management.repository;

import com.cafe.management.entity.StockHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockHistoryRepository extends JpaRepository<StockHistory, Long> {
    List<StockHistory> findByInventoryIdOrderByCreatedAtDesc(Long inventoryId);
}
