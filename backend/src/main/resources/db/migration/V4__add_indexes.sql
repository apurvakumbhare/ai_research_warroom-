-- Performance indexes for frequent queries
CREATE INDEX IF NOT EXISTS idx_projects_status ON projects(status);
CREATE INDEX IF NOT EXISTS idx_debate_sessions_project_id ON debate_sessions(project_id);
CREATE INDEX IF NOT EXISTS idx_agent_outputs_debate_session_id ON agent_outputs(debate_session_id);
CREATE INDEX IF NOT EXISTS idx_agent_outputs_agent_role ON agent_outputs(agent_role);
