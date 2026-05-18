package com.snowball.repository;
import com.snowball.entity.BattleReview;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BattleReviewRepository extends JpaRepository<BattleReview, Long> {
    List<BattleReview> findByEntryIdOrderByCreatedAtAsc(Long entryId);
}
