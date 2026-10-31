package com.daa.experiments;

import java.util.Map;
import java.util.Objects;

public record DsuExperimentResult(
        String variant,
        long seed,
        int vertexCount,
        int operationCount,
        int repetitionCount,
        long averageElapsedTimeNanos,
        Map<String, Long> operationCounts) {

    public DsuExperimentResult {
        Objects.requireNonNull(variant, "Variant cannot be null");
        Objects.requireNonNull(operationCounts, "Operation counts cannot be null");
        if (vertexCount <= 0 || operationCount < 0 || repetitionCount <= 0
                || averageElapsedTimeNanos < 0) {
            throw new IllegalArgumentException("Experiment measurements are invalid");
        }
        operationCounts = Map.copyOf(operationCounts);
    }
}
