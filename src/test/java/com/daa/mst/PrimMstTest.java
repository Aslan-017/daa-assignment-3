package com.daa.mst;

import com.daa.graph.WeightedGraph;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PrimMstTest {
    @Test
    void findsKnownMinimumSpanningTreeAndSkipsHeavierCycleEdges() {
        WeightedGraph graph = new WeightedGraph(4);
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 2);
        graph.addEdge(2, 3, 3);
        graph.addEdge(0, 2, 8);
        graph.addEdge(0, 3, 10);

        MstResult result = PrimMst.findMinimumSpanningTree(graph);

        assertTrue(result.isSpanningTree());
        assertEquals(6, result.getTotalWeight());
        assertEquals(3, result.getEdges().size());
    }

    @Test
    void returnsMinimumSpanningForestAndMarksDisconnectedGraph() {
        WeightedGraph graph = new WeightedGraph(5);
        graph.addEdge(0, 1, 2);
        graph.addEdge(2, 3, 4);

        MstResult result = PrimMst.findMinimumSpanningTree(graph);

        assertFalse(result.isSpanningTree());
        assertEquals(6, result.getTotalWeight());
        assertEquals(2, result.getEdges().size());
    }

    @Test
    void treatsSingleVertexAsACompleteSpanningTree() {
        MstResult result = PrimMst.findMinimumSpanningTree(new WeightedGraph(1));

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

        MstResult result = PrimMst.findMinimumSpanningTree(graph);

        assertTrue(result.isSpanningTree());
        assertEquals(2, result.getTotalWeight());
        assertEquals(2, result.getEdges().size());
    }

    @Test
    void choosesCheaperParallelEdgeAndIgnoresHeavierOne() {
        WeightedGraph graph = new WeightedGraph(2);
        graph.addEdge(0, 1, 9);
        graph.addEdge(0, 1, 3);

        MstResult result = PrimMst.findMinimumSpanningTree(graph);

        assertTrue(result.isSpanningTree());
        assertEquals(3, result.getTotalWeight());
        assertEquals(3, result.getEdges().get(0).getWeight());
    }

    @Test
    void agreesWithKruskalOnConnectedUndirectedWeightedGraphs() {
        WeightedGraph graph = new WeightedGraph(6);
        graph.addEdge(0, 1, 4);
        graph.addEdge(0, 2, 3);
        graph.addEdge(1, 2, 1);
        graph.addEdge(1, 3, 2);
        graph.addEdge(2, 3, 4);
        graph.addEdge(3, 4, 2);
        graph.addEdge(4, 5, 6);
        graph.addEdge(2, 5, 8);

        MstResult primResult = PrimMst.findMinimumSpanningTree(graph);
        MstResult kruskalResult = KruskalMst.findMinimumSpanningTree(graph);

        assertTrue(primResult.isSpanningTree());
        assertTrue(kruskalResult.isSpanningTree());
        assertEquals(kruskalResult.getTotalWeight(), primResult.getTotalWeight());
    }

    @Test
    void processesEveryDisconnectedComponentOnlyOnce() {
        WeightedGraph graph = new WeightedGraph(4);
        graph.addEdge(0, 1, 1);
        graph.addEdge(2, 3, 2);
        graph.addEdge(2, 2, 0);

        MstResult result = PrimMst.findMinimumSpanningTree(graph);

        assertFalse(result.isSpanningTree());
        assertEquals(3, result.getTotalWeight());
        assertEquals(2, result.getEdges().size());
    }

    @Test
    void rejectsNullGraph() {
        assertThrows(NullPointerException.class, () -> PrimMst.findMinimumSpanningTree(null));
    }
}
