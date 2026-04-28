package com.example.snowball.controller;

import com.example.snowball.common.Result;
import com.example.snowball.entity.Tag;
import com.example.snowball.repository.TagRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tags")
public class TagController {
    private final TagRepository tagRepository;
    public TagController(TagRepository tagRepository) { this.tagRepository = tagRepository; }

    @GetMapping
    public Result<List<Tag>> getAllTags() {
        return Result.success(tagRepository.findAll());
    }

    @PostMapping
    public Result<Tag> createTag(@RequestBody Tag tag) {
        // 简单防重
        if(tagRepository.findByName(tag.getName()).isPresent()) {
            return Result.error(400, "标签已存在");
        }
        return Result.success(tagRepository.save(tag));
    }
}
