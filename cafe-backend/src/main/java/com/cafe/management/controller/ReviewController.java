package com.cafe.management.controller;

import com.cafe.management.dto.ReviewDTOs.*;
import com.cafe.management.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "*")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @PostMapping("/submit")
    public ResponseEntity<List<ReviewResponse>> submitReviews(@RequestBody SubmitReviewRequest request) {
        return ResponseEntity.ok(reviewService.submitReviews(request));
    }

    @GetMapping("/item/{menuItemId}")
    public ResponseEntity<List<ReviewResponse>> getReviewsForMenuItem(@PathVariable Long menuItemId) {
        return ResponseEntity.ok(reviewService.getReviewsForMenuItem(menuItemId));
    }
}
