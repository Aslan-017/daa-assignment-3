package com.daa.experiments;

import java.util.Map;
import java.util.Objects;

/**
 * Structured timing data suitable for later table or plot export.
 *
 * <p>{@code operationCounts} is empty for current algorithms because their
 * implementations do not expose internal operation instrumentation. It must
 * not be interpreted as a measured zero.</p>
 */
public record ExperimentResult(
        String algorithm,
        int vertexCount,
        int edgeCount,
        int repetitionCount,
        long averageElapsedTimeNanos,
        Map<String, Long> operationCounts) {

    public ExperimentResult {
        Objects.requireNonNull(algorithm, "Algorithm cannot be null");
        Objects.requireNonNull(operationCounts, "Operation counts cannot be null");
        if (vertexCount < 0 || edgeCount < 0 || repetitionCount <= 0
                || averageElapsedTimeNanos < 0) {
            throw new IllegalArgumentException("Experiment measurements must be non-negative");
        }
        operationCounts = Map.copyOf(operationCounts);
    }
}
