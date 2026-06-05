ALTER TABLE project_messages
ADD COLUMN IF NOT EXISTS user_id INT REFERENCES users(id);
