package com.warroom.service.scoring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;

/**
 * Service for determining the overall confidence in a project's success.
 */
@Slf4j
@Service
public class ConfidenceScoreService {

    /**
     * Calculates the confidence score (0-100) by balancing positive optimizations
     * against calculated risks.
     * 
     * @param researcherOutput map containing 'strengths' and 'opportunities'
     * @param optimizerOutput  map containing 'optimizationStrategies'
     * @param riskScore        the pre-calculated risk score from RiskScoringService
     * @return an integer score between 0 and 100
     */
    public int calculateConfidenceScore(Map<String, Object> researcherOutput,
            Map<String, Object> optimizerOutput,
            int riskScore) {
        double score = 50.0; // Base confidence

        // Positive influence
        score += countItems(researcherOutput, "strengths") * 5;
        score += countItems(researcherOutput, "opportunities") * 4;
        score += countItems(optimizerOutput, "optimizationStrategies") * 3;

        // Negative influence from risk
        score -= (riskScore * 0.4);

        // Clamp 0-100
        int finalScore = (int) Math.max(0, Math.min(100, Math.round(score)));

        log.info("Calculated Confidence Score: {} (based on risk: {})", finalScore, riskScore);
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
