package com.snowball.controller;

import com.snowball.common.Result;
import com.snowball.dto.AiContinueRequest;
import com.snowball.dto.AiContinueResponse;
import com.snowball.service.AiService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai")
public class AiController extends BaseController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/continue")
    public Result<AiContinueResponse> continueNovel(@Valid @RequestBody AiContinueRequest request) {
        Long userId = getCurrentUserId();
        AiContinueResponse result = aiService.continueNovel(request.getArticleId(), userId);
        return Result.success(result);
    }
}
