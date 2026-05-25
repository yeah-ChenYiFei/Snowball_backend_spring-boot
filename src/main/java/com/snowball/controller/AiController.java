package com.snowball.controller;

import com.snowball.common.Result;
import com.snowball.dto.AiContinueResponse;
import com.snowball.service.AiService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai")
public class AiController extends BaseController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/continue")
    public Result<AiContinueResponse> continueNovel(@RequestBody Map<String, Object> request) {
        Long userId = getCurrentUserId();
        if (request.containsKey("novelId")) {
            return Result.success(aiService.continueNovel(request, userId));
        }
        Object articleIdObj = request.get("articleId");
        if (articleIdObj instanceof Number) {
            return Result.success(aiService.continueNovel(((Number) articleIdObj).longValue(), userId));
        }
        return Result.success(aiService.continueNovel(request, userId));
    }

    @PostMapping("/chain-continue")
    public Result<AiContinueResponse> continueChain(@RequestBody Map<String, Object> request) {
        Long userId = getCurrentUserId();
        Long chainId = request.get("chainId") instanceof Number
                ? ((Number) request.get("chainId")).longValue() : null;
        String prompt = request.get("prompt") instanceof String ? (String) request.get("prompt") : "";
        if (chainId == null) throw new RuntimeException("chainId is required");
        return Result.success(aiService.continueChain(chainId, prompt, userId));
    }

    @PostMapping("/world-story")
    public Result<AiContinueResponse> generateWorldStory(@RequestBody Map<String, Object> request) {
        Long userId = getCurrentUserId();
        Long worldId = request.get("worldId") instanceof Number
                ? ((Number) request.get("worldId")).longValue() : null;
        @SuppressWarnings("unchecked")
        List<Long> entryIds = request.get("entryIds") instanceof List
                ? ((List<?>) request.get("entryIds")).stream()
                    .filter(o -> o instanceof Number)
                    .map(o -> ((Number) o).longValue())
                    .toList()
                : List.of();
        String prompt = request.get("prompt") instanceof String ? (String) request.get("prompt") : "";
        if (worldId == null) throw new RuntimeException("worldId is required");
        return Result.success(aiService.generateWorldStory(worldId, entryIds, prompt, userId));
    }
}
