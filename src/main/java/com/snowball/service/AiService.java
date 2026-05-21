package com.snowball.service;

import com.snowball.dto.AiContinueResponse;

public interface AiService {
    AiContinueResponse continueNovel(Long articleId, Long userId);
}
