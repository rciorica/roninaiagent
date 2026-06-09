CREATE TABLE ranks (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    level INT NOT NULL,
    min_projects INT NOT NULL,
    max_projects INT NOT NULL,
    belt_color VARCHAR(20)
);

CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    display_name VARCHAR(255),
    password_hash VARCHAR(255),
    completed_projects INT DEFAULT 0,
    rank_id INT REFERENCES ranks(id),
    created_at TIMESTAMP DEFAULT NOW()
);
