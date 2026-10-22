package com.daa.shortestpath;

import com.daa.graph.WeightedGraph;
import com.daa.metrics.OperationCounts;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DijkstraOperationCountsTest {
    @Test
    void countsQueueOperationsStaleEntriesAndReachableEdges() {
        WeightedGraph graph = new WeightedGraph(5);
        graph.addEdge(0, 1, 10);
        graph.addEdge(0, 2, 1);
        graph.addEdge(2, 1, 1);
        graph.addEdge(1, 3, 1);
        OperationCounts counters = new OperationCounts();

        Dijkstra.findShortestPaths(graph, 0, counters);

        assertEquals(Map.of(
                "priorityQueueInsertions", 5L,
                "priorityQueueRemovals", 5L,
                "staleEntriesSkipped", 1L,
                "edgesExamined", 8L), counters.asMap());
    }

    @Test
    void countsOnlyReachableComponentWhenVerticesAreUnreachable() {
        WeightedGraph graph = new WeightedGraph(3);
        graph.addEdge(0, 1, 2);
        OperationCounts counters = new OperationCounts();

        Dijkstra.findShortestPaths(graph, 0, counters);

        assertEquals(Map.of(
                "priorityQueueInsertions", 2L,
                "priorityQueueRemovals", 2L,
                "staleEntriesSkipped", 0L,
                "edgesExamined", 2L), counters.asMap());
    }
}
