package com.daa.experiments;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DsuVariantExperimentTest {
    @Test
    void generatesTheSameOperationSequenceForTheSameSeed() {
        List<DsuVariantExperiment.Operation> first =
                DsuVariantExperiment.generateOperations(20, 100, 42);
        List<DsuVariantExperiment.Operation> second =
                DsuVariantExperiment.generateOperations(20, 100, 42);
        List<DsuVariantExperiment.Operation> differentSeed =
                DsuVariantExperiment.generateOperations(20, 100, 43);

        assertEquals(first, second);
        assertNotEquals(first, differentSeed);
    }

    @Test
    void verifiesEveryQueryAndCountsActualParentPointerChanges() {
        List<DsuVariantExperiment.Operation> operations = List.of(
                union(2, 3),
                union(0, 1),
                union(2, 0),
                union(4, 2),
                query(1, 4),
                query(3, 4));

        assertEquals(
                List.of(true, true),
                DsuVariantExperiment.verifyConnectivityResults(5, operations));

        List<DsuExperimentResult> results =
                DsuVariantExperiment.runExperiment(5, operations, 42);
        assertEquals(4, results.size());
        assertEquals(List.of(
                        "basic",
                        "path_compression_only",
                        "union_by_size_only",
                        "path_compression_and_union_by_size"),
                results.stream().map(DsuExperimentResult::variant).toList());

        for (DsuExperimentResult result : results) {
            assertEquals(6, result.operationCount());
            assertEquals(DsuVariantExperiment.REPETITIONS, result.repetitionCount());
            assertEquals(2L, result.operationCounts().get("connectivityQueries"));
            assertEquals(12L, result.operationCounts().get("dsuFindCalls"));
            assertEquals(4L, result.operationCounts().get("successfulUnions"));
            assertEquals(4L, result.operationCounts().get("unionParentPointerUpdates"));
            assertTrue(result.averageElapsedTimeNanos() >= 0);
        }

        assertEquals(5L, results.get(0).operationCounts().get("findTraversals"));
        assertEquals(5L, results.get(1).operationCounts().get("findTraversals"));
        assertEquals(6L, results.get(2).operationCounts().get("findTraversals"));
        assertEquals(6L, results.get(3).operationCounts().get("findTraversals"));
        assertEquals(0L,
                results.get(0).operationCounts().get("pathCompressionParentPointerUpdates"));
        assertEquals(3L,
                results.get(1).operationCounts().get("pathCompressionParentPointerUpdates"));
        assertEquals(0L,
                results.get(2).operationCounts().get("pathCompressionParentPointerUpdates"));
        assertEquals(1L,
                results.get(3).operationCounts().get("pathCompressionParentPointerUpdates"));
    }

    private DsuVariantExperiment.Operation union(int first, int second) {
        return new DsuVariantExperiment.Operation(
                DsuVariantExperiment.OperationType.UNION, first, second);
    }

    private DsuVariantExperiment.Operation query(int first, int second) {
        return new DsuVariantExperiment.Operation(
                DsuVariantExperiment.OperationType.CONNECTIVITY_QUERY, first, second);
    }
}
