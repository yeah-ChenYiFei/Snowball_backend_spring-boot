package com.snowball.repository;

import com.snowball.entity.PostTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostTagRepository extends JpaRepository<PostTag, Long> {
    List<PostTag> findByPostId(Long postId);

    void deleteByPostId(Long postId);

    @Query("SELECT pt FROM PostTag pt WHERE pt.postId IN :postIds")
    List<PostTag> findByPostIdIn(@Param("postIds") List<Long> postIds);

    @Query("SELECT pt.postId FROM PostTag pt JOIN Tag t ON pt.tagId = t.id WHERE t.name = :tagName")
    List<Long> findPostIdsByTagName(@Param("tagName") String tagName);
}
