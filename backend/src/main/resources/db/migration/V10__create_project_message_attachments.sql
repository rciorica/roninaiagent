CREATE TABLE project_message_attachments (
    id BIGSERIAL PRIMARY KEY,
    message_id BIGINT NOT NULL,
    file_name VARCHAR(255),
    content_type VARCHAR(255),
    content BYTEA,
    CONSTRAINT fk_project_message_attachment_message FOREIGN KEY (message_id) REFERENCES project_messages(id)
);
