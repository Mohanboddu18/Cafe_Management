package com.cafe.management.service;

import com.cafe.management.dto.ReviewDTOs.*;
import com.cafe.management.entity.MenuItem;
import com.cafe.management.entity.MenuItemReview;
import com.cafe.management.entity.Order;
import com.cafe.management.exception.ResourceNotFoundException;
import com.cafe.management.repository.MenuItemRepository;
import com.cafe.management.repository.MenuItemReviewRepository;
import com.cafe.management.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    @Autowired
    private MenuItemReviewRepository reviewRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Transactional
    public List<ReviewResponse> submitReviews(SubmitReviewRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + request.getOrderId()));

        String custName = request.getCustomerName() != null && !request.getCustomerName().isBlank()
                ? request.getCustomerName()
                : (order.getCustomer() != null && order.getCustomer().getName() != null ? order.getCustomer().getName() : "Valued Guest");

        List<MenuItemReview> savedReviews = new ArrayList<>();

        for (ItemRatingRequest itemRating : request.getRatings()) {
            if (itemRating.getRating() == null || itemRating.getRating() < 1 || itemRating.getRating() > 5) {
                continue;
            }

            MenuItem menuItem = menuItemRepository.findById(itemRating.getMenuItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Menu item not found: " + itemRating.getMenuItemId()));

            MenuItemReview review = MenuItemReview.builder()
                    .menuItem(menuItem)
                    .order(order)
                    .rating(itemRating.getRating())
                    .comment(itemRating.getComment())
                    .customerName(custName)
                    .createdAt(LocalDateTime.now())
                    .build();

            if (review.getCreatedAt() == null) {
                review.setCreatedAt(LocalDateTime.now());
            }

            savedReviews.add(reviewRepository.save(review));

            // Recalculate average rating & total count for MenuItem
            double oldAvg = menuItem.getAverageRating() != null ? menuItem.getAverageRating() : 5.0;
            int oldTotal = menuItem.getTotalRatings() != null ? menuItem.getTotalRatings() : 0;

            int newTotal = oldTotal + 1;
            double newAvg = ((oldAvg * oldTotal) + itemRating.getRating()) / (double) newTotal;
            newAvg = Math.round(newAvg * 10.0) / 10.0;

            menuItem.setAverageRating(newAvg);
            menuItem.setTotalRatings(newTotal);
            menuItemRepository.save(menuItem);
        }

        return savedReviews.stream()
                .map(r -> ReviewResponse.builder()
                        .id(r.getId())
                        .menuItemId(r.getMenuItem() != null ? r.getMenuItem().getId() : null)
                        .menuItemName(r.getMenuItem() != null ? r.getMenuItem().getName() : "Item")
                        .rating(r.getRating())
                        .comment(r.getComment())
                        .customerName(r.getCustomerName())
                        .createdAt(r.getCreatedAt() != null ? r.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                        .build())
                .collect(Collectors.toList());
    }

    public List<ReviewResponse> getReviewsForMenuItem(Long menuItemId) {
        return reviewRepository.findByMenuItemIdOrderByCreatedAtDesc(menuItemId).stream()
                .map(r -> ReviewResponse.builder()
                        .id(r.getId())
                        .menuItemId(r.getMenuItem() != null ? r.getMenuItem().getId() : null)
                        .menuItemName(r.getMenuItem() != null ? r.getMenuItem().getName() : "Item")
                        .rating(r.getRating())
                        .comment(r.getComment())
                        .customerName(r.getCustomerName())
                        .createdAt(r.getCreatedAt() != null ? r.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                        .build())
                .collect(Collectors.toList());
    }
}
