CREATE TABLE project_artifact_files (
    id SERIAL PRIMARY KEY,
    project_id INT REFERENCES projects(id),
    file_path VARCHAR(1024),
    content TEXT,
    created_at TIMESTAMP DEFAULT NOW()
);
