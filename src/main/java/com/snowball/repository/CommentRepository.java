package com.snowball.repository;
import com.snowball.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostIdAndIsDeletedFalseOrderByCreatedAtAsc(Long postId);
    int countByPostIdAndIsDeletedFalse(Long postId);

    List<Comment> findBySourceTypeAndSourceIdAndIsDeletedFalseOrderByCreatedAtAsc(String sourceType, Long sourceId);
    int countBySourceTypeAndSourceIdAndIsDeletedFalse(String sourceType, Long sourceId);

    @Query("SELECT c.postId, COUNT(c) FROM Comment c WHERE c.postId IN :postIds AND c.isDeleted = false GROUP BY c.postId")
    List<Object[]> countByPostIdIn(@Param("postIds") List<Long> postIds);
}
