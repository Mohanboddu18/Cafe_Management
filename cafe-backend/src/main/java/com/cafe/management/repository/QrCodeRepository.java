package com.cafe.management.repository;

import com.cafe.management.entity.QrCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QrCodeRepository extends JpaRepository<QrCodeEntity, Long> {
    Optional<QrCodeEntity> findByTableId(Long tableId);
}
