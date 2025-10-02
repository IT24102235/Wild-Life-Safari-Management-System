package com.safari.safarims.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_user_id")
    private User actorUser;

    @Column(name = "entity", nullable = false, length = 50)
    private String entity;

    @Column(name = "entity_id", nullable = false, length = 50)
    private String entityId;

    @Column(name = "action", nullable = false, length = 20)
    private String action; // CREATE, UPDATE, DELETE

    @Column(name = "before_json", columnDefinition = "NVARCHAR(MAX)")
    private String beforeJson;

    @Column(name = "after_json", columnDefinition = "NVARCHAR(MAX)")
    private String afterJson;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
