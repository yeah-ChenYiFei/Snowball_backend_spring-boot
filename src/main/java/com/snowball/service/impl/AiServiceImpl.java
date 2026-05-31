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
    private final StoryChainRepository storyChainRepository;
    private final ChainSegmentRepository chainSegmentRepository;
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
                         WorldEntryRepository worldEntryRepository,
                         StoryChainRepository storyChainRepository,
                         ChainSegmentRepository chainSegmentRepository) {
        this.articleRepository = articleRepository;
        this.novelRepository = novelRepository;
        this.novelChapterRepository = novelChapterRepository;
        this.worldRepository = worldRepository;
        this.worldEntryRepository = worldEntryRepository;
        this.storyChainRepository = storyChainRepository;
        this.chainSegmentRepository = chainSegmentRepository;
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

        String userContent = contextBuilder.toString();

        return callDeepSeek(systemPrompt, userContent);
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

        String userContent = contextBuilder.toString();
        // If user provided a prompt, append it
        String prompt = request.get("prompt") instanceof String ? (String) request.get("prompt") : "";
        if (!prompt.isBlank()) {
            userContent += "\n\n【用户对续写的特别要求】" + prompt;
        }

        return callDeepSeek(systemPrompt, userContent);
    }

    @Override
    public AiContinueResponse continueChain(Long chainId, String prompt, Long userId) {
        StoryChain chain = storyChainRepository.findById(chainId)
                .orElseThrow(() -> new BusinessException(404, "接龙不存在"));

        List<ChainSegment> segments = chainSegmentRepository.findByChainIdOrderByCreatedAtAsc(chainId);

        StringBuilder ctx = new StringBuilder();
        ctx.append("【接龙标题】").append(chain.getTitle()).append("\n\n");
        ctx.append("【已有的接龙内容】\n");
        for (int i = 0; i < segments.size(); i++) {
            ChainSegment seg = segments.get(i);
            String body = seg.getBody();
            if (body != null && body.length() > 1000) body = body.substring(body.length() - 1000);
            ctx.append("第").append(i + 1).append("段：").append(body != null ? body : "").append("\n\n");
        }

        String userContent = ctx.toString();
        if (prompt != null && !prompt.isBlank()) {
            userContent += "\n【续写提示】" + prompt;
        }

        String system = "你是一位创意写作者，正在参与一个故事接龙。请根据已有内容进行合理续写。\n"
                + "要求：1.自然衔接上文保持风格一致 2.输出纯正文不要包含引导语 3.续写约200-500字\n"
                + "4.如有续写提示请按提示的方向、人物、文风进行创作";

        return callDeepSeek(system, userContent);
    }

    @Override
    public AiContinueResponse generateWorldStory(Long worldId, List<Long> entryIds, String prompt, Long userId) {
        World world = worldRepository.findById(worldId)
                .orElseThrow(() -> new BusinessException(404, "世界不存在"));

        StringBuilder ctx = new StringBuilder();
        ctx.append("【世界观】").append(world.getName()).append("\n");
        if (world.getDescription() != null && !world.getDescription().isBlank()) {
            ctx.append(world.getDescription()).append("\n");
        }
        ctx.append("\n");

        List<WorldEntry> selected = new ArrayList<>();
        if (entryIds != null && !entryIds.isEmpty()) {
            ctx.append("【选中的设定条目】\n");
            for (Long eid : entryIds) {
                worldEntryRepository.findById(eid).ifPresent(e -> {
                    ctx.append("- ").append(e.getName());
                    if (e.getType() != null) ctx.append("（").append(e.getType()).append("）");
                    ctx.append(": ");
                    String c = e.getContent();
                    if (c != null) { if (c.length() > 800) c = c.substring(0, 800) + "..."; ctx.append(c); }
                    ctx.append("\n");
                });
            }
        } else {
            selected = worldEntryRepository.findByWorldIdOrderByCreatedAtDesc(worldId);
            if (!selected.isEmpty()) {
                ctx.append("【世界的设定条目】\n");
                for (WorldEntry e : selected) {
                    ctx.append("- ").append(e.getName());
                    if (e.getType() != null) ctx.append("（").append(e.getType()).append("）");
                    String c = e.getContent();
                    if (c != null) { if (c.length() > 500) c = c.substring(0, 500) + "..."; ctx.append(": ").append(c); }
                    ctx.append("\n");
                }
            }
        }

        ctx.append("\n");
        if (prompt != null && !prompt.isBlank()) {
            ctx.append("【故事要求】").append(prompt);
        } else {
            ctx.append("【故事要求】请根据以上世界观设定创作一个有趣的故事片段。");
        }

        String system = "你是一位富有创意的小说作者。根据世界观设定创作引人入胜的故事。\n"
                + "要求：1.严格遵循设定 2.故事有趣有冲突有情感 3.输出纯正文不要引导语 4.约500-1500字";

        AiContinueResponse response = callDeepSeek(system, ctx.toString());

        // Save as an Article so the user can revisit it later
        Article article = new Article();
        article.setUserId(userId);
        article.setType(Article.ArticleType.ESSAY);
        article.setTitle(world.getName() + " - AI故事");
        article.setBody(response.getContinuation());
        article.setWorldId(worldId);
        article = articleRepository.save(article);
        response.setArticleId(article.getId());

        return response;
    }

    private AiContinueResponse callDeepSeek(String systemPrompt, String userPrompt) {
        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("model", model);
            body.put("messages", List.of(
                    Map.of("role", "system", "content", systemPrompt),
                    Map.of("role", "user", "content", userPrompt)
            ));
            body.put("max_tokens", 2048);
            body.put("temperature", 0.8);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> resp = restTemplate.exchange(
                    apiUrl + "/v1/chat/completions", HttpMethod.POST, entity, String.class);

            JsonNode root = objectMapper.readTree(resp.getBody());
            String text = root.path("choices").get(0).path("message").path("content").asText();
            int tokens = root.path("usage").path("total_tokens").asInt();
            return new AiContinueResponse(text, model, tokens);
        } catch (Exception e) {
            throw new BusinessException(500, "AI请求失败: " + e.getMessage());
        }
    }
}
