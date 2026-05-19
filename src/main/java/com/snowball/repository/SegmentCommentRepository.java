package com.snowball.repository;

import com.snowball.entity.SegmentComment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SegmentCommentRepository extends JpaRepository<SegmentComment, Long> {
    List<SegmentComment> findBySegmentIdOrderByCreatedAtAsc(Long segmentId);
    int countBySegmentId(Long segmentId);
}
