INSERT INTO llm_providers (name, category, priority, daily_free_tokens_limit, active) VALUES
-- FRONTEND
('Llama-3-70B-Instruct', 'FRONTEND', 1, 50000, true),
('Mistral-7B-Instruct', 'FRONTEND', 2, 30000, true),

-- BACKEND
('Llama-3-70B-Code', 'BACKEND', 1, 50000, true),
('CodeGemma-7B', 'BACKEND', 2, 30000, true),

-- DB
('Llama-3-8B-SQL', 'DB', 1, 20000, true),
('SQLCoder-7B', 'DB', 2, 15000, true),

-- CLOUD
('Llama-3-70B-DevOps', 'CLOUD', 1, 40000, true),
('Mistral-7B-DevOps', 'CLOUD', 2, 25000, true),

-- GENERAL fallback
('Llama-3-8B', 'GENERAL', 99, 20000, true),
('Mistral-7B', 'GENERAL', 100, 20000, true);
