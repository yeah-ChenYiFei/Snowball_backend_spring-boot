package com.snowball.repository;

import com.snowball.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    // 按用户查询帖子列表
    Page<Post> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // 查询所有非隐藏/非删除状态的帖子（用于广场展示）
    List<Post> findByStatusNotIn(List<String> excludedStatuses);

    // 👇 补上这个 SearchController 需要的方法：按标题模糊搜索且排除隐藏帖子
    List<Post> findByTitleContainingIgnoreCaseAndStatusNotIn(String title, List<String> excludedStatuses);

    // 👇 补上按类型搜索的方法
    List<Post> findByTypeAndStatusNotIn(String type, List<String> excludedStatuses);
}
