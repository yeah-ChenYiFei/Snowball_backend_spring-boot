package com.snowball.repository;

import com.snowball.entity.World;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorldRepository extends JpaRepository<World, Long> {
    List<World> findByUserIdOrderByCreatedAtDesc(Long userId);
}
