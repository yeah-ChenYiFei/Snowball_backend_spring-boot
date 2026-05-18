package com.snowball.controller;

import com.snowball.common.Result;
import com.snowball.dto.InspirationCreateDTO;
import com.snowball.dto.InspirationUpdateDTO;
import com.snowball.service.InspirationService;
import com.snowball.vo.InspirationVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inspirations")
public class InspirationController extends BaseController {

    private final InspirationService inspirationService;

    public InspirationController(InspirationService inspirationService) {
        this.inspirationService = inspirationService;
    }

    @GetMapping
    public Result<List<InspirationVO>> getMyInspirations() {
        return Result.success(inspirationService.getMyInspirations(getCurrentUserId()));
    }

    @PostMapping
    public Result<InspirationVO> addInspiration(@Valid @RequestBody InspirationCreateDTO dto) {
        return Result.success(inspirationService.addInspiration(getCurrentUserId(), dto));
    }

    @PutMapping("/{id}")
    public Result<InspirationVO> updateInspiration(@PathVariable Long id, @Valid @RequestBody InspirationUpdateDTO dto) {
        return Result.success(inspirationService.updateInspiration(id, getCurrentUserId(), dto));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteInspiration(@PathVariable Long id) {
        inspirationService.deleteInspiration(id, getCurrentUserId());
        return Result.success();
    }
}
