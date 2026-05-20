package com.snowball.entity;
import jakarta.persistence.*;
import lombok.Data;
@Data
@Entity
@Table(name = "group_members", indexes = {
    @Index(name = "idx_gm_group_id", columnList = "group_id"),
    @Index(name = "idx_gm_user_id", columnList = "user_id")
})
public class GroupMember {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "group_id", nullable = false)
    private Long groupId;
    @Column(name = "user_id", nullable = false)
    private Long userId;
    @Column(length = 20)
    private String role = "member";
}
