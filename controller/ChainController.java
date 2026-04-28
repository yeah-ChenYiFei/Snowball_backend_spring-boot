package com.example.snowball.controller;
import com.example.snowball.common.Result;
import com.example.snowball.entity.ChainSegment;
import com.example.snowball.entity.StoryChain;
import com.example.snowball.repository.ChainSegmentRepository;
import com.example.snowball.repository.StoryChainRepository;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/api/v1/chains")
public class ChainController {
    private final StoryChainRepository chainRepository;
    private final ChainSegmentRepository segmentRepository;
    public ChainController(StoryChainRepository chainRepository, ChainSegmentRepository segmentRepository) {
        this.chainRepository = chainRepository;
        this.segmentRepository = segmentRepository;
    }

    @GetMapping
    public Result<List<StoryChain>> getChains() {
        return Result.success(chainRepository.findAll());
    }

    @GetMapping("/{chainId}")
    public Result<List<ChainSegment>> getChainDetail(@PathVariable Long chainId) {
        return Result.success(segmentRepository.findByChainIdOrderByCreatedAtAsc(chainId));
    }

    @PostMapping
    public Result<StoryChain> createChain(@RequestBody Map<String, String> body, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        StoryChain chain = new StoryChain();
        chain.setCreatorId(userId);
        chain.setTitle(body.get("title"));
        chainRepository.save(chain);

        // 自动创建第一段
        ChainSegment seg = new ChainSegment();
        seg.setChainId(chain.getId());
        seg.setUserId(userId);
        seg.setBody(body.get("first_segment_body"));
        segmentRepository.save(seg);

        return Result.success(chain);
    }

    @PostMapping("/{chainId}/segments")
    public Result<ChainSegment> addSegment(@PathVariable Long chainId, @RequestBody Map<String, String> body, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        ChainSegment seg = new ChainSegment();
        seg.setChainId(chainId);
        seg.setUserId(userId);
        seg.setBody(body.get("body"));
        if(body.containsKey("prev_segment_id") && !body.get("prev_segment_id").isEmpty()) {
            seg.setPrevSegmentId(Long.parseLong(body.get("prev_segment_id")));
            seg.setDepth(2); // 简化处理
        }
        return Result.success(segmentRepository.save(seg));
    }
}
