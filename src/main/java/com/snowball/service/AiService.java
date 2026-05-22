package com.snowball.service;

import com.snowball.dto.AiContinueResponse;

import java.util.Map;

public interface AiService {
    AiContinueResponse continueNovel(Long articleId, Long userId);

    AiContinueResponse continueNovel(Map<String, Object> request, Long userId);
}
