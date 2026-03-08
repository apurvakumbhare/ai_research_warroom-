package com.warroom.service.scoring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;

/**
 * Service for deterministic risk assessment based on AI critic and devil's
 * advocate outputs.
 */
@Slf4j
@Service
public class RiskScoringService {

    /**
     * Calculates a normalized risk score (0-100) based on observed negatives.
     * 
     * @param criticOutput map containing 'weaknesses' and 'risks'
     * @param devilOutput  map containing 'failureScenarios'
     * @return an integer score between 0 and 100
     */
    public int calculateRiskScore(Map<String, Object> criticOutput, Map<String, Object> devilOutput) {
        int score = 0;

        score += countItems(criticOutput, "weaknesses") * 5;
        score += countItems(criticOutput, "risks") * 7;
        score += countItems(devilOutput, "failureScenarios") * 10;

        // Clamp 0-100
        int finalScore = Math.max(0, Math.min(100, score));

        log.info("Calculated Risk Score: {}", finalScore);
        return finalScore;
    }

    private int countItems(Map<String, Object> output, String key) {
        if (output == null || !output.containsKey(key)) {
            return 0;
        }
        Object value = output.get(key);
        if (value instanceof Collection) {
            return ((Collection<?>) value).size();
        }
        return 0;
    }
}
