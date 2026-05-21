package com.snowball.dto;

import jakarta.validation.constraints.NotNull;

public class AiContinueRequest {
    @NotNull
    private Long articleId;

    public Long getArticleId() { return articleId; }
    public void setArticleId(Long articleId) { this.articleId = articleId; }
}
