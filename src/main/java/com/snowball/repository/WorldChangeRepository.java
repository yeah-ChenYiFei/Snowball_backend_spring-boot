package com.snowball.repository;

import com.snowball.entity.WorldChange;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WorldChangeRepository extends JpaRepository<WorldChange, Long> {

    List<WorldChange> findByWorldIdOrderByCreatedAtDesc(Long worldId);

    List<WorldChange> findByWorldIdAndStatusOrderByCreatedAtDesc(Long worldId, WorldChange.ChangeStatus status);

    Optional<WorldChange> findByIdAndWorldId(Long id, Long worldId);
}
