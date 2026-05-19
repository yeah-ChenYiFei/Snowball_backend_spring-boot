-- Migration: Public Chain Enhancement
-- Run against the snowball database

ALTER TABLE story_chains ADD COLUMN description TEXT AFTER title;
ALTER TABLE story_chains ADD COLUMN deadline DATETIME AFTER status;
ALTER TABLE chain_segments ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'APPROVED' AFTER body;

CREATE TABLE IF NOT EXISTS segment_comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    segment_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    body TEXT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (segment_id) REFERENCES chain_segments(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
