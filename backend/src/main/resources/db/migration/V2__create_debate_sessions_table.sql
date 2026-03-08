CREATE TABLE IF NOT EXISTS debate_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL,
    status VARCHAR(50),
    started_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMPTZ,
    CONSTRAINT fk_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
);
