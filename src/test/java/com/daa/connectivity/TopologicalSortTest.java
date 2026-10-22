package com.daa.connectivity;

import com.daa.graph.DirectedGraph;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TopologicalSortTest {
    @Test
    void returnsKnownValidOrderingForDag() {
        DirectedGraph graph = new DirectedGraph(6);
        graph.addEdge(5, 2);
        graph.addEdge(5, 0);
        graph.addEdge(4, 0);
        graph.addEdge(4, 1);
        graph.addEdge(2, 3);
        graph.addEdge(3, 1);

        TopologicalSortResult result = TopologicalSort.sort(graph);

        assertTrue(result.isAcyclic());
        assertEquals(6, result.getOrdering().size());
        assertValidOrdering(graph, result.getOrdering());
    }

    @Test
    void acceptsAnyOfMultipleValidOrderings() {
        DirectedGraph graph = new DirectedGraph(3);
        graph.addEdge(0, 2);
        graph.addEdge(1, 2);

        TopologicalSortResult result = TopologicalSort.sort(graph);

        assertTrue(result.isAcyclic());
        assertValidOrdering(graph, result.getOrdering());
        assertTrue(result.getOrdering().indexOf(2) > result.getOrdering().indexOf(0));
        assertTrue(result.getOrdering().indexOf(2) > result.getOrdering().indexOf(1));
    }

    @Test
    void detectsCycleAndDoesNotReturnPartialOrdering() {
        DirectedGraph graph = new DirectedGraph(3);
        graph.addEdge(0, 1);
        graph.addEdge(1, 2);
        graph.addEdge(2, 0);

        TopologicalSortResult result = TopologicalSort.sort(graph);

        assertFalse(result.isAcyclic());
        assertTrue(result.getOrdering().isEmpty());
    }

    @Test
    void ordersDisconnectedDag() {
        DirectedGraph graph = new DirectedGraph(5);
        graph.addEdge(0, 1);
        graph.addEdge(2, 3);

        TopologicalSortResult result = TopologicalSort.sort(graph);

        assertTrue(result.isAcyclic());
        assertValidOrdering(graph, result.getOrdering());
    }

    @Test
    void handlesEmptyGraphAndSingleVertex() {
        TopologicalSortResult emptyResult = TopologicalSort.sort(new DirectedGraph(0));
        TopologicalSortResult singleResult = TopologicalSort.sort(new DirectedGraph(1));

        assertTrue(emptyResult.isAcyclic());
        assertTrue(emptyResult.getOrdering().isEmpty());
        assertTrue(singleResult.isAcyclic());
        assertEquals(List.of(0), singleResult.getOrdering());
    }

    @Test
    void handlesParallelEdgesAndSelfLoopCycle() {
        DirectedGraph parallelEdges = new DirectedGraph(2);
        parallelEdges.addEdge(0, 1);
        parallelEdges.addEdge(0, 1);
        assertTrue(TopologicalSort.sort(parallelEdges).isAcyclic());

        DirectedGraph selfLoop = new DirectedGraph(1);
        selfLoop.addEdge(0, 0);
        TopologicalSortResult result = TopologicalSort.sort(selfLoop);
        assertFalse(result.isAcyclic());
        assertTrue(result.getOrdering().isEmpty());
    }

    private void assertValidOrdering(DirectedGraph graph, List<Integer> ordering) {
        assertEquals(graph.getVertexCount(), ordering.size());
        Map<Integer, Integer> position = new HashMap<>();
        for (int index = 0; index < ordering.size(); index++) {
            assertTrue(position.put(ordering.get(index), index) == null,
                    "Each vertex must appear exactly once");
        }
        for (int source = 0; source < graph.getVertexCount(); source++) {
            for (int destination : graph.getAdjacentVertices(source)) {
                assertTrue(position.get(source) < position.get(destination),
                        "Every edge must point forward in the ordering");
            }
        }
    }
}
