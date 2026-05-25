package com.snowball.service;

import com.snowball.dto.AiContinueResponse;

import java.util.List;
import java.util.Map;

public interface AiService {
    AiContinueResponse continueNovel(Long articleId, Long userId);

    AiContinueResponse continueNovel(Map<String, Object> request, Long userId);

    AiContinueResponse continueChain(Long chainId, String prompt, Long userId);

    AiContinueResponse generateWorldStory(Long worldId, List<Long> entryIds, String prompt, Long userId);
}
