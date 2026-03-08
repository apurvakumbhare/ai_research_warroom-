CREATE TABLE IF NOT EXISTS agent_outputs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    debate_session_id UUID NOT NULL,
    agent_role VARCHAR(100) NOT NULL,
    output_json JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_debate_session FOREIGN KEY (debate_session_id) REFERENCES debate_sessions(id) ON DELETE CASCADE
);
