-- Add validation_issues column to track code quality checks
ALTER TABLE project_messages
ADD COLUMN IF NOT EXISTS validation_issues TEXT;
