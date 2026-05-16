package com.snowball.controller;

import com.snowball.common.Result;
import com.snowball.dto.TagCreateDTO;
import com.snowball.service.TagService;
import com.snowball.vo.TagVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tags")
public class TagController extends BaseController { // ✅ 继承基类

    private final TagService tagService; // ✅ 只找 Service

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping
    public Result<List<TagVO>> getAllTags() {
        return Result.success(tagService.getAllTags());
    }

    @PostMapping
    public Result<TagVO> createTag(@RequestBody TagCreateDTO dto) { // ✅ 用 DTO 接收
        return Result.success(tagService.createTag(dto));
    }
}
