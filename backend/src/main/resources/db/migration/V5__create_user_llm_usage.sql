CREATE TABLE user_llm_usage (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id),
    provider_id INT REFERENCES llm_providers(id),
    date DATE,
    tokens_used INT DEFAULT 0,
    limit_reached BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW()
);
