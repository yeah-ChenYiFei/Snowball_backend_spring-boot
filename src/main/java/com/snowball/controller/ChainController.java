package com.snowball.controller;

import com.snowball.common.Result;
import com.snowball.dto.ChainCreateDTO;
import com.snowball.dto.ChainSegmentCreateDTO;
import com.snowball.dto.SegmentCommentCreateDTO;
import com.snowball.dto.SegmentReviewDTO;
import com.snowball.service.ChainService;
import com.snowball.vo.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chains")
public class ChainController extends BaseController {

    private final ChainService chainService;

    public ChainController(ChainService chainService) {
        this.chainService = chainService;
    }

    // ===== Public chains =====

    @GetMapping("/public")
    public Result<List<ChainVO>> getPublicChains() {
        return Result.success(chainService.getPublicChains());
    }

    @GetMapping("/public/{chainId}")
    public Result<ChainDetailVO> getPublicChainDetail(@PathVariable Long chainId) {
        return Result.success(chainService.getChainDetail(chainId));
    }

    @PostMapping("/public")
    public Result<ChainVO> createPublicChain(@Valid @RequestBody ChainCreateDTO dto) {
        dto.setGroupId(null); // enforce public
        return Result.success(chainService.createChain(getCurrentUserId(), dto));
    }

    @PostMapping("/public/{chainId}/join")
    public Result<ChainSegmentVO> joinChain(@PathVariable Long chainId, @Valid @RequestBody ChainSegmentCreateDTO dto) {
        return Result.success(chainService.addSegment(chainId, getCurrentUserId(), dto));
    }

    // ===== Group chains (existing) =====

    @GetMapping
    public Result<List<ChainVO>> getAllChains() {
        return Result.success(chainService.getAllChains());
    }

    @GetMapping("/{chainId}")
    public Result<ChainDetailVO> getChainDetail(@PathVariable Long chainId) {
        return Result.success(chainService.getChainDetail(chainId));
    }

    @PostMapping
    public Result<ChainVO> createChain(@Valid @RequestBody ChainCreateDTO dto) {
        return Result.success(chainService.createChain(getCurrentUserId(), dto));
    }

    @PostMapping("/{chainId}/segments")
    public Result<ChainSegmentVO> addSegment(@PathVariable Long chainId, @Valid @RequestBody ChainSegmentCreateDTO dto) {
        return Result.success(chainService.addSegment(chainId, getCurrentUserId(), dto));
    }

    // ===== Segment comments =====

    @GetMapping("/segments/{segmentId}/comments")
    public Result<List<SegmentCommentVO>> getComments(@PathVariable Long segmentId) {
        return Result.success(chainService.getComments(segmentId));
    }

    @PostMapping("/segments/{segmentId}/comments")
    public Result<SegmentCommentVO> addComment(@PathVariable Long segmentId, @Valid @RequestBody SegmentCommentCreateDTO dto) {
        return Result.success(chainService.addComment(segmentId, getCurrentUserId(), dto));
    }

    // ===== Segment deletion =====

    @DeleteMapping("/segments/{segmentId}")
    public Result<String> deleteSegment(@PathVariable Long segmentId) {
        chainService.deleteSegment(segmentId, getCurrentUserId());
        return Result.success("ok");
    }

    // ===== Segment review =====

    @PutMapping("/segments/{segmentId}/review")
    public Result<String> reviewSegment(@PathVariable Long segmentId, @Valid @RequestBody SegmentReviewDTO dto) {
        chainService.reviewSegment(segmentId, getCurrentUserId(), dto.getStatus());
        return Result.success("ok");
    }
}
