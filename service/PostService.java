package com.example.snowball.service;

import com.example.snowball.dto.PostCreateDTO;
import com.example.snowball.dto.PostUpdateDTO;
import com.example.snowball.entity.PostVersion;
import com.example.snowball.vo.PostDetailVO;

import java.util.List;

public interface PostService {
    // 创建
    PostDetailVO createPost(Long userId, PostCreateDTO dto);

    // 获取列表
    List<PostDetailVO> getAllPosts();

    // 获取详情
    PostDetailVO getPostById(Long id);

    // 编辑（含版本控制核心算法）
    PostDetailVO updatePost(Long id, Long userId, PostUpdateDTO dto);

    // 逻辑删除
    void deletePost(Long id, Long userId);

    // 获取历史版本列表
    List<PostVersion> getPostVersions(Long id);
    // 回滚到指定版本
    PostDetailVO rollbackPost(Long id, Long verId, Long userId);
}
