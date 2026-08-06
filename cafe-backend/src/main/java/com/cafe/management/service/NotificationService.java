package com.cafe.management.service;

import com.cafe.management.entity.Notification;
import com.cafe.management.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public Notification sendNotification(String targetRole, String title, String message, Long orderId, Long tableId) {
        Notification notification = Notification.builder()
                .targetRole(targetRole)
                .title(title)
                .message(message)
                .orderId(orderId)
                .tableId(tableId)
                .isRead(false)
                .build();

        Notification saved = notificationRepository.save(notification);

        // Send via WebSocket STOMP
        messagingTemplate.convertAndSend("/topic/notifications/" + targetRole.toLowerCase(), saved);
        messagingTemplate.convertAndSend("/topic/notifications/all", saved);

        return saved;
    }

    public List<Notification> getNotificationsForRole(String targetRole) {
        return notificationRepository.findByTargetRoleOrderByCreatedAtDesc(targetRole.toUpperCase());
    }

    public void markAsRead(Long id) {
        notificationRepository.findById(id).ifPresent(n -> {
            n.setIsRead(true);
            notificationRepository.save(n);
        });
    }
}
