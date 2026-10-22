package com.daa.graph;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DirectedGraphTest {
    @Test
    void createsFixedSizeGraphAndAddsDirectedEdges() {
        DirectedGraph graph = new DirectedGraph(3);

        graph.addEdge(0, 2);

        assertEquals(3, graph.getVertexCount());
        assertEquals(1, graph.getEdgeCount());
        assertEquals(List.of(2), graph.getAdjacentVertices(0));
        assertEquals(List.of(), graph.getAdjacentVertices(2));
        assertEquals(List.of(List.of(2), List.of(), List.of()), graph.getAdjacencyList());
    }

    @Test
    void permitsParallelEdgesAndSelfLoopsAsSeparateEntries() {
        DirectedGraph graph = new DirectedGraph(2);

        graph.addEdge(0, 1);
        graph.addEdge(0, 1);
        graph.addEdge(1, 1);

        assertEquals(3, graph.getEdgeCount());
        assertEquals(List.of(1, 1), graph.getAdjacentVertices(0));
        assertEquals(List.of(1), graph.getAdjacentVertices(1));
    }

    @Test
    void rejectsNegativeVertexCountAndInvalidEndpoints() {
        assertThrows(IllegalArgumentException.class, () -> new DirectedGraph(-1));

        DirectedGraph graph = new DirectedGraph(2);
        assertThrows(IndexOutOfBoundsException.class, () -> graph.addEdge(-1, 1));
        assertThrows(IndexOutOfBoundsException.class, () -> graph.addEdge(0, 2));
        assertThrows(IndexOutOfBoundsException.class, () -> graph.getAdjacentVertices(2));
    }

    @Test
    void adjacencySnapshotsAreImmutable() {
        DirectedGraph graph = new DirectedGraph(2);
        graph.addEdge(0, 1);

        assertThrows(UnsupportedOperationException.class, () -> graph.getAdjacencyList().clear());
        assertThrows(UnsupportedOperationException.class, () -> graph.getAdjacencyList().get(0).clear());
        assertThrows(UnsupportedOperationException.class, () -> graph.getAdjacentVertices(0).clear());
    }
}
