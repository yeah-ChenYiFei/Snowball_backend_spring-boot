-- ============================================================
-- Novel system migration: convert Article-based novels to
-- Novel + NovelChapter entities
-- ============================================================

-- Step 1: Insert novels from grouped NOVEL articles
INSERT INTO novels (user_id, title, description, has_volumes, world_id, is_published, published_at, status, created_at, updated_at)
SELECT
    a.user_id,
    a.title,
    '' AS description,
    COALESCE(
        (SELECT CASE WHEN MAX(CASE WHEN a2.chapter LIKE '$$cfg:hasVolumes=1%' THEN 1 ELSE 0 END) = 1 THEN TRUE ELSE FALSE END
         FROM articles a2
         WHERE a2.type = 'NOVEL'
           AND a2.status <> 'DELETED'
           AND a2.title = a.title
           AND a2.user_id = a.user_id),
        FALSE
    ) AS has_volumes,
    (SELECT MAX(a3.world_id)
     FROM articles a3
     WHERE a3.type = 'NOVEL'
       AND a3.status <> 'DELETED'
       AND a3.title = a.title
       AND a3.user_id = a.user_id
       AND a3.world_id IS NOT NULL
     LIMIT 1) AS world_id,
    MAX(a.is_published) AS is_published,
    MAX(a.published_at) AS published_at,
    'PUBLISHED' AS status,
    MIN(a.created_at) AS created_at,
    MAX(a.updated_at) AS updated_at
FROM articles a
WHERE a.type = 'NOVEL'
  AND a.status <> 'DELETED'
  AND (a.chapter IS NULL OR a.chapter NOT LIKE '$$cfg:%')
GROUP BY a.user_id, a.title;

-- Step 2: Insert novel chapters from non-config NOVEL articles
INSERT INTO novel_chapters (novel_id, section, volume_number, chapter_number, title, body, word_count, created_at, updated_at)
SELECT
    n.id AS novel_id,
    COALESCE(
        SUBSTRING_INDEX(SUBSTRING_INDEX(a.chapter, ':', 1), '||', 1),
        'main'
    ) AS section,
    COALESCE(
        CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(a.chapter, ':', 2), ':', -1) AS SIGNED),
        0
    ) AS volume_number,
    COALESCE(
        CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(SUBSTRING_INDEX(a.chapter, ':', 3), ':', -1), '||', 1) AS SIGNED),
        1
    ) AS chapter_number,
    COALESCE(
        SUBSTRING_INDEX(a.chapter, '||', -1),
        a.chapter
    ) AS title,
    a.body,
    COALESCE(LENGTH(a.body), 0) AS word_count,
    a.created_at,
    a.updated_at
FROM articles a
JOIN novels n ON n.title = a.title AND n.user_id = a.user_id
WHERE a.type = 'NOVEL'
  AND a.status <> 'DELETED'
  AND a.chapter IS NOT NULL
  AND a.chapter != ''
  AND a.chapter NOT LIKE '$$cfg:%';

-- Step 3: Mark old NOVEL articles as migrated
UPDATE articles SET status = 'MIGRATED'
WHERE type = 'NOVEL' AND status <> 'DELETED';

-- Verify (run after migration):
-- SELECT COUNT(*) AS novel_count FROM novels;
-- SELECT COUNT(*) AS chapter_count FROM novel_chapters;
-- SELECT COUNT(*) AS old_novel_articles FROM articles WHERE type = 'NOVEL' AND status = 'MIGRATED';
