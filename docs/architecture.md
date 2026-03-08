# AI Research War-Room Architecture

## 1. System Overview
The AI Research War-Room is a multi-agent orchestration platform designed to refine research papers and startup ideas. It leverages specialized LLM agents (Researcher, Critic, Optimizer, etc.) to debate and improve a concept through multiple iterations.

## 2. High-Level Architecture
The system follows a Clean Architecture approach with a decoupled multi-agent layer and a persistent memory store.

```text
       [ Client/Frontend ]
               | (REST API)
               v
    [ Controller Layer ] <------------------+
               |                            |
               v                            |
    [ Service Layer ] <-----> [ Redis Memory ]
               |               (Conversation Context)
               v
    [ Repository Layer ] <---> [ PostgreSQL ]
               |               (Entity Persistence)
               v
    [ AI Orchestration ] <---> [ OpenAI API ]
               |
               +--- [ PdfExtractor/TextCleaner ]
```

## 3. Component Breakdown

### Controller Layer
- **ProjectController**: Manages CRUD operations for research concepts.
- **DebateController**: Triggers and monitors AI debate sessions.
- **FileUploadController**: Handles multi-part file uploads for document-based research.

### Service Layer
- **ProjectService**: Business logic for project management and coordination.
- **WarRoomOrchestrator**: High-level lifecycle management of the debate boardroom.
- **DebatePipeline**: Sequential execution of specialized AI agents.
- **ConversationMemoryService**: Bridges stateful cross-agent context using Redis.

### Repository Layer
- **ProjectRepository**: UUID-based persistence for projects.
- **DebateSessionRepository**: Tracks execution history and metadata.
- **AgentOutputRepository**: Stores granular feedback from individual agents.

### Redis Cache Layer
- Stores the "working memory" of a debate session.
- Uses Redis LIST structures to maintain chronological agent contributions.
- Ensures the `OpenAI` prompt context remains coherent across agent calls.

### AI Integration Layer
- **DebateAgent Interface**: Common contract for all specialized agents.
- **Concrete Agents**: Researcher, Critic, Optimizer, Devil's Advocate, and Summarizer.
- **OpenAIClient**: Safe wrapper around Spring AI for LLM interaction.

## 4. Data Flow
1. User uploads a research paper or provides a description.
2. The system extracts and cleans text (PDF extraction).
3. A `DebateSession` is initialized.
4. Agents run in sequence:
   - **Researcher** expands the idea.
   - **Critic** identifies flaws.
   - **Optimizer** suggests improvements.
   - **Devil's Advocate** explores failure modes.
5. All results are stored in PostgreSQL and cached in Redis for context continuity.
6. **Summarizer** synthesizes the final result and updates the `Project`.

## 5. Deployment Architecture
The system is fully containerized using Docker.
- **warroom-backend**: Spring Boot application.
- **warroom-postgres**: PostgreSQL 15 persistent store.
- **warroom-redis**: Redis 7 memory layer.
- **warroom-bridge**: Shared internal bridge network.

## 6. Scaling Strategy
- Vertical: Optimized JVM settings and connection pooling.
- Horizontal: Stateless service design allows multiple backend instances behind a load balancer, sharing the same Redis/PostgreSQL clusters.

## 7. Security Considerations
- Non-root container execution.
- Environment-based secret management (OpenAI API Keys).
- Input validation (JSR-303) and sanitization.
- Exception mapping to prevent internal leakages.

## 8. Future Improvements
- Multi-user authentication (Spring Security).
- WebSocket notifications for real-time debate progress.
- Vector database integration for long-term memory (RAG).
