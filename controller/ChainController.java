package com.example.snowball.controller;

import com.example.snowball.common.Result;
import com.example.snowball.dto.ChainCreateDTO;
import com.example.snowball.dto.ChainSegmentCreateDTO;
import com.example.snowball.service.ChainService;
import com.example.snowball.vo.ChainSegmentVO;
import com.example.snowball.vo.ChainVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chains")
public class ChainController extends BaseController {

    private final ChainService chainService;

    public ChainController(ChainService chainService) {
        this.chainService = chainService;
    }

    @GetMapping
    public Result<List<ChainVO>> getChains() {
        return Result.success(chainService.getAllChains());
    }

    @GetMapping("/{chainId}")
    public Result<List<ChainSegmentVO>> getChainDetail(@PathVariable Long chainId) {
        return Result.success(chainService.getChainDetail(chainId));
    }

    @PostMapping
    public Result<ChainVO> createChain(@RequestBody ChainCreateDTO dto) {
        return Result.success(chainService.createChain(getCurrentUserId(), dto));
    }

    @PostMapping("/{chainId}/segments")
    public Result<ChainSegmentVO> addSegment(@PathVariable Long chainId, @RequestBody ChainSegmentCreateDTO dto) {
        return Result.success(chainService.addSegment(chainId, getCurrentUserId(), dto));
    }
}
