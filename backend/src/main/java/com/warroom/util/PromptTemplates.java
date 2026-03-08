package com.warroom.util;

/**
 * Centralized repository for AI agent prompts.
 * Designed to guide agents into producing structured, high-quality JSON
 * responses.
 */
public final class PromptTemplates {

    private PromptTemplates() {
        // Prevent instantiation
    }

    public static String buildResearcherPrompt(String title, String description) {
        return buildStrategistPrompt(title, description); // Alias for consistency
    }

    public static String buildStrategistPrompt(String title, String description) {
        return new StringBuilder()
                .append("You are the Lead Researcher and Strategist in the AI Research War-Room.\n")
                .append("Your goal is to expand and strengthen the following project idea:\n\n")
                .append("Title: ").append(title).append("\n")
                .append("Description: ").append(description).append("\n\n")
                .append("Analyze the technical depth, market viability, and overall strategy.")
                .append("Respond ONLY in valid JSON format with the following schema:\n")
                .append("{\n")
                .append("  \"strengths\": [],\n")
                .append("  \"opportunities\": [],\n")
                .append("  \"marketInsights\": [],\n")
                .append("  \"technicalFeasibility\": \"\"\n")
                .append("}")
                .toString();
    }

    public static String buildCriticPrompt(String title, String description) {
        return new StringBuilder()
                .append("You are the Chief Critic Agent in the AI Research War-Room.\n")
                .append("Your goal is to identify flaws and logical gaps in the following idea:\n\n")
                .append("Title: ").append(title).append("\n")
                .append("Description: ").append(description).append("\n\n")
                .append("Be brutally honest and specific about assumptions and risks.")
                .append("Respond ONLY in valid JSON format with the following schema:\n")
                .append("{\n")
                .append("  \"weaknesses\": [],\n")
                .append("  \"risks\": [],\n")
                .append("  \"assumptionsChallenged\": [],\n")
                .append("  \"improvementAreas\": []\n")
                .append("}")
                .toString();
    }

    public static String buildOptimizerPrompt(String title, String description) {
        return new StringBuilder()
                .append("You are the Strategic Optimizer Agent in the AI Research War-Room.\n")
                .append("Your goal is to improve the scalability and efficiency of the following concept:\n\n")
                .append("Title: ").append(title).append("\n")
                .append("Description: ").append(description).append("\n\n")
                .append("Respond ONLY in valid JSON format with the following schema:\n")
                .append("{\n")
                .append("  \"optimizationStrategies\": [],\n")
                .append("  \"costReductionIdeas\": [],\n")
                .append("  \"scalabilityEnhancements\": [],\n")
                .append("  \"performanceImprovements\": []\n")
                .append("}")
                .toString();
    }

    public static String buildDevilPrompt(String title, String description) {
        return new StringBuilder()
                .append("You are the Devil's Advocate Agent in the AI Research War-Room.\n")
                .append("Your goal is to explore worst-case scenarios and hidden risks for the following concept:\n\n")
                .append("Title: ").append(title).append("\n")
                .append("Description: ").append(description).append("\n\n")
                .append("Respond ONLY in valid JSON format with the following schema:\n")
                .append("{\n")
                .append("  \"failureScenarios\": [],\n")
                .append("  \"hiddenRisks\": [],\n")
                .append("  \"worstCaseOutcomes\": [],\n")
                .append("  \"mitigationSuggestions\": []\n")
                .append("}")
                .toString();
    }

    public static String buildSynthesizerPrompt(String debateContent) {
        return new StringBuilder()
                .append("You are the Final Synthesizer Agent in the AI Research War-Room Boardroom.\n")
                .append("Review the following debate session content and produce a final refinement:\n\n")
                .append(debateContent).append("\n\n")
                .append("Produce a cohesive, improved version and a strategic roadmap.")
                .append("Respond ONLY in valid JSON format with the following schema:\n")
                .append("{\n")
                .append("  \"finalVerdict\": \"\",\n")
                .append("  \"keyStrengths\": [],\n")
                .append("  \"majorRisks\": [],\n")
                .append("  \"actionPlan\": [],\n")
                .append("  \"confidenceScore\": 0\n")
                .append("}")
                .toString();
    }
}
