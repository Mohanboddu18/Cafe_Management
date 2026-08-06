package com.cafe.management.repository;

import com.cafe.management.entity.MenuItemReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemReviewRepository extends JpaRepository<MenuItemReview, Long> {
    List<MenuItemReview> findByMenuItemIdOrderByCreatedAtDesc(Long menuItemId);
    boolean existsByOrderIdAndMenuItemId(Long orderId, Long menuItemId);
}
