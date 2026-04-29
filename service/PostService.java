package com.example.snowball.service;

import com.example.snowball.dto.PostCreateDTO;
import com.example.snowball.dto.PostUpdateDTO;
import com.example.snowball.entity.PostVersion;
import com.example.snowball.vo.PostDetailVO;
import java.util.List;

// ✅ 1. 新增：导入 Post 实体类
import com.example.snowball.entity.Post;

public interface PostService {
    // 创建
    PostDetailVO createPost(Long userId, PostCreateDTO dto);
    // 获取列表
    List<PostDetailVO> getAllPosts(Long userId);
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
    // ✅ 2. 新增：把实现类里的转换方法暴露到接口中
    PostDetailVO convertToVO(Post post);

    void forceDeletePost(Long id);
    // 在 PostService 接口里加上这个方法声明
    void react(Long postId, Long userId, String reactionTypeStr);

}
