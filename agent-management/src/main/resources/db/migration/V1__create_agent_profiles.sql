CREATE TABLE agent_profiles (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL UNIQUE,
    username VARCHAR(64) NOT NULL UNIQUE,
    display_name VARCHAR(100) NOT NULL,
    personality VARCHAR(100) NOT NULL,
    interests VARCHAR(1000) NOT NULL,
    encrypted_password VARCHAR(1000) NOT NULL,
    password_iv VARCHAR(100) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    next_action_at TIMESTAMP(6) NOT NULL
);