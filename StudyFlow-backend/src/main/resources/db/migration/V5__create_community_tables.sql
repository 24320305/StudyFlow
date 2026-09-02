-- Purpose: Cheng Zekai's community module: posts, comments, follows, and likes.
-- Dependencies: V4__create_check_in_foundation.sql.

CREATE TABLE post (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    checkin_id BIGINT,
    content VARCHAR(500) NOT NULL,
    visibility VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_post_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
    CONSTRAINT fk_post_checkin FOREIGN KEY (checkin_id) REFERENCES check_in(id),
    CONSTRAINT uk_post_checkin UNIQUE (checkin_id),
    CONSTRAINT ck_post_visibility CHECK (visibility IN ('PUBLIC', 'PRIVATE')),
    CONSTRAINT ck_post_status CHECK (status IN ('PENDING', 'VISIBLE', 'HIDDEN', 'DELETED'))
);

CREATE INDEX idx_post_discovery ON post(visibility, status, created_at);
CREATE INDEX idx_post_user_created ON post(user_id, created_at);

CREATE TABLE comment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    post_id BIGINT NOT NULL,
    content VARCHAR(500) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_comment_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
    CONSTRAINT fk_comment_post FOREIGN KEY (post_id) REFERENCES post(id),
    CONSTRAINT ck_comment_status CHECK (status IN ('VISIBLE', 'HIDDEN', 'DELETED'))
);

CREATE INDEX idx_comment_post_created ON comment(post_id, status, created_at);

CREATE TABLE follow (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    follower_id BIGINT NOT NULL,
    following_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_follow_follower FOREIGN KEY (follower_id) REFERENCES sys_user(id),
    CONSTRAINT fk_follow_following FOREIGN KEY (following_id) REFERENCES sys_user(id),
    CONSTRAINT uk_follow_follower_following UNIQUE (follower_id, following_id),
    CONSTRAINT ck_follow_not_self CHECK (follower_id <> following_id)
);

CREATE INDEX idx_follow_following ON follow(following_id, created_at);

CREATE TABLE post_like (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    post_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_post_like_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
    CONSTRAINT fk_post_like_post FOREIGN KEY (post_id) REFERENCES post(id),
    CONSTRAINT uk_post_like_user_post UNIQUE (user_id, post_id)
);

CREATE INDEX idx_post_like_post ON post_like(post_id);
