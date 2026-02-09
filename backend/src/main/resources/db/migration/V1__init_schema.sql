-- ═══════════════════════════════════════════════════════════════
-- Reddit Clone — Initial Schema Migration
-- ═══════════════════════════════════════════════════════════════

-- ─── Users & Roles ──────────────────────────────────────────
CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    username        VARCHAR(30) NOT NULL UNIQUE,
    email           VARCHAR(100) NOT NULL UNIQUE,
    password        VARCHAR(255) NOT NULL,
    display_name    VARCHAR(50),
    bio             TEXT,
    avatar_url      VARCHAR(500),
    banner_url      VARCHAR(500),
    karma           INTEGER DEFAULT 0,
    enabled         BOOLEAN DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE roles (
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(20) NOT NULL UNIQUE
);

INSERT INTO roles (name) VALUES ('ROLE_USER'), ('ROLE_MODERATOR'), ('ROLE_ADMIN');

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE refresh_tokens (
    id          BIGSERIAL PRIMARY KEY,
    token       VARCHAR(500) NOT NULL UNIQUE,
    user_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    expiry_date TIMESTAMP NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ─── Subreddits ─────────────────────────────────────────────
CREATE TABLE subreddits (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(50) NOT NULL UNIQUE,
    description     TEXT,
    banner_url      VARCHAR(500),
    icon_url        VARCHAR(500),
    member_count    INTEGER DEFAULT 0,
    creator_id      BIGINT NOT NULL REFERENCES users(id),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE subreddit_memberships (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    subreddit_id    BIGINT NOT NULL REFERENCES subreddits(id) ON DELETE CASCADE,
    role            VARCHAR(20) DEFAULT 'MEMBER',
    joined_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, subreddit_id)
);

-- ─── Posts ───────────────────────────────────────────────────
CREATE TABLE posts (
    id              BIGSERIAL PRIMARY KEY,
    title           VARCHAR(300) NOT NULL,
    content         TEXT,
    url             VARCHAR(1000),
    image_url       VARCHAR(500),
    post_type       VARCHAR(10) NOT NULL DEFAULT 'TEXT',
    vote_count      INTEGER DEFAULT 0,
    comment_count   INTEGER DEFAULT 0,
    is_locked       BOOLEAN DEFAULT FALSE,
    is_deleted      BOOLEAN DEFAULT FALSE,
    subreddit_id    BIGINT NOT NULL REFERENCES subreddits(id) ON DELETE CASCADE,
    author_id       BIGINT NOT NULL REFERENCES users(id),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ─── Comments ───────────────────────────────────────────────
CREATE TABLE comments (
    id              BIGSERIAL PRIMARY KEY,
    content         TEXT NOT NULL,
    vote_count      INTEGER DEFAULT 0,
    is_deleted      BOOLEAN DEFAULT FALSE,
    post_id         BIGINT NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    author_id       BIGINT NOT NULL REFERENCES users(id),
    parent_id       BIGINT REFERENCES comments(id) ON DELETE CASCADE,
    depth           INTEGER DEFAULT 0,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ─── Votes ──────────────────────────────────────────────────
CREATE TABLE votes (
    id              BIGSERIAL PRIMARY KEY,
    vote_type       SMALLINT NOT NULL, -- 1 = upvote, -1 = downvote
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    post_id         BIGINT REFERENCES posts(id) ON DELETE CASCADE,
    comment_id      BIGINT REFERENCES comments(id) ON DELETE CASCADE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT unique_post_vote UNIQUE (user_id, post_id),
    CONSTRAINT unique_comment_vote UNIQUE (user_id, comment_id),
    CONSTRAINT vote_target_check CHECK (
        (post_id IS NOT NULL AND comment_id IS NULL) OR
        (post_id IS NULL AND comment_id IS NOT NULL)
    )
);

-- ─── Notifications ──────────────────────────────────────────
CREATE TABLE notifications (
    id              BIGSERIAL PRIMARY KEY,
    type            VARCHAR(30) NOT NULL,
    message         TEXT NOT NULL,
    entity_id       BIGINT,
    entity_type     VARCHAR(20),
    is_read         BOOLEAN DEFAULT FALSE,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    triggered_by_id BIGINT REFERENCES users(id),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ─── Indexes ────────────────────────────────────────────────
CREATE INDEX idx_posts_subreddit ON posts(subreddit_id);
CREATE INDEX idx_posts_author ON posts(author_id);
CREATE INDEX idx_posts_created_at ON posts(created_at DESC);
CREATE INDEX idx_posts_vote_count ON posts(vote_count DESC);

CREATE INDEX idx_comments_post ON comments(post_id);
CREATE INDEX idx_comments_parent ON comments(parent_id);
CREATE INDEX idx_comments_author ON comments(author_id);

CREATE INDEX idx_votes_post ON votes(post_id);
CREATE INDEX idx_votes_comment ON votes(comment_id);
CREATE INDEX idx_votes_user ON votes(user_id);

CREATE INDEX idx_subreddit_memberships_user ON subreddit_memberships(user_id);
CREATE INDEX idx_subreddit_memberships_subreddit ON subreddit_memberships(subreddit_id);

CREATE INDEX idx_notifications_user ON notifications(user_id);
CREATE INDEX idx_notifications_read ON notifications(user_id, is_read);

CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);
