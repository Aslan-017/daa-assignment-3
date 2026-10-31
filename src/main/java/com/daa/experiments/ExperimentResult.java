package com.daa.experiments;

import java.util.Map;
import java.util.Objects;

public record ExperimentResult(
        String algorithm,
        String scenario,
        int vertexCount,
        int edgeCount,
        int repetitionCount,
        long averageElapsedTimeNanos,
        Map<String, Long> operationCounts) {

    public ExperimentResult {
        Objects.requireNonNull(algorithm, "Algorithm cannot be null");
        Objects.requireNonNull(scenario, "Scenario cannot be null");
        Objects.requireNonNull(operationCounts, "Operation counts cannot be null");
        if (vertexCount < 0 || edgeCount < 0 || repetitionCount <= 0
                || averageElapsedTimeNanos < 0) {
            throw new IllegalArgumentException("Experiment measurements must be non-negative");
        }
        operationCounts = Map.copyOf(operationCounts);
    }
}
