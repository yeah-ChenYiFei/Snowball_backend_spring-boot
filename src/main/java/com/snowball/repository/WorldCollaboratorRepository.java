package com.snowball.repository;

import com.snowball.entity.WorldCollaborator;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WorldCollaboratorRepository extends JpaRepository<WorldCollaborator, Long> {

    List<WorldCollaborator> findByWorldId(Long worldId);

    List<WorldCollaborator> findByUserId(Long userId);

    Optional<WorldCollaborator> findByWorldIdAndUserId(Long worldId, Long userId);

    boolean existsByWorldIdAndUserId(Long worldId, Long userId);

    void deleteByWorldIdAndUserId(Long worldId, Long userId);

    long countByWorldId(Long worldId);
}
