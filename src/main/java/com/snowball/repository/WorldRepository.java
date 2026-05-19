package com.snowball.repository;

import com.snowball.entity.World;
import org.springframework.data.jpa.repository.JpaRepository;

import com.snowball.entity.WorldCollaborator;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WorldRepository extends JpaRepository<World, Long> {
    List<World> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<World> findByIsPublicTrueAndUserIdNotOrderByCreatedAtDesc(Long userId);

    @Query("SELECT w FROM World w JOIN WorldCollaborator wc ON w.id = wc.worldId WHERE wc.userId = :userId ORDER BY w.createdAt DESC")
    List<World> findByCollaboratorUserId(@Param("userId") Long userId);
}
