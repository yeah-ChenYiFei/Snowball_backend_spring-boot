package com.snowball.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.snowball.common.BusinessException;
import com.snowball.dto.AiContinueResponse;
import com.snowball.entity.*;
import com.snowball.repository.*;
import com.snowball.service.AiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AiServiceImpl implements AiService {

    private final ArticleRepository articleRepository;
    private final NovelRepository novelRepository;
    private final NovelChapterRepository novelChapterRepository;
    private final WorldRepository worldRepository;
    private final WorldEntryRepository worldEntryRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${deepseek.api.key:}")
    private String apiKey;

    @Value("${deepseek.api.url:https://api.deepseek.com}")
    private String apiUrl;

    @Value("${deepseek.api.model:deepseek-v4-pro}")
    private String model;

    public AiServiceImpl(ArticleRepository articleRepository,
                         NovelRepository novelRepository,
                         NovelChapterRepository novelChapterRepository,
                         WorldRepository worldRepository,
                         WorldEntryRepository worldEntryRepository) {
        this.articleRepository = articleRepository;
        this.novelRepository = novelRepository;
        this.novelChapterRepository = novelChapterRepository;
        this.worldRepository = worldRepository;
        this.worldEntryRepository = worldEntryRepository;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(30000);
        factory.setReadTimeout(120000);
        this.restTemplate = new RestTemplate(factory);
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public AiContinueResponse continueNovel(Long articleId, Long userId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new BusinessException(404, "文章不存在"));

        if (!article.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作此文章");
        }

        if (article.getType() != Article.ArticleType.NOVEL) {
            throw new BusinessException(400, "仅小说类型支持AI续写");
        }

        // Gather context
        StringBuilder contextBuilder = new StringBuilder();

        // 1. World context
        if (article.getWorldId() != null) {
            World world = worldRepository.findById(article.getWorldId()).orElse(null);
            if (world != null) {
                contextBuilder.append("【世界观】\n");
                contextBuilder.append("名称：").append(world.getName()).append("\n");
                if (world.getDescription() != null && !world.getDescription().isBlank()) {
                    contextBuilder.append("描述：").append(world.getDescription()).append("\n");
                }

                List<WorldEntry> entries = worldEntryRepository.findByWorldIdOrderByCreatedAtDesc(article.getWorldId());
                if (!entries.isEmpty()) {
                    contextBuilder.append("\n世界设定条目：\n");
                    for (WorldEntry e : entries) {
                        contextBuilder.append("- ").append(e.getName());
                        if (e.getType() != null) contextBuilder.append("（").append(e.getType()).append("）");
                        contextBuilder.append(": ");
                        String content = e.getContent();
                        if (content.length() > 500) content = content.substring(0, 500) + "...";
                        contextBuilder.append(content).append("\n");
                    }
                }
                contextBuilder.append("\n");
            }
        }

        // 2. Existing chapters
        List<Article> chapters = articleRepository.findByUserIdAndTypeAndStatusNotOrderByCreatedAtDesc(
                article.getUserId(), Article.ArticleType.NOVEL, "DELETED");
        chapters = chapters.stream()
                .filter(a -> a.getTitle().equals(article.getTitle()))
                .collect(Collectors.toList());

        contextBuilder.append("【已有章节】（按创建顺序）\n");
        for (Article ch : chapters) {
            if (ch.getChapter() != null && ch.getChapter().startsWith("$$cfg")) continue; // skip config
            String label = ch.getChapter() != null ? ch.getChapter() : "未标记章节";
            contextBuilder.append("--- ").append(label).append(" ---\n");
            String body = ch.getBody();
            if (body != null && body.length() > 1500) body = body.substring(body.length() - 1500);
            if (body != null) contextBuilder.append(body).append("\n\n");
        }

        // 3. Current chapter content (the most recent written content)
        String currentContent = article.getBody();
        contextBuilder.append("【当前正在写作的章节内容】\n");
        if (currentContent != null && !currentContent.isBlank()) {
            contextBuilder.append(currentContent);
        } else {
            contextBuilder.append("（新章节，尚未写入内容）");
        }

        // Build the system + user prompt
        String systemPrompt = "你是一位专业的小说续写助手。根据提供的世界观设定和已有章节内容，为当前正在写作的章节进行合理且富有文采的续写。\n"
                + "要求：\n"
                + "1. 续写内容应自然衔接已有内容，保持风格一致\n"
                + "2. 尊重世界设定，不违背已有的人物关系和背景\n"
                + "3. 输出纯正文内容，不要包含\"续写如下\"等引导语\n"
                + "4. 续写长度约500-1000字\n"
                + "5. 如果当前章节为空，则根据上下文写出开篇";

        String userPrompt = contextBuilder.toString();

        // Call DeepSeek API
        try {
            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", List.of(
                    Map.of("role", "system", "content", systemPrompt),
                    Map.of("role", "user", "content", userPrompt)
            ));
            requestBody.put("max_tokens", 2048);
            requestBody.put("temperature", 0.8);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl + "/v1/chat/completions",
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            JsonNode root = objectMapper.readTree(response.getBody());
            String continuation = root.path("choices").get(0)
                    .path("message").path("content").asText();
            int tokensUsed = root.path("usage").path("total_tokens").asInt();

            return new AiContinueResponse(continuation, model, tokensUsed);
        } catch (Exception e) {
            throw new BusinessException(500, "AI续写请求失败: " + e.getMessage());
        }
    }

    @Override
    public AiContinueResponse continueNovel(Map<String, Object> request, Long userId) {
        Object novelIdObj = request.get("novelId");
        if (!(novelIdObj instanceof Number)) {
            throw new BusinessException(400, "缺少novelId");
        }
        Long novelId = ((Number) novelIdObj).longValue();

        Novel novel = novelRepository.findById(novelId)
                .orElseThrow(() -> new BusinessException(404, "小说不存在"));
        if (!novel.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作此小说");
        }

        String currentBody = request.get("currentBody") instanceof String
                ? (String) request.get("currentBody") : "";

        StringBuilder contextBuilder = new StringBuilder();

        // 1. Novel description
        if (novel.getDescription() != null && !novel.getDescription().isBlank()) {
            contextBuilder.append("【小说简介】\n").append(novel.getDescription()).append("\n\n");
        }

        // 2. World context
        if (novel.getWorldId() != null) {
            World world = worldRepository.findById(novel.getWorldId()).orElse(null);
            if (world != null) {
                contextBuilder.append("【世界观】\n");
                contextBuilder.append("名称：").append(world.getName()).append("\n");
                if (world.getDescription() != null && !world.getDescription().isBlank()) {
                    contextBuilder.append("描述：").append(world.getDescription()).append("\n");
                }
                List<WorldEntry> entries = worldEntryRepository.findByWorldIdOrderByCreatedAtDesc(novel.getWorldId());
                if (!entries.isEmpty()) {
                    contextBuilder.append("\n世界设定条目：\n");
                    for (WorldEntry e : entries) {
                        contextBuilder.append("- ").append(e.getName());
                        if (e.getType() != null) contextBuilder.append("（").append(e.getType()).append("）");
                        contextBuilder.append(": ");
                        String content = e.getContent();
                        if (content.length() > 500) content = content.substring(0, 500) + "...";
                        contextBuilder.append(content).append("\n");
                    }
                }
                contextBuilder.append("\n");
            }
        }

        // 3. Existing chapters
        List<NovelChapter> chapters = novelChapterRepository
                .findByNovelIdOrderBySectionAscVolumeNumberAscChapterNumberAsc(novelId);
        contextBuilder.append("【已有章节】（按顺序）\n");
        for (NovelChapter ch : chapters) {
            String label = ch.getSection() + " ";
            if (novel.getHasVolumes() != null && novel.getHasVolumes() && ch.getVolumeNumber() > 0) {
                label += "第" + ch.getVolumeNumber() + "卷 ";
            }
            label += "第" + ch.getChapterNumber() + "章";
            if (ch.getTitle() != null && !ch.getTitle().isBlank()) {
                label += " " + ch.getTitle();
            }
            contextBuilder.append("--- ").append(label).append(" ---\n");
            String body = ch.getBody();
            if (body != null && body.length() > 1500) body = body.substring(body.length() - 1500);
            if (body != null) contextBuilder.append(body).append("\n\n");
        }

        // 4. Current chapter content
        contextBuilder.append("【当前正在写作的章节内容】\n");
        if (currentBody != null && !currentBody.isBlank()) {
            contextBuilder.append(currentBody);
        } else {
            contextBuilder.append("（新章节，尚未写入内容）");
        }

        String systemPrompt = "你是一位专业的小说续写助手。根据提供的世界观设定和已有章节内容，为当前正在写作的章节进行合理且富有文采的续写。\n"
                + "要求：\n"
                + "1. 续写内容应自然衔接已有内容，保持风格一致\n"
                + "2. 尊重世界设定，不违背已有的人物关系和背景\n"
                + "3. 输出纯正文内容，不要包含\"续写如下\"等引导语\n"
                + "4. 续写长度约500-1000字\n"
                + "5. 如果当前章节为空，则根据上下文写出开篇";

        String userPrompt = contextBuilder.toString();

        try {
            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", List.of(
                    Map.of("role", "system", "content", systemPrompt),
                    Map.of("role", "user", "content", userPrompt)
            ));
            requestBody.put("max_tokens", 2048);
            requestBody.put("temperature", 0.8);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl + "/v1/chat/completions",
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            JsonNode root = objectMapper.readTree(response.getBody());
            String continuation = root.path("choices").get(0)
                    .path("message").path("content").asText();
            int tokensUsed = root.path("usage").path("total_tokens").asInt();

            return new AiContinueResponse(continuation, model, tokensUsed);
        } catch (Exception e) {
            throw new BusinessException(500, "AI续写请求失败: " + e.getMessage());
        }
    }
}
