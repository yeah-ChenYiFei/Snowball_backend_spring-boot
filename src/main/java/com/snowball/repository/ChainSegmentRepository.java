package com.snowball.repository;
import com.snowball.entity.ChainSegment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
public interface ChainSegmentRepository extends JpaRepository<ChainSegment, Long> {
    List<ChainSegment> findByChainIdOrderByCreatedAtAsc(Long chainId);
    int countByChainId(Long chainId);

    @Query("SELECT DISTINCT cs.chainId FROM ChainSegment cs WHERE cs.userId = :userId")
    List<Long> findDistinctChainIdsByUserId(Long userId);
}
