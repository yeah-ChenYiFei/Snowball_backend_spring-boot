package com.example.snowball.repository;
import com.example.snowball.entity.ChainSegment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ChainSegmentRepository extends JpaRepository<ChainSegment, Long> {
    List<ChainSegment> findByChainIdOrderByCreatedAtAsc(Long chainId);
}
