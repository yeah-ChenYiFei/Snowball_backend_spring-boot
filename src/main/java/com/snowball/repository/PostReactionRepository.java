package com.snowball.repository;

import com.snowball.entity.PostReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostReactionRepository extends JpaRepository<PostReaction, Long> {

    /** 查询某用户对某帖子的评价 */
    Optional<PostReaction> findByPostIdAndUserId(Long postId, Long userId);

    /** 某帖子的赞数 */
    long countByPostIdAndReactionType(Long postId, PostReaction.ReactionType type);

    /** 批量查询某用户对一组帖子的评价状态（N+1 优化） */
    @Query("SELECT r FROM PostReaction r WHERE r.postId IN :postIds AND r.userId = :userId")
    List<PostReaction> findByPostIdInAndUserId(@Param("postIds") List<Long> postIds, @Param("userId") Long userId);

    /** 删除评价（取消） */
    @Modifying
    void deleteByPostIdAndUserId(Long postId, Long userId);
}
