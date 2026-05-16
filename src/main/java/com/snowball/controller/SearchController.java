package com.snowball.controller;

import com.snowball.common.Result;
import com.snowball.service.PostService;
import com.snowball.vo.PostDetailVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/search")
public class SearchController {

    private final PostService postService; // ✅ 只留 PostService，删掉 PostRepository

    public SearchController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public Result<List<PostDetailVO>> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String type) {
        // ✅ 之前那一大坨 if-else 全删了，一行搞定
        return Result.success(postService.searchPosts(q, type));
    }
}
