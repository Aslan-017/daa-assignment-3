package com.daa.experiments;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExperimentResultTest {
    @Test
    void storesStructuredMeasurementAndCopiesOperationCounts() {
        Map<String, Long> operationCounts = new HashMap<>();
        operationCounts.put("edge-relaxations", 12L);

        ExperimentResult result = new ExperimentResult(
                "Dijkstra", "connected", 10, 20, 5, 100L, operationCounts);
        operationCounts.put("edge-relaxations", 99L);

        assertEquals("Dijkstra", result.algorithm());
        assertEquals("connected", result.scenario());
        assertEquals(10, result.vertexCount());
        assertEquals(20, result.edgeCount());
        assertEquals(5, result.repetitionCount());
        assertEquals(100L, result.averageElapsedTimeNanos());
        assertEquals(Map.of("edge-relaxations", 12L), result.operationCounts());
        assertThrows(
                UnsupportedOperationException.class,
                () -> result.operationCounts().put("new-counter", 1L));
    }

    @Test
    void rejectsInvalidMeasurementCounts() {
        assertThrows(IllegalArgumentException.class,
                () -> new ExperimentResult("Prim", "connected", -1, 0, 5, 100, Map.of()));
        assertThrows(IllegalArgumentException.class,
                () -> new ExperimentResult("Prim", "connected", 1, 0, 0, 100, Map.of()));
    }
}
