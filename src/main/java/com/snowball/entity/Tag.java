package com.snowball.entity;
import jakarta.persistence.*;
import lombok.Data;
@Data
@Entity
@Table(name = "tags")
public class Tag {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 50, unique = true)
    private String name;
    @Column(length = 200)
    private String description;
}
