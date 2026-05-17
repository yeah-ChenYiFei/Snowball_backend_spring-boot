package com.snowball.service.impl;

import com.snowball.common.BusinessException;
import com.snowball.dto.PostCreateDTO;
import com.snowball.dto.PostUpdateDTO;
import com.snowball.entity.Post;
// ✅ 新增导入
import com.snowball.entity.PostReaction;
import com.snowball.entity.PostVersion;
import com.snowball.repository.CommentRepository;
import com.snowball.repository.PostRepository;
// ✅ 修复：加上了漏掉的引号
import com.snowball.repository.PostReactionRepository;
import com.snowball.repository.PostVersionRepository;
import com.snowball.repository.UserRepository;
import com.snowball.service.PostService;
import com.snowball.vo.PostDetailVO;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
// ✅ 新增导入
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final PostVersionRepository postVersionRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final PostReactionRepository postReactionRepository;

    public PostServiceImpl(PostRepository postRepository, PostVersionRepository postVersionRepository, UserRepository userRepository, CommentRepository commentRepository, PostReactionRepository postReactionRepository) {
        this.postRepository = postRepository;
        this.postVersionRepository = postVersionRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.postReactionRepository = postReactionRepository;
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

        PostVersion version = new PostVersion();
        version.setPostId(post.getId());
        version.setVersionNumber(1);
        version.setBodySnapshot(dto.getBody());
        version.setChangeSummary("初始创建");
        postVersionRepository.save(version);

        PostDetailVO vo = convertToVO(post);
        vo.setCommentCount(0);
        return vo;
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
            PostVersion versionRecord = new PostVersion();
            versionRecord.setPostId(post.getId());
            versionRecord.setVersionNumber(post.getVersion().intValue() + 1);
            versionRecord.setBodySnapshot(post.getCurrentBody());
            versionRecord.setChangeSummary("回滚至版本 V" + targetVersion.getVersionNumber());
            postVersionRepository.save(versionRecord);

            post.setCurrentBody(targetVersion.getBodySnapshot());
            postRepository.save(post);

            PostDetailVO vo = convertToVO(post);
            vo.setCommentCount(commentRepository.countByPostIdAndIsDeletedFalse(post.getId()));
            return vo;
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new BusinessException(409, "数据冲突，请刷新后重试");
        }
    }

    // ✅ 删除了老的空参 getAllPosts() 方法，只保留这个带 userId 的
    @Override
    public List<PostDetailVO> getAllPosts(Long userId) {
        List<Post> posts = postRepository.findByStatusNotIn(Arrays.asList("HIDDEN", "DELETED"));
        if (posts.isEmpty()) return List.of();

        List<Long> postIds = posts.stream().map(Post::getId).toList();

        // 批量查询用户名
        List<Long> userIds = posts.stream().map(Post::getUserId).distinct().toList();
        Map<Long, String> usernameMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(com.snowball.entity.User::getId, com.snowball.entity.User::getUsername));

        // 批量查询评论数
        Map<Long, Integer> commentCountMap = commentRepository.countByPostIdIn(postIds).stream()
                .collect(Collectors.toMap(row -> (Long) row[0], row -> ((Number) row[1]).intValue()));

        // 批量查询赞/踩数
        Map<Long, Long> likeCountMap = new java.util.HashMap<>();
        Map<Long, Long> dislikeCountMap = new java.util.HashMap<>();
        postReactionRepository.countGroupByPostIds(postIds).forEach(row -> {
            Long pid = (Long) row[0];
            PostReaction.ReactionType type = (PostReaction.ReactionType) row[1];
            long cnt = (Long) row[2];
            if (type == PostReaction.ReactionType.LIKE) likeCountMap.put(pid, cnt);
            else dislikeCountMap.put(pid, cnt);
        });

        // 批量查询当前用户的评价状态
        Map<Long, PostReaction.ReactionType> reactionMap = (userId != null)
                ? postReactionRepository.findByPostIdInAndUserId(postIds, userId).stream()
                        .collect(Collectors.toMap(PostReaction::getPostId, PostReaction::getReactionType, (a, b) -> a))
                : Map.of();

        // 组装 VO
        List<PostDetailVO> voList = posts.stream().map(post -> {
            PostDetailVO vo = convertToVO(post, usernameMap);
            vo.setLikeCount(likeCountMap.getOrDefault(post.getId(), 0L));
            vo.setDislikeCount(dislikeCountMap.getOrDefault(post.getId(), 0L));
            vo.setCommentCount(commentCountMap.getOrDefault(post.getId(), 0));
            vo.setCurrentUserReaction(reactionMap.get(post.getId()) != null ? reactionMap.get(post.getId()).name() : null);
            return vo;
        }).collect(Collectors.toList());

        voList.sort((a, b) -> Double.compare(calcHotScore(b), calcHotScore(a)));
        return voList;
    }

    private double calcHotScore(PostDetailVO vo) {
        long netLikes = (vo.getLikeCount() != null ? vo.getLikeCount() : 0) - (vo.getDislikeCount() != null ? vo.getDislikeCount() : 0);
        if (netLikes <= 0) return 0;
        long hoursSincePost = java.time.Duration.between(vo.getCreatedAt(), java.time.LocalDateTime.now()).toHours() + 2;
        return netLikes / Math.pow(hoursSincePost, 1.5);
    }

    @Override
    public List<PostDetailVO> getUserPosts(Long userId) {
        List<Post> posts = postRepository.findByUserIdAndStatusNotInOrderByCreatedAtDesc(userId, Arrays.asList("HIDDEN", "DELETED"));
        if (posts.isEmpty()) return List.of();

        List<Long> postIds = posts.stream().map(Post::getId).toList();
        Map<Long, Integer> commentCountMap = commentRepository.countByPostIdIn(postIds).stream()
                .collect(Collectors.toMap(row -> (Long) row[0], row -> ((Number) row[1]).intValue()));

        return posts.stream().map(post -> {
            PostDetailVO vo = convertToVO(post);
            vo.setCommentCount(commentCountMap.getOrDefault(post.getId(), 0));
            return vo;
        }).toList();
    }

    @Override
    public PostDetailVO getPostById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "帖子不存在"));
        PostDetailVO vo = convertToVO(post);
        vo.setCommentCount(commentRepository.countByPostIdAndIsDeletedFalse(post.getId()));
        return vo;
    }

    @Override
    @Transactional
    public PostDetailVO updatePost(Long id, Long userId, PostUpdateDTO dto) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "帖子不存在"));
        if (!post.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权编辑他人作品");
        }
        try {
            List<PostVersion> existingVersions = postVersionRepository.findByPostIdOrderByVersionNumberDesc(id);
            int currentMaxVer = existingVersions.isEmpty() ? 0 : existingVersions.get(0).getVersionNumber();
            int nextVersion = currentMaxVer + 1;

            PostVersion version = new PostVersion();
            version.setPostId(post.getId());
            version.setVersionNumber(nextVersion);
            version.setBodySnapshot(post.getCurrentBody());
            version.setChangeSummary(dto.getChangeSummary());
            postVersionRepository.save(version);

            post.setTitle(dto.getTitle());
            post.setCurrentBody(dto.getBody());
            postRepository.save(post);
            PostDetailVO vo = convertToVO(post);
            vo.setCommentCount(commentRepository.countByPostIdAndIsDeletedFalse(post.getId()));
            return vo;
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new BusinessException(409, "版本号冲突，请刷新页面后重试");
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new BusinessException(409, "内容已被其他人修改，请刷新后重试");
        }
    }

    @Override
    @Transactional
    public void deletePost(Long id, Long userId) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "帖子不存在"));
        if (!post.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权删除他人作品");
        }
        post.setStatus("HIDDEN");
        postRepository.save(post);
    }

    @Override
    public List<PostVersion> getPostVersions(Long id) {
        if (!postRepository.existsById(id)) {
            throw new BusinessException(404, "帖子不存在");
        }
        return postVersionRepository.findByPostIdOrderByVersionNumberDesc(id);
    }

    public PostDetailVO convertToVO(Post post) {
        return convertToVO(post, Map.of());
    }

    private PostDetailVO convertToVO(Post post, Map<Long, String> usernameMap) {
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
        String username = usernameMap.get(post.getUserId());
        if (username != null) {
            vo.setAuthorName(username);
        } else {
            userRepository.findById(post.getUserId()).ifPresent(user -> vo.setAuthorName(user.getUsername()));
        }
        return vo;
    }

    @Override
    public void forceDeletePost(Long id) {
        postRepository.deleteById(id);
    }

    // ==================== 新增：评价相关方法 ====================
    @Override
    @Transactional
    public void react(Long postId, Long userId, String reactionTypeStr) {
        PostReaction.ReactionType newType = PostReaction.ReactionType.valueOf(reactionTypeStr);
        Optional<PostReaction> existing = postReactionRepository.findByPostIdAndUserId(postId, userId);

        if (existing.isPresent()) {
            PostReaction r = existing.get();
            if (r.getReactionType() == newType) {
                postReactionRepository.delete(r);
            } else {
                r.setReactionType(newType);
                postReactionRepository.save(r);
            }
        } else {
            PostReaction r = new PostReaction();
            r.setPostId(postId);
            r.setUserId(userId);
            r.setReactionType(newType);
            postReactionRepository.save(r);
        }
    }
    @Override
    public List<PostDetailVO> searchPosts(String q, String type) {
        List<String> hiddenStatuses = List.of("HIDDEN", "DELETED");
        List<Post> posts;

        if (q != null && !q.isEmpty()) {
            posts = postRepository.findByTitleContainingIgnoreCaseAndStatusNotIn(q, hiddenStatuses);
        } else if (type != null && !type.isEmpty()) {
            posts = postRepository.findByTypeAndStatusNotIn(type, hiddenStatuses);
        } else {
            posts = postRepository.findByStatusNotIn(hiddenStatuses);
        }
        if (posts.isEmpty()) return List.of();

        List<Long> postIds = posts.stream().map(Post::getId).toList();
        Map<Long, Integer> commentCountMap = commentRepository.countByPostIdIn(postIds).stream()
                .collect(Collectors.toMap(row -> (Long) row[0], row -> ((Number) row[1]).intValue()));

        return posts.stream().map(post -> {
            PostDetailVO vo = convertToVO(post);
            vo.setCommentCount(commentCountMap.getOrDefault(post.getId(), 0));
            return vo;
        }).toList();
    }
}
