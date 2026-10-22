package com.daa.graph;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WeightedGraphTest {
    @Test
    void createsGraphWithFixedVertexCountAndNoEdges() {
        WeightedGraph graph = new WeightedGraph(3);

        assertEquals(3, graph.getVertexCount());
        assertEquals(0, graph.getEdgeCount());
        assertEquals(List.of(List.of(), List.of(), List.of()), graph.getAdjacencyList());
    }

    @Test
    void insertsEdgeOnceAndAddsOrientedEntriesToBothAdjacencyLists() {
        WeightedGraph graph = new WeightedGraph(3);
        Edge insertedEdge = graph.addEdge(0, 2, 7);

        assertEquals(1, graph.getEdgeCount());
        assertEquals(List.of(insertedEdge), graph.getEdges());
        assertEquals(List.of(insertedEdge), graph.getAdjacentEdges(0));
        assertEquals(List.of(new Edge(2, 0, 7)), graph.getAdjacentEdges(2));
        assertEquals(
                List.of(List.of(insertedEdge), List.of(), List.of(new Edge(2, 0, 7))),
                graph.getAdjacencyList());
    }

    @Test
    void allowsParallelEdgesAsSeparateInsertions() {
        WeightedGraph graph = new WeightedGraph(2);

        graph.addEdge(0, 1, 4);
        graph.addEdge(0, 1, 4);

        assertEquals(2, graph.getEdgeCount());
        assertEquals(2, graph.getEdges().size());
        assertEquals(2, graph.getAdjacentEdges(0).size());
        assertEquals(2, graph.getAdjacentEdges(1).size());
        assertNotSame(graph.getAdjacentEdges(0).get(0), graph.getAdjacentEdges(0).get(1));
    }

    @Test
    void rejectsNegativeVertexCountAndInvalidEdgeEndpoints() {
        assertThrows(IllegalArgumentException.class, () -> new WeightedGraph(-1));

        WeightedGraph graph = new WeightedGraph(2);
        assertThrows(IndexOutOfBoundsException.class, () -> graph.addEdge(-1, 1, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> graph.addEdge(0, 2, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> graph.getAdjacentEdges(-1));
    }

    @Test
    void rejectsNegativeWeightsButEdgeCanRepresentThem() {
        WeightedGraph graph = new WeightedGraph(2);

        assertThrows(IllegalArgumentException.class, () -> graph.addEdge(0, 1, -1));
        assertEquals(-1, new Edge(0, 1, -1).getWeight());
        assertEquals(0, graph.getEdgeCount());
    }

    @Test
    void allowsSelfLoopOnceInAdjacencyList() {
        WeightedGraph graph = new WeightedGraph(1);

        graph.addEdge(0, 0, 3);

        assertEquals(1, graph.getEdgeCount());
        assertEquals(1, graph.getAdjacentEdges(0).size());
        assertTrue(graph.getAdjacentEdges(0).get(0).equals(new Edge(0, 0, 3)));
    }

    @Test
    void returnedEdgeAndAdjacencyListsAreImmutable() {
        WeightedGraph graph = new WeightedGraph(2);
        graph.addEdge(0, 1, 1);

        assertThrows(UnsupportedOperationException.class, () -> graph.getEdges().clear());
        assertThrows(UnsupportedOperationException.class, () -> graph.getAdjacentEdges(0).clear());
        assertThrows(UnsupportedOperationException.class, () -> graph.getAdjacencyList().get(0).clear());
    }
}
