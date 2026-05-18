package com.snowball.repository;
import com.snowball.entity.WritingBattle;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WritingBattleRepository extends JpaRepository<WritingBattle, Long> {
    List<WritingBattle> findByGroupIdOrderByCreatedAtDesc(Long groupId);
}
