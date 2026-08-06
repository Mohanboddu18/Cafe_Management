package com.cafe.management.repository;

import com.cafe.management.entity.RestaurantTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RestaurantTableRepository extends JpaRepository<RestaurantTable, Long> {
    Optional<RestaurantTable> findByTableNumber(Integer tableNumber);
    Optional<RestaurantTable> findByQrToken(String qrToken);
    List<RestaurantTable> findByStatus(String status);
}
