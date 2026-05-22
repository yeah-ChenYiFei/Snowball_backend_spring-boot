package com.snowball.controller;

import com.snowball.common.Result;
import com.snowball.dto.AiContinueRequest;
import com.snowball.dto.AiContinueResponse;
import com.snowball.service.AiService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

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
        // Old format: {articleId}
        Object articleIdObj = request.get("articleId");
        if (articleIdObj instanceof Number) {
            return Result.success(aiService.continueNovel(((Number) articleIdObj).longValue(), userId));
        }
        return Result.success(aiService.continueNovel(request, userId));
    }
}
