package com.snowball.repository;
import com.snowball.entity.BattleEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BattleEntryRepository extends JpaRepository<BattleEntry, Long> {
    List<BattleEntry> findByBattleIdOrderByCreatedAtAsc(Long battleId);
}
