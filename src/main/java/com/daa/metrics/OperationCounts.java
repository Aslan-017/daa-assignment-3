package com.daa.metrics;

import java.util.EnumMap;
import java.util.Map;

/**
 * Mutable opt-in counters for algorithm instrumentation.
 *
 * <p>Each instrumented algorithm enables only its relevant counters. A counter
 * is present in {@link #asMap()} with value zero when it was enabled but no
 * matching event occurred. Instances are not thread-safe and should be created
 * fresh for each instrumentation run.</p>
 */
public final class OperationCounts {
    public enum Counter {
        edgesExamined,
        dsuFindCalls,
        successfulUnions,
        rejectedCycleFormingEdges,
        priorityQueueInsertions,
        priorityQueueRemovals,
        staleEntriesSkipped,
        verticesProcessed,
        verticesVisited
    }

    private final EnumMap<Counter, Long> counts = new EnumMap<>(Counter.class);

    public void enable(Counter... counters) {
        for (Counter counter : counters) {
            counts.putIfAbsent(counter, 0L);
        }
    }

    public void increment(Counter counter) {
        Long count = counts.get(counter);
        if (count == null) {
            throw new IllegalStateException("Counter was not enabled: " + counter);
        }
        counts.put(counter, count + 1);
    }

    /**
     * Returns an immutable snapshot of enabled counters.
     */
    public Map<String, Long> asMap() {
        Map<String, Long> snapshot = new java.util.LinkedHashMap<>();
        for (Counter counter : Counter.values()) {
            Long count = counts.get(counter);
            if (count != null) {
                snapshot.put(counter.name(), count);
            }
        }
        return Map.copyOf(snapshot);
    }
}
