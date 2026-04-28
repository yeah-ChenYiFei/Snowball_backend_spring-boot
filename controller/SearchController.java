package com.example.snowball.controller;

import com.example.snowball.common.Result;
import com.example.snowball.entity.Post;
import com.example.snowball.repository.PostRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/search")
public class SearchController {
    private final PostRepository postRepository;

    public SearchController(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @GetMapping
    public Result<List<Post>> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String author) {

        List<String> hiddenStatuses = List.of("HIDDEN", "DELETED");

        // 1. 优先按标题模糊搜索
        if (q != null && !q.isEmpty()) {
            return Result.success(postRepository.findByTitleContainingIgnoreCaseAndStatusNotIn(q, hiddenStatuses));
        }

        // 2. 其次按类型精确过滤
        if (type != null && !type.isEmpty()) {
            return Result.success(postRepository.findByTypeAndStatusNotIn(type, hiddenStatuses));
        }

        // 3. author 过滤（由于没有建关联索引，暂时用内存过滤或留作后续优化，这里先返回空列表占位）
        if (author != null && !author.isEmpty()) {
            // 实际生产中应写自定义 SQL 或用 ElasticSearch
            return Result.success(List.of());
        }

        // 4. 什么都没传，返回最新列表
        return Result.success(postRepository.findByStatusNotIn(hiddenStatuses));
    }
}
