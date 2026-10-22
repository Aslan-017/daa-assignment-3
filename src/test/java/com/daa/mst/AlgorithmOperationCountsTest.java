package com.daa.mst;

import com.daa.graph.WeightedGraph;
import com.daa.metrics.OperationCounts;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AlgorithmOperationCountsTest {
    @Test
    void kruskalCountsExaminedEdgesDsuFindsUnionsAndCycleRejections() {
        WeightedGraph graph = new WeightedGraph(4);
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 2);
        graph.addEdge(0, 2, 3);
        graph.addEdge(2, 3, 4);
        OperationCounts counters = new OperationCounts();

        KruskalMst.findMinimumSpanningTree(graph, counters);

        assertEquals(Map.of(
                "edgesExamined", 4L,
                "dsuFindCalls", 8L,
                "successfulUnions", 3L,
                "rejectedCycleFormingEdges", 1L), counters.asMap());
    }

    @Test
    void primCountsQueueOperationsStaleCandidatesAndExaminedEdges() {
        WeightedGraph graph = new WeightedGraph(3);
        graph.addEdge(0, 1, 1);
        graph.addEdge(0, 2, 2);
        graph.addEdge(1, 2, 3);
        OperationCounts counters = new OperationCounts();

        PrimMst.findMinimumSpanningTree(graph, counters);

        assertEquals(Map.of(
                "priorityQueueInsertions", 3L,
                "priorityQueueRemovals", 3L,
                "staleEntriesSkipped", 1L,
                "edgesExamined", 3L), counters.asMap());
    }
}
