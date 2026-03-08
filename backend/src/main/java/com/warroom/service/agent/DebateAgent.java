package com.warroom.service.agent;

/**
 * Interface defining the base contract for all specialized AI agents.
 */
public interface DebateAgent {

    /**
     * @return The unique string identifier for this agent type.
     */
    String getAgentRole();

    /**
     * Executes the agent's logic against a project concept.
     * 
     * @param title           the project title
     * @param description     the project description or previous agent output
     * @param previousContext optional context from previous debate steps
     * @return the structured JSON response from the agent
     */
    String execute(String title, String description, String previousContext);
}
