package com.snowball.repository;

import com.snowball.entity.CommentReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommentReactionRepository extends JpaRepository<CommentReaction, Long> {

    Optional<CommentReaction> findByCommentIdAndUserId(Long commentId, Long userId);

    void deleteByCommentIdAndUserId(Long commentId, Long userId);

    long countByCommentIdAndReactionType(Long commentId, CommentReaction.ReactionType type);

    @Query("SELECT cr.commentId FROM CommentReaction cr WHERE cr.commentId IN :commentIds AND cr.userId = :userId")
    List<Long> findUserReactedCommentIds(@Param("commentIds") List<Long> commentIds, @Param("userId") Long userId);
}
