CREATE TABLE generated_images (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    prompt TEXT NOT NULL,
    revised_prompt TEXT,
    image_url TEXT NOT NULL,
    provider VARCHAR(255),
    model VARCHAR(255),
    size VARCHAR(50),
    format VARCHAR(50),
    generation_time_ms BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_generated_images_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT fk_generated_images_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_generated_images_project_id ON generated_images(project_id);
CREATE INDEX idx_generated_images_user_id ON generated_images(user_id);
CREATE INDEX idx_generated_images_created_at ON generated_images(created_at);
