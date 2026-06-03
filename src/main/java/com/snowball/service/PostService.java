package com.snowball.service;

import com.snowball.dto.PostCreateDTO;
import com.snowball.dto.PostUpdateDTO;
import com.snowball.entity.PostVersion;
import com.snowball.vo.PostDetailVO;
import java.util.List;

// ✅ 1. 新增：导入 Post 实体类
import com.snowball.entity.Post;

public interface PostService {
    // 创建
    PostDetailVO createPost(Long userId, PostCreateDTO dto);
    // 获取全站列表
    List<PostDetailVO> getAllPosts(Long userId, String sort);
    // 获取某用户的帖子
    List<PostDetailVO> getUserPosts(Long userId);
    // 获取详情（userId 可空，游客传 null）
    PostDetailVO getPostById(Long id, Long userId);
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

    List<PostDetailVO> searchPosts(String q, String type, String tag);

    // Favorites
    boolean toggleFavorite(Long postId, Long userId);
    List<PostDetailVO> getFavoritePosts(Long userId);

    // Batch delete (returns count of deleted posts)
    int batchDelete(List<Long> ids, Long userId);

    // Batch toggle status
    int batchToggleStatus(List<Long> ids, Long userId, String newStatus);

    // Single post status toggle
    PostDetailVO toggleStatus(Long id, Long userId, String newStatus);
}
