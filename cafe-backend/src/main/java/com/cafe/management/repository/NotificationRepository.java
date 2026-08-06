package com.cafe.management.repository;

import com.cafe.management.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByTargetRoleOrderByCreatedAtDesc(String targetRole);
    List<Notification> findByTargetRoleAndIsReadFalseOrderByCreatedAtDesc(String targetRole);
}
