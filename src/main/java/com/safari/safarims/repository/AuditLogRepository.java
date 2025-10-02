package com.safari.safarims.repository;

import com.safari.safarims.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByEntityAndEntityIdOrderByCreatedAtDesc(String entity, String entityId);
    List<AuditLog> findByActorUserIdOrderByCreatedAtDesc(Long actorUserId);
    List<AuditLog> findByEntityOrderByCreatedAtDesc(String entity);
}
