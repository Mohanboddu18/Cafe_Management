package com.cafe.management.service;

import com.cafe.management.entity.AuditLog;
import com.cafe.management.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    public void log(Long userId, String username, String action, String targetEntity, String details, String ipAddress) {
        AuditLog log = AuditLog.builder()
                .userId(userId)
                .username(username != null ? username : "System")
                .action(action)
                .targetEntity(targetEntity)
                .details(details)
                .ipAddress(ipAddress != null ? ipAddress : "127.0.0.1")
                .build();

        auditLogRepository.save(log);
    }

    public List<AuditLog> getAllLogs() {
        return auditLogRepository.findAllByOrderByTimestampDesc();
    }
}
