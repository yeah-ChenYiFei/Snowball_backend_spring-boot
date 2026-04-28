package com.example.snowball.repository;
import com.example.snowball.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostIdAndIsDeletedFalseOrderByCreatedAtAsc(Long postId);
}
