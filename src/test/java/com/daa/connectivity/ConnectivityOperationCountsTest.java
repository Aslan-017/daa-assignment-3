package com.daa.connectivity;

import com.daa.graph.DirectedGraph;
import com.daa.metrics.OperationCounts;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConnectivityOperationCountsTest {
    @Test
    void topologicalSortCountsProcessedVerticesAndBothEdgePasses() {
        DirectedGraph graph = new DirectedGraph(3);
        graph.addEdge(0, 1);
        graph.addEdge(1, 2);
        OperationCounts counters = new OperationCounts();

        TopologicalSort.sort(graph, counters);

        assertEquals(Map.of(
                "verticesProcessed", 3L,
                "edgesExamined", 4L), counters.asMap());
    }

    @Test
    void topologicalSortCountsOnlyProcessedVerticesWhenCycleBlocksOrdering() {
        DirectedGraph graph = new DirectedGraph(3);
        graph.addEdge(0, 1);
        graph.addEdge(1, 0);
        OperationCounts counters = new OperationCounts();

        TopologicalSort.sort(graph, counters);

        assertEquals(Map.of(
                "verticesProcessed", 1L,
                "edgesExamined", 2L), counters.asMap());
    }

    @Test
    void stronglyConnectedComponentsCountsVisitsAndEdgesInBothPasses() {
        DirectedGraph graph = new DirectedGraph(3);
        graph.addEdge(0, 1);
        graph.addEdge(1, 2);
        graph.addEdge(2, 0);
        OperationCounts counters = new OperationCounts();

        StronglyConnectedComponents.find(graph, counters);

        assertEquals(Map.of(
                "verticesVisited", 6L,
                "edgesExamined", 6L), counters.asMap());
    }
}
