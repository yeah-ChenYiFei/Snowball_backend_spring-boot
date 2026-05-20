package com.snowball.entity;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "group_messages")
public class GroupMessage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String body;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private MessageType type = MessageType.CHAT;

    @Column(name = "ref_id")
    private Long refId;

    @Column(name = "ref_type", length = 20)
    @Enumerated(EnumType.STRING)
    private RefType refType;

    public enum MessageType { CHAT, CHAIN_START, CHAIN_SEGMENT, BATTLE_START, BATTLE_ENTRY, SYSTEM }
    public enum RefType { CHAIN, BATTLE }

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }
}
