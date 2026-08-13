CREATE TABLE IF NOT EXISTS users (
    id CHAR(36) NOT NULL PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    user_type VARCHAR(32) NOT NULL,
    enabled BOOLEAN NOT NULL,
    last_active_at TIMESTAMP(6) NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL
);

CREATE TABLE IF NOT EXISTS follows (
    id CHAR(36) NOT NULL PRIMARY KEY,
    follower_id CHAR(36) NOT NULL,
    followee_id CHAR(36) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT uq_follows_pair UNIQUE (follower_id, followee_id),
    CONSTRAINT fk_follows_follower FOREIGN KEY (follower_id) REFERENCES users (id),
    CONSTRAINT fk_follows_followee FOREIGN KEY (followee_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS tweets (
    id CHAR(36) NOT NULL PRIMARY KEY,
    author_id CHAR(36) NOT NULL,
    content VARCHAR(4000) NOT NULL,
    idempotency_key VARCHAR(128) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT uq_tweets_author_idempotency UNIQUE (author_id, idempotency_key),
    CONSTRAINT fk_tweets_author FOREIGN KEY (author_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS chats (
    id CHAR(36) NOT NULL PRIMARY KEY,
    participant_pair_key VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP(6) NOT NULL
);

CREATE TABLE IF NOT EXISTS chat_participants (
    chat_id CHAR(36) NOT NULL,
    participant_id CHAR(36) NOT NULL,
    PRIMARY KEY (chat_id, participant_id),
    CONSTRAINT fk_chat_participants_chat FOREIGN KEY (chat_id) REFERENCES chats (id),
    CONSTRAINT fk_chat_participants_user FOREIGN KEY (participant_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS messages (
    id CHAR(36) NOT NULL PRIMARY KEY,
    chat_id CHAR(36) NOT NULL,
    sender_id CHAR(36) NOT NULL,
    content VARCHAR(4000) NOT NULL,
    idempotency_key VARCHAR(128) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT uq_messages_sender_idempotency UNIQUE (chat_id, sender_id, idempotency_key),
    CONSTRAINT fk_messages_chat FOREIGN KEY (chat_id) REFERENCES chats (id),
    CONSTRAINT fk_messages_sender FOREIGN KEY (sender_id) REFERENCES users (id)
);

SET @index_exists = (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'follows'
      AND index_name = 'ix_follows_followee'
);
SET @index_sql = IF(
    @index_exists = 0,
    'CREATE INDEX ix_follows_followee ON follows (followee_id)',
    'SELECT 1'
);
PREPARE index_statement FROM @index_sql;
EXECUTE index_statement;
DEALLOCATE PREPARE index_statement;

SET @index_exists = (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'tweets'
      AND index_name = 'ix_tweets_author_created'
);
SET @index_sql = IF(
    @index_exists = 0,
    'CREATE INDEX ix_tweets_author_created ON tweets (author_id, created_at DESC)',
    'SELECT 1'
);
PREPARE index_statement FROM @index_sql;
EXECUTE index_statement;
DEALLOCATE PREPARE index_statement;

SET @index_exists = (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'messages'
      AND index_name = 'ix_messages_chat_created'
);
SET @index_sql = IF(
    @index_exists = 0,
    'CREATE INDEX ix_messages_chat_created ON messages (chat_id, created_at)',
    'SELECT 1'
);
PREPARE index_statement FROM @index_sql;
EXECUTE index_statement;
DEALLOCATE PREPARE index_statement;