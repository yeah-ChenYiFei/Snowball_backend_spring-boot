package com.snowball.repository;

import com.snowball.entity.WorldRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WorldRelationRepository extends JpaRepository<WorldRelation, Long> {

    List<WorldRelation> findByWorldIdOrderByCreatedAtDesc(Long worldId);

    @Query("SELECT r FROM WorldRelation r WHERE r.worldId = :worldId AND (r.fromEntryId = :entryId OR r.toEntryId = :entryId)")
    List<WorldRelation> findByWorldIdAndEntryId(@Param("worldId") Long worldId, @Param("entryId") Long entryId);
}
