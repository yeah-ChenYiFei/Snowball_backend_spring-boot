package com.snowball.repository;

import com.snowball.entity.PostVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostVersionRepository extends JpaRepository<PostVersion, Long> {
    // 查找指定帖子的所有历史版本，按版本号倒序
    List<PostVersion> findByPostIdOrderByVersionNumberDesc(Long postId);
    void deleteByPostId(Long postId);
}
