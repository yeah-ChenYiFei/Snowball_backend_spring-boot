package com.snowball.controller;

import com.snowball.common.Result;
import com.snowball.dto.ChainCreateDTO;
import com.snowball.dto.ChainSegmentCreateDTO;
import com.snowball.service.ChainService;
import com.snowball.vo.ChainDetailVO;
import com.snowball.vo.ChainSegmentVO;
import com.snowball.vo.ChainVO;
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
    public Result<ChainDetailVO> getChainDetail(@PathVariable Long chainId) {
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
