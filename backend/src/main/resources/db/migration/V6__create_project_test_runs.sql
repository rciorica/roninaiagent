CREATE TABLE project_test_runs (
    id SERIAL PRIMARY KEY,
    project_id INT REFERENCES projects(id),
    status VARCHAR(50),
    logs TEXT,
    created_at TIMESTAMP DEFAULT NOW()
);
