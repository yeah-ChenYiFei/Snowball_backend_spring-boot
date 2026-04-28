package com.example.snowball.controller;

import com.example.snowball.common.Result;
import com.example.snowball.entity.Post;
import com.example.snowball.repository.PostRepository;
import com.example.snowball.service.PostService; // ✅ 引入 Service
import com.example.snowball.vo.PostDetailVO;     // ✅ 引入 VO
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/search")
public class SearchController {

    private final PostRepository postRepository;
    private final PostService postService; // ✅ 声明 Service

    // ✅ 构造函数注入
    public SearchController(PostRepository postRepository, PostService postService) {
        this.postRepository = postRepository;
        this.postService = postService;
    }

    // ✅ 返回值改成 List<PostDetailVO>
    @GetMapping
    public Result<List<PostDetailVO>> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String author) {

        List<String> hiddenStatuses = List.of("HIDDEN", "DELETED");
        List<Post> posts;

        // 1. 优先按标题模糊搜索
        if (q != null && !q.isEmpty()) {
            posts = postRepository.findByTitleContainingIgnoreCaseAndStatusNotIn(q, hiddenStatuses);
        }
        // 2. 其次按类型精确过滤
        else if (type != null && !type.isEmpty()) {
            posts = postRepository.findByTypeAndStatusNotIn(type, hiddenStatuses);
        }
        // 3. author 过滤（占位）
        else if (author != null && !author.isEmpty()) {
            return Result.success(List.of());
        }
        // 4. 什么都没传，返回最新列表
        else {
            posts = postRepository.findByStatusNotIn(hiddenStatuses);
        }

        // ✅ 核心：统一将 Post 实体转换为带 authorName 等聚合数据的 VO
        List<PostDetailVO> voList = posts.stream()
                .map(post -> postService.convertToVO(post))
                .toList();

        return Result.success(voList);
    }
}
