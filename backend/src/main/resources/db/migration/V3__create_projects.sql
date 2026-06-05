CREATE TABLE projects (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id),
    name VARCHAR(255),
    description TEXT,
    phase VARCHAR(50),
    status VARCHAR(50),
    repo_url TEXT,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE project_messages (
    id SERIAL PRIMARY KEY,
    project_id INT REFERENCES projects(id),
    sender VARCHAR(20),
    message TEXT,
    model_used VARCHAR(100),
    created_at TIMESTAMP DEFAULT NOW()
);
