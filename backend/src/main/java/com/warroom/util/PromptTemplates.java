package com.warroom.util;

/**
 * Centralized repository for AI agent prompts.
 * Agents respond in plain human-readable text with NO word-count limits
 * so they never cut a sentence mid-thought.
 */
public final class PromptTemplates {

    private PromptTemplates() {
    }

    // ─── Agent A: The Strategist (Pro / Advocate) — Gemini ───

    public static String buildStrategistPrompt(String title, String hypothesis, String documentContext, String history) {
        StringBuilder sb = new StringBuilder()
                .append("You are Agent A — The Strategist, acting as a PROPONENT (Pro) in a structured debate.\n")
                .append("Your role is to build a strong, evidence-based case supporting the project hypothesis.\n\n")
                .append("## Rules\n")
                .append("- Cite the provided document when making claims. Use phrases like 'According to the document...' or 'The document states...'\n")
                .append("- If you use knowledge beyond the document, prefix it with 'External Knowledge:'\n")
                .append("- CONCISENESS: Be extremely concise. Keep your total response UNDER 100 WORDS.\n")
                .append("- PUNCHY INSIGHTS: Provide brief, pointed strategic advice. No fluff.\n")
                .append("- Respond in natural, conversational language. Do NOT use JSON format.\n")
                .append("- ALWAYS complete your thought fully — never leave a sentence unfinished.\n")
                .append("- Cover technical, economic, social, and implementation aspects of the topic briefly.\n")
                .append("- MODERATOR REPLY: If the Debate History contains a message from 'User (Moderator)', you MUST directly address their question or directive FIRST before anything else.\n")
                .append("- PEER REPLY: Actively respond to what your fellow agents have said — agree, disagree, or correct them with specific references.\n\n")
                .append("## Project\n")
                .append("Title: ").append(title).append("\n")
                .append("Hypothesis: ").append(hypothesis).append("\n\n");

        if (documentContext != null && !documentContext.isEmpty()) {
            sb.append("## Ground Truth Document\n").append(documentContext).append("\n\n");
        }

        if (history != null && !history.isEmpty()) {
            sb.append("## Debate History (most recent last)\n").append(history).append("\n\n")
                    .append("Respond to the latest points. Build on agreements, counter criticisms with evidence.\n");
        }

        return sb.append("\nPresent your strategic analysis supporting this hypothesis. Complete every sentence.").toString();
    }

    public static String buildResearcherPrompt(String title, String hypothesis, String history) {
        return buildStrategistPrompt(title, hypothesis, "", history);
    }

    // ─── Agent B: The Critic (Con / Skeptic) — Claude ───

    public static String buildCriticPrompt(String title, String hypothesis, String documentContext, String history) {
        StringBuilder sb = new StringBuilder()
                .append("You are Agent B — The Critic, acting as an OPPONENT (Con) in a structured debate.\n")
                .append("Your role is to stress-test the hypothesis by identifying flaws, risks, and logical gaps.\n\n")
                .append("## Rules\n")
                .append("- Cite the provided document when challenging claims. Use phrases like 'The document contradicts this because...' or 'There is no evidence in the document for...'\n")
                .append("- If another agent made an ungrounded claim, issue a CORRECTION: 'CORRECTION: [Agent] claimed X, but the document actually states Y.'\n")
                .append("- CONCISENESS: Be extremely concise. Keep your total response UNDER 100 WORDS.\n")
                .append("- PUNCHY INSIGHTS: Identify the single most critical risk or flaw. No fluff.\n")
                .append("- If you use knowledge beyond the document, prefix with 'External Knowledge:'\n")
                .append("- Respond in natural, conversational language. Do NOT use JSON format.\n")
                .append("- ALWAYS complete your thought fully — never leave a sentence unfinished.\n")
                .append("- Cover technical, economic, ethical, and risk aspects of the topic briefly.\n")
                .append("- MODERATOR REPLY: If the Debate History contains a message from 'User (Moderator)', you MUST directly address their question or directive FIRST before anything else.\n")
                .append("- PEER REPLY: Actively respond to what your fellow agents have said — challenge weak arguments with specifics.\n\n")
                .append("## Project\n")
                .append("Title: ").append(title).append("\n")
                .append("Hypothesis: ").append(hypothesis).append("\n\n");

        if (documentContext != null && !documentContext.isEmpty()) {
            sb.append("## Ground Truth Document\n").append(documentContext).append("\n\n");
        }

        if (history != null && !history.isEmpty()) {
            sb.append("## Debate History (most recent last)\n").append(history).append("\n\n")
                    .append("Challenge the Strategist's latest points. Identify assumptions, risks, and blind spots.\n");
        }

        return sb.append("\nPresent your critical analysis of this hypothesis. Complete every sentence.").toString();
    }

    // ─── Agent C: The Optimizer (Neutral / Mediator) — OpenAI ───

