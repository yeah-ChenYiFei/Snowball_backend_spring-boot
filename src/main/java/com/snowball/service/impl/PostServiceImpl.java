package com.snowball.service.impl;

import com.snowball.common.BusinessException;
import com.snowball.dto.PostCreateDTO;
import com.snowball.dto.PostUpdateDTO;
import com.snowball.entity.Post;
import com.snowball.entity.PostReaction;
import com.snowball.entity.PostTag;
import com.snowball.entity.PostVersion;
import com.snowball.entity.Tag;
import com.snowball.entity.User;
import com.snowball.repository.CommentRepository;
import com.snowball.repository.PostRepository;
import com.snowball.repository.PostReactionRepository;
import com.snowball.repository.PostTagRepository;
import com.snowball.repository.PostVersionRepository;
import com.snowball.repository.TagRepository;
import com.snowball.repository.UserRepository;
import com.snowball.service.NotificationService;
import com.snowball.service.PostService;
import com.snowball.vo.PostDetailVO;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final PostVersionRepository postVersionRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final PostReactionRepository postReactionRepository;
    private final NotificationService notificationService;
    private final PostTagRepository postTagRepository;
    private final TagRepository tagRepository;

    public PostServiceImpl(PostRepository postRepository, PostVersionRepository postVersionRepository,
                           UserRepository userRepository, CommentRepository commentRepository,
                           PostReactionRepository postReactionRepository, NotificationService notificationService,
                           PostTagRepository postTagRepository, TagRepository tagRepository) {
        this.postRepository = postRepository;
        this.postVersionRepository = postVersionRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.postReactionRepository = postReactionRepository;
        this.notificationService = notificationService;
        this.postTagRepository = postTagRepository;
        this.tagRepository = tagRepository;
    }

    private void syncTags(Long postId, List<String> tagNames) {
        postTagRepository.deleteByPostId(postId);
        if (tagNames == null || tagNames.isEmpty()) return;
        for (String name : tagNames) {
            String trimmed = name.trim();
            if (trimmed.isEmpty()) continue;
            Tag tag = tagRepository.findByName(trimmed).orElseGet(() -> {
                Tag t = new Tag();
                t.setName(trimmed);
                return tagRepository.save(t);
            });
            PostTag pt = new PostTag();
            pt.setPostId(postId);
            pt.setTagId(tag.getId());
            postTagRepository.save(pt);
        }
    }

    private Map<Long, List<String>> batchLoadTags(List<Long> postIds) {
        List<PostTag> allPt = postTagRepository.findByPostIdIn(postIds);
        if (allPt.isEmpty()) return Map.of();
        List<Long> tagIds = allPt.stream().map(PostTag::getTagId).distinct().toList();
        Map<Long, String> tagNameMap = tagRepository.findAllById(tagIds).stream()
                .collect(Collectors.toMap(Tag::getId, Tag::getName));
        Map<Long, List<String>> result = new HashMap<>();
        for (PostTag pt : allPt) {
            String name = tagNameMap.get(pt.getTagId());
            if (name != null) {
                result.computeIfAbsent(pt.getPostId(), k -> new ArrayList<>()).add(name);
            }
        }
        return result;
    }

    @Override
    @Transactional
    public PostDetailVO createPost(Long userId, PostCreateDTO dto) {
        Post post = new Post();
        post.setUserId(userId);
        post.setType(Post.PostType.valueOf(dto.getType()));
        post.setTitle(dto.getTitle());
        post.setCurrentBody(dto.getBody());
        post.setChapter(dto.getChapter());
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            post.setImages(toJson(dto.getImages()));
        }
        postRepository.save(post);

        PostVersion version = new PostVersion();
        version.setPostId(post.getId());
        version.setVersionNumber(1);
        version.setBodySnapshot(dto.getBody());
        version.setImagesSnapshot(post.getImages());
        version.setChangeSummary("初始创建");
        postVersionRepository.save(version);

        syncTags(post.getId(), dto.getTags());

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
            versionRecord.setImagesSnapshot(post.getImages());
            versionRecord.setChangeSummary("回滚至版本 V" + targetVersion.getVersionNumber());
            postVersionRepository.save(versionRecord);

            post.setCurrentBody(targetVersion.getBodySnapshot());
            if (targetVersion.getImagesSnapshot() != null) {
                post.setImages(targetVersion.getImagesSnapshot());
            }
            postRepository.save(post);

            PostDetailVO vo = convertToVO(post);
            vo.setCommentCount(commentRepository.countByPostIdAndIsDeletedFalse(post.getId()));
            return vo;
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new BusinessException(409, "数据冲突，请刷新后重试");
        }
    }

    @Override
    public List<PostDetailVO> getAllPosts(Long userId) {
        List<Post> posts = postRepository.findByStatusNotIn(Arrays.asList("HIDDEN", "DELETED"));
        if (posts.isEmpty()) return List.of();

        List<Long> postIds = posts.stream().map(Post::getId).toList();
        List<Long> creatorIds = posts.stream().map(Post::getUserId).distinct().toList();

        Map<Long, String> usernameMap = userRepository.findAllById(creatorIds).stream()
                .collect(Collectors.toMap(User::getId, User::getUsername));
        Map<Long, Integer> commentCountMap = commentRepository.countByPostIdIn(postIds).stream()
                .collect(Collectors.toMap(row -> (Long) row[0], row -> ((Number) row[1]).intValue()));
        Map<Long, List<String>> tagsMap = batchLoadTags(postIds);

        Map<Long, Long> likeCountMap = new HashMap<>();
        Map<Long, Long> dislikeCountMap = new HashMap<>();
        postReactionRepository.countGroupByPostIds(postIds).forEach(row -> {
            Long pid = (Long) row[0];
            PostReaction.ReactionType type = (PostReaction.ReactionType) row[1];
            long cnt = (Long) row[2];
            if (type == PostReaction.ReactionType.LIKE) likeCountMap.put(pid, cnt);
            else dislikeCountMap.put(pid, cnt);
        });

        Map<Long, PostReaction.ReactionType> reactionMap = (userId != null)
                ? postReactionRepository.findByPostIdInAndUserId(postIds, userId).stream()
                        .collect(Collectors.toMap(PostReaction::getPostId, PostReaction::getReactionType, (a, b) -> a))
                : Map.of();

        List<PostDetailVO> voList = posts.stream().map(post -> {
            PostDetailVO vo = convertToVO(post, usernameMap, tagsMap);
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
        Map<Long, List<String>> tagsMap = batchLoadTags(postIds);

        return posts.stream().map(post -> {
            PostDetailVO vo = convertToVO(post, Map.of(), tagsMap);
            vo.setCommentCount(commentCountMap.getOrDefault(post.getId(), 0));
            return vo;
        }).toList();
    }

    @Override
    @Transactional
    public PostDetailVO getPostById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "帖子不存在"));
        post.setViewCount((post.getViewCount() != null ? post.getViewCount() : 0) + 1);
        postRepository.save(post);
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
            version.setImagesSnapshot(post.getImages());
            version.setChangeSummary(dto.getChangeSummary());
            postVersionRepository.save(version);

            post.setTitle(dto.getTitle());
            post.setCurrentBody(dto.getBody());
            if (dto.getChapter() != null) {
                post.setChapter(dto.getChapter());
            }
            postRepository.save(post);

            if (dto.getTags() != null) {
                syncTags(post.getId(), dto.getTags());
            }
            if (dto.getImages() != null) {
                post.setImages(toJson(dto.getImages()));
                postRepository.save(post);
            }

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
        List<String> tags = postTagRepository.findByPostId(post.getId()).stream()
                .map(pt -> tagRepository.findById(pt.getTagId()).map(Tag::getName).orElse(null))
                .filter(name -> name != null)
                .toList();
        PostDetailVO vo = convertToVO(post, Map.of(), Map.of(post.getId(), tags));
        return vo;
    }

    private PostDetailVO convertToVO(Post post, Map<Long, String> usernameMap, Map<Long, List<String>> tagsMap) {
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
        vo.setChapter(post.getChapter());
        vo.setViewCount(post.getViewCount() != null ? post.getViewCount() : 0);
        if (post.getCurrentBody() != null) {
            vo.setWordCount(post.getCurrentBody().length());
        }
        String username = usernameMap.get(post.getUserId());
        if (username != null) {
            vo.setAuthorName(username);
        } else {
            userRepository.findById(post.getUserId()).ifPresent(user -> vo.setAuthorName(user.getUsername()));
        }
        vo.setTags(tagsMap.getOrDefault(post.getId(), List.of()));
        vo.setImages(fromJson(post.getImages()));
        return vo;
    }

    private String toJson(List<String> list) {
        if (list == null || list.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(list.get(i).replace("\\", "\\\\").replace("\"", "\\\"")).append("\"");
        }
        sb.append("]");
        return sb.toString();
    }

    private List<String> fromJson(String json) {
        if (json == null || json.isBlank() || json.equals("[]")) return List.of();
        String inner = json.trim();
        if (inner.startsWith("[")) inner = inner.substring(1);
        if (inner.endsWith("]")) inner = inner.substring(0, inner.length() - 1);
        if (inner.isBlank()) return List.of();
        List<String> result = new ArrayList<>();
        for (String part : inner.split(",")) {
            String url = part.trim();
            if (url.startsWith("\"") && url.endsWith("\"")) {
                url = url.substring(1, url.length() - 1);
                url = url.replace("\\\\", "\\").replace("\\\"", "\"");
            }
            if (!url.isBlank()) result.add(url);
        }
        return result;
    }

    @Override
    public void forceDeletePost(Long id) {
        postRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void react(Long postId, Long userId, String reactionTypeStr) {
        PostReaction.ReactionType newType = PostReaction.ReactionType.valueOf(reactionTypeStr);
        Optional<PostReaction> existing = postReactionRepository.findByPostIdAndUserId(postId, userId);

        boolean isNewLike = false;
        if (existing.isPresent()) {
            PostReaction r = existing.get();
            if (r.getReactionType() == newType) {
                postReactionRepository.delete(r);
            } else {
                r.setReactionType(newType);
                postReactionRepository.save(r);
                if (newType == PostReaction.ReactionType.LIKE) isNewLike = true;
            }
        } else {
            PostReaction r = new PostReaction();
            r.setPostId(postId);
            r.setUserId(userId);
            r.setReactionType(newType);
            postReactionRepository.save(r);
            if (newType == PostReaction.ReactionType.LIKE) isNewLike = true;
        }

        if (isNewLike) {
            Post post = postRepository.findById(postId).orElse(null);
            if (post != null && !post.getUserId().equals(userId)) {
                String actorName = userRepository.findById(userId).map(u -> u.getUsername()).orElse("");
                notificationService.create(post.getUserId(), "LIKE_POST", postId, "POST",
                        userId, actorName + " 赞了你的帖子");
            }
        }
    }

    @Override
    public List<PostDetailVO> searchPosts(String q, String type, String tag) {
        List<String> hiddenStatuses = List.of("HIDDEN", "DELETED");
        List<Post> posts;

        if (tag != null && !tag.isEmpty()) {
            List<Long> taggedPostIds = postTagRepository.findPostIdsByTagName(tag);
            if (taggedPostIds.isEmpty()) return List.of();
            posts = postRepository.findAllById(taggedPostIds).stream()
                    .filter(p -> !hiddenStatuses.contains(p.getStatus()))
                    .toList();
        } else if (q != null && !q.isEmpty()) {
            posts = postRepository.findByTitleContainingIgnoreCaseAndStatusNotIn(q, hiddenStatuses);
        } else if (type != null && !type.isEmpty()) {
            posts = postRepository.findByTypeAndStatusNotIn(Post.PostType.valueOf(type), hiddenStatuses);
        } else {
            posts = postRepository.findByStatusNotIn(hiddenStatuses);
        }
        if (posts.isEmpty()) return List.of();

        List<Long> postIds = posts.stream().map(Post::getId).toList();
        Map<Long, Integer> commentCountMap = commentRepository.countByPostIdIn(postIds).stream()
                .collect(Collectors.toMap(row -> (Long) row[0], row -> ((Number) row[1]).intValue()));
        Map<Long, List<String>> tagsMap = batchLoadTags(postIds);

        return posts.stream().map(post -> {
            PostDetailVO vo = convertToVO(post, Map.of(), tagsMap);
            vo.setCommentCount(commentCountMap.getOrDefault(post.getId(), 0));
            return vo;
        }).toList();
    }
}
