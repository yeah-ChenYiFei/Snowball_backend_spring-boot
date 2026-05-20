package com.snowball.service.impl;

import com.snowball.common.BusinessException;
import com.snowball.dto.CommentCreateDTO;
import com.snowball.entity.Comment;
import com.snowball.entity.CommentReaction;
import com.snowball.entity.Post;
import com.snowball.entity.User;
import com.snowball.repository.CommentReactionRepository;
import com.snowball.repository.CommentRepository;
import com.snowball.repository.PostRepository;
import com.snowball.repository.UserRepository;
import com.snowball.service.CommentService;
import com.snowball.service.NotificationService;
import com.snowball.vo.CommentVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final CommentReactionRepository reactionRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final NotificationService notificationService;

    public CommentServiceImpl(CommentRepository commentRepository,
                              CommentReactionRepository reactionRepository,
                              UserRepository userRepository,
                              PostRepository postRepository,
                              NotificationService notificationService) {
        this.commentRepository = commentRepository;
        this.reactionRepository = reactionRepository;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.notificationService = notificationService;
    }

    @Override
    public List<CommentVO> getCommentsByPostId(Long postId, Long currentUserId) {
        List<Comment> comments = commentRepository.findByPostIdAndIsDeletedFalseOrderByCreatedAtAsc(postId);
        if (comments.isEmpty()) return List.of();

        // Batch usernames
        List<Long> userIds = comments.stream().map(Comment::getUserId).distinct().toList();
        Map<Long, String> usernameMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, User::getUsername));

        // Batch current user reactions
        Set<Long> reactedCommentIds = Set.of();
        if (currentUserId != null) {
            List<Long> commentIds = comments.stream().map(Comment::getId).toList();
            reactedCommentIds = Set.copyOf(reactionRepository.findUserReactedCommentIds(commentIds, currentUserId));
        }

        List<CommentVO> voList = new ArrayList<>();
        for (Comment c : comments) {
            CommentVO vo = new CommentVO();
            vo.setId(c.getId());
            vo.setPostId(c.getPostId());
            vo.setBody(c.getBody());
            vo.setParentId(c.getParentId());
            vo.setUserId(c.getUserId());
            vo.setAuthorName(usernameMap.getOrDefault(c.getUserId(), ""));
            vo.setLikeCount(c.getLikeCount());
            vo.setDislikeCount(c.getDislikeCount());
            vo.setCurrentUserReaction(reactedCommentIds.contains(c.getId()) ? "LIKE" : null);
            vo.setImageUrl(c.getImageUrl());
            vo.setCreatedAt(c.getCreatedAt());
            voList.add(vo);
        }
        return voList;
    }

    @Override
    @Transactional
    public void createComment(Long postId, Long userId, CommentCreateDTO dto) {
        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setBody(dto.getBody());
        comment.setParentId(dto.getParentId());
        comment.setImageUrl(dto.getImageUrl());
        commentRepository.save(comment);

        // Load post owner for notification
        Post post = postRepository.findById(postId).orElse(null);
        if (post == null) return;

        String actorName = userRepository.findById(userId).map(User::getUsername).orElse("");

        if (dto.getParentId() != null) {
            // Reply: notify parent comment owner
            Comment parentComment = commentRepository.findById(dto.getParentId()).orElse(null);
            if (parentComment != null && !parentComment.getUserId().equals(userId)) {
                String preview = dto.getBody().length() > 80 ? dto.getBody().substring(0, 80) + "..." : dto.getBody();
                notificationService.create(parentComment.getUserId(), "REPLY", comment.getId(), "COMMENT",
                        userId, actorName + " 回复了你的评论：" + preview);
            }
        } else {
            // Top-level comment: notify post owner
            if (!post.getUserId().equals(userId)) {
                String preview = dto.getBody().length() > 80 ? dto.getBody().substring(0, 80) + "..." : dto.getBody();
                notificationService.create(post.getUserId(), "COMMENT", comment.getId(), "COMMENT",
                        userId, actorName + " 评论了你的帖子：" + preview);
            }
        }
    }

    @Override
    public List<CommentVO> getCommentsBySource(String sourceType, Long sourceId, Long currentUserId) {
        List<Comment> comments = commentRepository.findBySourceTypeAndSourceIdAndIsDeletedFalseOrderByCreatedAtAsc(sourceType, sourceId);
        if (comments.isEmpty()) return List.of();

        List<Long> userIds = comments.stream().map(Comment::getUserId).distinct().toList();
        Map<Long, String> usernameMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, User::getUsername));

        List<CommentVO> voList = new ArrayList<>();
        for (Comment c : comments) {
            CommentVO vo = new CommentVO();
            vo.setId(c.getId());
            vo.setPostId(c.getSourceId());
            vo.setBody(c.getBody());
            vo.setParentId(c.getParentId());
            vo.setUserId(c.getUserId());
            vo.setAuthorName(usernameMap.getOrDefault(c.getUserId(), ""));
            vo.setLikeCount(c.getLikeCount() != null ? c.getLikeCount() : 0);
            vo.setDislikeCount(c.getDislikeCount() != null ? c.getDislikeCount() : 0);
            vo.setImageUrl(c.getImageUrl());
            vo.setCreatedAt(c.getCreatedAt());
            voList.add(vo);
        }
        return voList;
    }

    @Override
    @Transactional
    public void createGenericComment(String sourceType, Long sourceId, Long userId, CommentCreateDTO dto) {
        Comment comment = new Comment();
        comment.setSourceType(sourceType);
        comment.setSourceId(sourceId);
        comment.setUserId(userId);
        comment.setBody(dto.getBody());
        comment.setParentId(dto.getParentId());
        comment.setImageUrl(dto.getImageUrl());
        commentRepository.save(comment);
    }

    @Override
    @Transactional
    public void reactToComment(Long commentId, Long userId, String reactionType) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(404, "评论不存在"));

        int curLike = comment.getLikeCount() != null ? comment.getLikeCount() : 0;
        int curDislike = comment.getDislikeCount() != null ? comment.getDislikeCount() : 0;

        reactionRepository.findByCommentIdAndUserId(commentId, userId).ifPresentOrElse(
                existing -> {
                    if (existing.getReactionType() == CommentReaction.ReactionType.LIKE) {
                        comment.setLikeCount(Math.max(0, curLike - 1));
                    } else {
                        comment.setDislikeCount(Math.max(0, curDislike - 1));
                    }
                    reactionRepository.delete(existing);
                },
                () -> {
                    CommentReaction r = new CommentReaction();
                    r.setCommentId(commentId);
                    r.setUserId(userId);
                    r.setReactionType(CommentReaction.ReactionType.valueOf(reactionType));
                    reactionRepository.save(r);
                    comment.setLikeCount(curLike + 1);

                    // Notify comment owner
                    if (!comment.getUserId().equals(userId)) {
                        String actorName = userRepository.findById(userId).map(User::getUsername).orElse("");
                        notificationService.create(comment.getUserId(), "LIKE_COMMENT", commentId, "COMMENT",
                                userId, actorName + " 赞了你的评论");
                    }
                }
        );
        commentRepository.save(comment);
    }
}
