package com.example.snowball.service.impl;

import com.example.snowball.common.BusinessException;
import com.example.snowball.dto.PostCreateDTO;
import com.example.snowball.dto.PostUpdateDTO;
import com.example.snowball.entity.Post;
import com.example.snowball.entity.PostVersion;
import com.example.snowball.repository.CommentRepository;
import com.example.snowball.repository.PostRepository;
import com.example.snowball.repository.PostVersionRepository;
import com.example.snowball.repository.UserRepository;
import com.example.snowball.service.PostService;
import com.example.snowball.vo.PostDetailVO;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final PostVersionRepository postVersionRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    public PostServiceImpl(PostRepository postRepository, PostVersionRepository postVersionRepository, UserRepository userRepository, CommentRepository commentRepository) {
        this.postRepository = postRepository;
        this.postVersionRepository = postVersionRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
    }

    @Override
    @Transactional
    public PostDetailVO createPost(Long userId, PostCreateDTO dto) {
        Post post = new Post();
        post.setUserId(userId);
        post.setType(Post.PostType.valueOf(dto.getType()));
        post.setTitle(dto.getTitle());
        post.setCurrentBody(dto.getBody());
        postRepository.save(post);

        // 生成 V1 版本记录
        PostVersion version = new PostVersion();
        version.setPostId(post.getId());
        version.setVersionNumber(1);
        version.setBodySnapshot(dto.getBody());
        version.setChangeSummary("初始创建");
        postVersionRepository.save(version);

        return convertToVO(post);
    }
    @Override
    @Transactional
    public PostDetailVO rollbackPost(Long id, Long verId, Long userId) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "帖子不存在"));
        if (!post.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作他人作品");
        }

        PostVersion targetVersion = postVersionRepository.findById(verId)
                .orElseThrow(() -> new BusinessException(404, "历史版本不存在"));
        if (!targetVersion.getPostId().equals(id)) {
            throw new BusinessException(400, "该版本不属于此帖子");
        }

        try {
            // 1. 把当前状态存为新版本（回滚本身也是一种操作，保留完整历史）
            PostVersion versionRecord = new PostVersion();
            versionRecord.setPostId(post.getId());
            versionRecord.setVersionNumber(post.getVersion().intValue() + 1);
            versionRecord.setBodySnapshot(post.getCurrentBody());
            versionRecord.setChangeSummary("回滚至版本 V" + targetVersion.getVersionNumber());
            postVersionRepository.save(versionRecord);

            // 2. 将目标版本的内容恢复到主表
            post.setCurrentBody(targetVersion.getBodySnapshot());
            postRepository.save(post); // 触发乐观锁

            return convertToVO(post);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new BusinessException(409, "数据冲突，请刷新后重试");
        }
    }

    @Override
    public List<PostDetailVO> getAllPosts() {
        // 排除 HIDDEN 和 DELETED 状态的帖子
        List<Post> posts = postRepository.findByStatusNotIn(Arrays.asList("HIDDEN", "DELETED"));
        return posts.stream().map(this::convertToVO).toList();
    }

    @Override
    public PostDetailVO getPostById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("帖子不存在"));
        return convertToVO(post);
    }

    @Override
    @Transactional
    public PostDetailVO updatePost(Long id, Long userId, PostUpdateDTO dto) {
        // 1. 查询原帖子
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("帖子不存在"));

        // 简单权限校验（未来替换为 JWT 校验）
        if (!post.getUserId().equals(userId)) {
            throw new RuntimeException("无权编辑他人作品"); // 对应文档返回 403
        }

        try {
            // 2. 生成不可变的历史版本快照（存入 post_versions 表）
            PostVersion version = new PostVersion();
            version.setPostId(post.getId());
            version.setVersionNumber(post.getVersion().intValue() + 1); // 版本号+1
            version.setBodySnapshot(post.getCurrentBody()); // 存旧内容
            version.setChangeSummary(dto.getChangeSummary());
            postVersionRepository.save(version);

            // 3. 更新当前帖子内容
            post.setTitle(dto.getTitle());
            post.setCurrentBody(dto.getBody());

            // 4. 保存帖子。@Version 注解会自动比对 version 字段，
            // 如果并发导致 version 不匹配，JPA 会抛出 ObjectOptimisticLockingFailureException
            postRepository.save(post);

            return convertToVO(post);

        } catch (ObjectOptimisticLockingFailureException e) {
            // 对应文档 3.1.2：并发冲突使用乐观锁返回 409
            throw new RuntimeException("CONFLICT: 内容已被其他人修改，请刷新后重试");
        }
    }

    @Override
    @Transactional
    public void deletePost(Long id, Long userId) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("帖子不存在"));

        if (!post.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除他人作品");
        }

        // 对应文档 3.1.1：逻辑删除，标记为隐藏状态
        post.setStatus("HIDDEN");
        postRepository.save(post);
    }

    @Override
    public List<PostVersion> getPostVersions(Long id) {
        if (!postRepository.existsById(id)) {
            throw new RuntimeException("帖子不存在");
        }
        // 复用 Repository 里写好的倒序查询方法
        return postVersionRepository.findByPostIdOrderByVersionNumberDesc(id);
    }

    // --- 私有辅助方法：将 Entity 转换为前端需要的 VO ---
    public PostDetailVO convertToVO(Post post) {
        PostDetailVO vo = new PostDetailVO();
        vo.setId(post.getId());
        vo.setUserId(post.getUserId());
        vo.setTitle(post.getTitle());
        vo.setBody(post.getCurrentBody());
        vo.setType(post.getType().name());
        vo.setStatus(post.getStatus());
        vo.setVersion(post.getVersion());
        vo.setCreatedAt(post.getCreatedAt());
        vo.setUpdatedAt(post.getUpdatedAt());
        userRepository.findById(post.getUserId()).ifPresent(user -> {
            vo.setAuthorName(user.getUsername()); // 确保 PostDetailVO 里有 private String authorName; 字段
        });
        vo.setCommentCount(commentRepository.countByPostIdAndIsDeletedFalse(post.getId()));
        return vo;
    }
}
