package com.cafe.management.repository;

import com.cafe.management.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByTargetRoleOrderByCreatedAtDesc(String targetRole);
    List<Notification> findByTargetRoleAndIsReadFalseOrderByCreatedAtDesc(String targetRole);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query("UPDATE Notification n SET n.isRead = true WHERE UPPER(n.targetRole) = UPPER(:targetRole)")
    void markAllAsReadForRole(@org.springframework.data.repository.query.Param("targetRole") String targetRole);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    void deleteByTargetRole(String targetRole);
}
