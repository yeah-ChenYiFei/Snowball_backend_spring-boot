package com.snowball.vo;

import java.time.LocalDateTime;

public class CommentVO {
    private Long id;
    private Long postId;
    private String body;
    private Long parentId;
    private Long userId;
    private String authorName;
    private Integer likeCount;
    private Integer dislikeCount;
    private String currentUserReaction;
    private String imageUrl;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    public Integer getLikeCount() { return likeCount; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }
    public Integer getDislikeCount() { return dislikeCount; }
    public void setDislikeCount(Integer dislikeCount) { this.dislikeCount = dislikeCount; }
    public String getCurrentUserReaction() { return currentUserReaction; }
    public void setCurrentUserReaction(String currentUserReaction) { this.currentUserReaction = currentUserReaction; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
