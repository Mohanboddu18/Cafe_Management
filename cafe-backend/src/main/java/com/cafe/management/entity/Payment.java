package com.cafe.management.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    @org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
    private Invoice invoice;

    @Column(name = "payment_method", nullable = false, length = 30)
    private String paymentMethod; // CASH, CARD, UPI

    @Column(name = "transaction_ref", length = 100)
    private String transactionRef;

    @Column(name = "payment_status", nullable = false, length = 20)
    private String paymentStatus = "COMPLETED"; // PENDING, COMPLETED, FAILED

    @Column(name = "paid_at", updatable = false)
    private LocalDateTime paidAt;

    @PrePersist
    protected void onCreate() {
        paidAt = LocalDateTime.now();
    }
}
