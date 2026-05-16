package com.snowball.repository;
import com.snowball.entity.ChainSegment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ChainSegmentRepository extends JpaRepository<ChainSegment, Long> {
    List<ChainSegment> findByChainIdOrderByCreatedAtAsc(Long chainId);
}