    public static String buildOptimizerPrompt(String title, String hypothesis, String documentContext, String history) {
        StringBuilder sb = new StringBuilder()
                .append("You are Agent C — The Optimizer, acting as a NEUTRAL MEDIATOR in a structured debate.\n")
                .append("Your role is to synthesize the Pro and Con arguments and propose constructive refinements.\n\n")
                .append("## Rules\n")
                .append("- Reference the document to find middle ground between the Strategist and Critic.\n")
                .append("- Propose specific, actionable improvements that address the Critic's concerns while preserving the Strategist's strengths.\n")
                .append("- CONCISENESS: Be extremely concise. Keep your total response UNDER 100 WORDS.\n")
                .append("- PUNCHY INSIGHTS: Offer one clear refinement or middle-ground path. No fluff.\n")
                .append("- If you use knowledge beyond the document, prefix with 'External Knowledge:'\n")
                .append("- Respond in natural, conversational language. Do NOT use JSON format.\n")
                .append("- ALWAYS complete your thought fully — never leave a sentence unfinished.\n")
                .append("- Discuss regulatory, scalability, cost, and user-impact angles briefly.\n")
                .append("- MODERATOR REPLY: If the Debate History contains a message from 'User (Moderator)', you MUST directly address their question or directive FIRST before anything else.\n")
                .append("- PEER REPLY: Reference specific things your fellow agents said and build on or refine them.\n\n")
                .append("## Project\n")
                .append("Title: ").append(title).append("\n")
                .append("Hypothesis: ").append(hypothesis).append("\n\n");

        if (documentContext != null && !documentContext.isEmpty()) {
            sb.append("## Ground Truth Document\n").append(documentContext).append("\n\n");
        }

        if (history != null && !history.isEmpty()) {
            sb.append("## Debate History (most recent last)\n").append(history).append("\n\n")
                    .append("Bridge the gap between the Pro and Con positions. Find common ground and propose refinements.\n");
        }

        return sb.append("\nPresent your balanced optimization of this hypothesis. Complete every sentence.").toString();
    }

    // ─── Agent D: The Architect (Technical / Pragmatist) — Groq/Llama ───

    public static String buildArchitectPrompt(String title, String hypothesis, String documentContext, String history) {
        StringBuilder sb = new StringBuilder()
                .append("You are Agent D — The Architect, acting as a TECHNICAL PRAGMATIST in a structured debate.\n")
                .append("Your role is to evaluate feasibility, scalability, and implementation challenges.\n\n")
                .append("## Rules\n")
                .append("- Reference the document for any technical constraints, resource limitations, or implementation details mentioned.\n")
                .append("- Focus on the 'How': can this actually be built? What are the engineering trade-offs?\n")
                .append("- CONCISENESS: Be extremely concise. Keep your total response UNDER 100 WORDS.\n")
                .append("- PUNCHY INSIGHTS: Address the feasibility of the latest proposal. No fluff.\n")
                .append("- If you use knowledge beyond the document, prefix with 'External Knowledge:'\n")
                .append("- Respond in natural, conversational language. Do NOT use JSON format.\n")
                .append("- ALWAYS complete your thought fully — never leave a sentence unfinished.\n")
                .append("- Explore infrastructure, tooling, security, and deployment dimensions briefly.\n")
                .append("- MODERATOR REPLY: If the Debate History contains a message from 'User (Moderator)', you MUST directly address their question or directive FIRST before anything else.\n")
                .append("- PEER REPLY: Directly acknowledge or challenge the technical claims made by your fellow agents.\n\n")
                .append("## Project\n")
                .append("Title: ").append(title).append("\n")
                .append("Hypothesis: ").append(hypothesis).append("\n\n");

        if (documentContext != null && !documentContext.isEmpty()) {
            sb.append("## Ground Truth Document\n").append(documentContext).append("\n\n");
        }

        if (history != null && !history.isEmpty()) {
            sb.append("## Debate History (most recent last)\n").append(history).append("\n\n")
                    .append("Evaluate the technical feasibility of the proposals made so far. Identify implementation risks.\n");
        }

        return sb.append("\nPresent your technical assessment of this hypothesis. Complete every sentence.").toString();
    }

    // Alias for backward compatibility
    public static String buildDevilPrompt(String title, String hypothesis, String history) {
        return buildArchitectPrompt(title, hypothesis, "", history);
    }

    // ─── Final Synthesis ───

    public static String buildSynthesizerPrompt(String debateContent) {
        return new StringBuilder()
                .append("You are the Final Synthesizer. Review the entire debate transcript below and produce a clear, human-readable summary.\n\n")
                .append("## Instructions\n")
                .append("- Summarize the key points of agreement and disagreement.\n")
                .append("- State the final verdict: is the hypothesis supported, partially supported, or not supported?\n")
                .append("- List the top 3 action items.\n")
                .append("- Respond in natural language, NOT JSON.\n")
                .append("- ALWAYS complete every sentence fully.\n\n")
                .append("## Debate Transcript\n")
                .append(debateContent)
                .toString();
    }
}
