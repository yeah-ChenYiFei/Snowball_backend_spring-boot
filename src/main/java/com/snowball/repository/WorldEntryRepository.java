package com.snowball.repository;

import com.snowball.entity.WorldEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WorldEntryRepository extends JpaRepository<WorldEntry, Long> {

    List<WorldEntry> findByWorldIdOrderByCreatedAtDesc(Long worldId);

    List<WorldEntry> findByWorldIdAndTypeOrderByCreatedAtDesc(Long worldId, String type);

    @Query("SELECT e FROM WorldEntry e WHERE e.worldId = :worldId AND e.name LIKE %:keyword% ORDER BY e.createdAt DESC")
    List<WorldEntry> searchByWorldIdAndName(@Param("worldId") Long worldId, @Param("keyword") String keyword);

    @Query("SELECT e FROM WorldEntry e WHERE e.worldId = :worldId AND e.type = :type AND e.name LIKE %:keyword% ORDER BY e.createdAt DESC")
    List<WorldEntry> searchByWorldIdAndTypeAndName(@Param("worldId") Long worldId, @Param("type") String type, @Param("keyword") String keyword);

    @Query("SELECT DISTINCT e.type FROM WorldEntry e WHERE e.worldId = :worldId AND e.type IS NOT NULL")
    List<String> findDistinctTypesByWorldId(@Param("worldId") Long worldId);
}
