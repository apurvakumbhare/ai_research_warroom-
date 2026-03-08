package com.warroom.migration;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteBatch;
import com.google.cloud.firestore.DocumentReference;
import com.warroom.entity.Project;
import com.warroom.entity.DebateSession;
import com.warroom.entity.AgentOutput;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.time.ZoneOffset;

/**
 * One-time migration script to move historical data from PostgreSQL to
 * Firestore.
 * 
 * Instructions:
 * 1. Uncomment the @Component annotation below to run this on startup.
 * 2. Update the JDBC URL, username, and password with your PostgreSQL
 * credentials.
 * 3. Make sure the PostgreSQL JDBC driver is on your classpath (e.g., add to
 * pom.xml).
 * 4. Run the application. Once it logs "Migration completed successfully",
 * shut down the application and re-comment the @Component annotation.
 */
@Slf4j
// @Component // <-- Uncomment to run migration
@RequiredArgsConstructor
public class FirestoreMigrationRunner implements CommandLineRunner {

    private final Firestore firestore;

    // TODO: Configure your old PostgreSQL connection details here
    private static final String PG_URL = "jdbc:postgresql://localhost:5432/warroom";
    private static final String PG_USER = "postgres";
    private static final String PG_PASSWORD = "password";

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting one-time migration from PostgreSQL to Firestore...");

        try (Connection conn = DriverManager.getConnection(PG_URL, PG_USER, PG_PASSWORD)) {

            migrateProjects(conn);
            migrateDebateSessions(conn);
            migrateAgentOutputs(conn);

            log.info("Migration completed successfully. Please disable this runner.");
        } catch (Exception e) {
            log.error("Migration failed: {}", e.getMessage(), e);
        }
    }

    private void migrateProjects(Connection conn) throws Exception {
        log.info("Migrating Projects...");
        String sql = "SELECT id, title, description, type, status, final_content, created_at, updated_at FROM projects";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            WriteBatch batch = firestore.batch();
            int count = 0;

            while (rs.next()) {
                Project p = new Project();
                p.setId(rs.getString("id"));
                p.setProjectName(rs.getString("title"));
                p.setCoreHypothesis(rs.getString("description"));
                p.setType(rs.getString("type"));
                p.setStatus(rs.getString("status"));
                p.setFinalContent(rs.getString("final_content"));

                if (rs.getTimestamp("created_at") != null) {
                    p.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                }
                if (rs.getTimestamp("updated_at") != null) {
                    p.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                }

                DocumentReference docRef = firestore.collection("projects").document(p.getId());
                batch.set(docRef, p);
                count++;
            }
            if (count > 0) {
                batch.commit().get();
            }
            log.info("Migrated {} Projects.", count);
        }
    }

    private void migrateDebateSessions(Connection conn) throws Exception {
        log.info("Migrating Debate Sessions...");
        String sql = "SELECT id, project_id, status, started_at, completed_at FROM debate_sessions";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            WriteBatch batch = firestore.batch();
            int count = 0;

            while (rs.next()) {
                DebateSession session = new DebateSession();
                session.setId(rs.getString("id"));
                session.setProjectId(rs.getString("project_id"));
                session.setStatus(rs.getString("status"));

                if (rs.getTimestamp("started_at") != null) {
                    session.setStartedAt(rs.getTimestamp("started_at").toLocalDateTime());
                }
                if (rs.getTimestamp("completed_at") != null) {
                    session.setCompletedAt(rs.getTimestamp("completed_at").toLocalDateTime());
                }

                DocumentReference docRef = firestore.collection("debate_sessions").document(session.getId());
                batch.set(docRef, session);
                count++;
            }
            if (count > 0) {
                batch.commit().get();
            }
            log.info("Migrated {} Debate Sessions.", count);
        }
    }

    private void migrateAgentOutputs(Connection conn) throws Exception {
        log.info("Migrating Agent Outputs...");
        String sql = "SELECT id, debate_session_id, agent_role, output_json, created_at FROM agent_outputs";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            WriteBatch batch = firestore.batch();
            int count = 0;

            while (rs.next()) {
                AgentOutput output = new AgentOutput();
                output.setId(rs.getString("id"));
                output.setDebateSessionId(rs.getString("debate_session_id"));
                output.setAgentName(rs.getString("agent_role"));
                output.setOutput(rs.getString("output_json"));

                if (rs.getTimestamp("created_at") != null) {
                    output.setGeneratedAt(rs.getTimestamp("created_at").toLocalDateTime());
                }

                DocumentReference docRef = firestore.collection("agent_outputs").document(output.getId());
                batch.set(docRef, output);
                count++;
            }
            if (count > 0) {
                batch.commit().get();
            }
            log.info("Migrated {} Agent Outputs.", count);
        }
    }
}
