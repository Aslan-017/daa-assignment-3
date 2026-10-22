package com.daa.mst;

import com.daa.graph.WeightedGraph;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KruskalMstTest {
    @Test
    void findsKnownMinimumSpanningTreeAndSkipsHeavierCycleEdges() {
        WeightedGraph graph = new WeightedGraph(4);
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 2);
        graph.addEdge(2, 3, 3);
        graph.addEdge(0, 2, 8);
        graph.addEdge(0, 3, 10);

        MstResult result = KruskalMst.findMinimumSpanningTree(graph);

        assertTrue(result.isSpanningTree());
        assertEquals(6, result.getTotalWeight());
        assertEquals(3, result.getEdges().size());
    }

    @Test
    void returnsMinimumSpanningForestAndMarksDisconnectedGraph() {
        WeightedGraph graph = new WeightedGraph(5);
        graph.addEdge(0, 1, 2);
        graph.addEdge(2, 3, 4);

        MstResult result = KruskalMst.findMinimumSpanningTree(graph);

        assertFalse(result.isSpanningTree());
        assertEquals(6, result.getTotalWeight());
        assertEquals(2, result.getEdges().size());
    }

    @Test
    void treatsSingleVertexAsACompleteSpanningTree() {
        MstResult result = KruskalMst.findMinimumSpanningTree(new WeightedGraph(1));

        assertTrue(result.isSpanningTree());
        assertEquals(0, result.getTotalWeight());
        assertTrue(result.getEdges().isEmpty());
    }

    @Test
    void returnsOneOfMultipleValidTreesWhenWeightsAreTied() {
        WeightedGraph graph = new WeightedGraph(3);
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 1);
        graph.addEdge(0, 2, 1);

        MstResult result = KruskalMst.findMinimumSpanningTree(graph);

        assertTrue(result.isSpanningTree());
        assertEquals(2, result.getTotalWeight());
        assertEquals(2, result.getEdges().size());
    }

    @Test
    void choosesCheaperParallelEdgeAndIgnoresHeavierOne() {
        WeightedGraph graph = new WeightedGraph(2);
        graph.addEdge(0, 1, 9);
        graph.addEdge(0, 1, 3);

        MstResult result = KruskalMst.findMinimumSpanningTree(graph);

        assertTrue(result.isSpanningTree());
        assertEquals(3, result.getTotalWeight());
        assertEquals(3, result.getEdges().get(0).getWeight());
    }

    @Test
    void usesLongForTheTotalWeight() {
        WeightedGraph graph = new WeightedGraph(3);
        graph.addEdge(0, 1, Integer.MAX_VALUE);
        graph.addEdge(1, 2, Integer.MAX_VALUE);

        MstResult result = KruskalMst.findMinimumSpanningTree(graph);

        assertEquals(2L * Integer.MAX_VALUE, result.getTotalWeight());
    }

    @Test
    void rejectsNullGraph() {
        assertThrows(NullPointerException.class, () -> KruskalMst.findMinimumSpanningTree(null));
    }
}
