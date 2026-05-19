-- Migration: 大世界 + 文阁 + 绑定功能
ALTER TABLE articles ADD COLUMN is_published BOOLEAN DEFAULT FALSE;
ALTER TABLE articles ADD COLUMN published_at DATETIME;
ALTER TABLE articles ADD COLUMN world_id BIGINT;

-- 评论系统通用化
ALTER TABLE comments ADD COLUMN source_type VARCHAR(20) DEFAULT 'POST';
ALTER TABLE comments ADD COLUMN source_id BIGINT;
ALTER TABLE comments MODIFY COLUMN post_id BIGINT NULL;

CREATE TABLE IF NOT EXISTS join_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    world_id BIGINT NOT NULL,
    applicant_id BIGINT NOT NULL,
    reason TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (world_id) REFERENCES worlds(id) ON DELETE CASCADE,
    FOREIGN KEY (applicant_id) REFERENCES users(id) ON DELETE CASCADE
);
