package com.daa.shortestpath;

import com.daa.graph.WeightedGraph;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DijkstraTest {
    @Test
    void findsKnownShortestDistancesFromSource() {
        WeightedGraph graph = new WeightedGraph(5);
        graph.addEdge(0, 1, 4);
        graph.addEdge(0, 2, 1);
        graph.addEdge(2, 1, 2);
        graph.addEdge(1, 3, 1);
        graph.addEdge(2, 3, 5);
        graph.addEdge(3, 4, 3);

        DijkstraResult result = Dijkstra.findShortestPaths(graph, 0);

        assertEquals(List.of(0L, 3L, 1L, 4L, 7L), result.getDistances());
        assertEquals(0, result.getSource());
    }

    @Test
    void reconstructsShortestPathAndExposesPredecessors() {
        WeightedGraph graph = new WeightedGraph(4);
        graph.addEdge(0, 1, 2);
        graph.addEdge(1, 2, 2);
        graph.addEdge(0, 2, 9);
        graph.addEdge(2, 3, 1);

        DijkstraResult result = Dijkstra.findShortestPaths(graph, 0);

        assertEquals(List.of(0, 1, 2, 3), result.getPathTo(3));
        assertEquals(OptionalInt.of(2), result.getPredecessorOf(3));
        assertEquals(OptionalInt.empty(), result.getPredecessorOf(0));
        assertEquals(List.of(0), result.getPathTo(0));
    }

    @Test
    void marksUnreachableVerticesWithLongMaxValueAndEmptyPaths() {
        WeightedGraph graph = new WeightedGraph(3);
        graph.addEdge(0, 1, 5);

        DijkstraResult result = Dijkstra.findShortestPaths(graph, 0);

        assertEquals(Long.MAX_VALUE, result.getDistanceTo(2));
        assertEquals(OptionalInt.empty(), result.getPredecessorOf(2));
        assertEquals(List.of(), result.getPathTo(2));
    }

    @Test
    void handlesSingleVertexGraph() {
        DijkstraResult result = Dijkstra.findShortestPaths(new WeightedGraph(1), 0);

        assertEquals(0, result.getDistanceTo(0));
        assertEquals(List.of(0), result.getPathTo(0));
        assertEquals(OptionalInt.empty(), result.getPredecessorOf(0));
    }

    @Test
    void selectsTheCheapestOfMultipleRoutesToADestination() {
        WeightedGraph graph = new WeightedGraph(4);
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 3, 10);
        graph.addEdge(0, 2, 4);
        graph.addEdge(2, 3, 2);

        DijkstraResult result = Dijkstra.findShortestPaths(graph, 0);

        assertEquals(6, result.getDistanceTo(3));
        assertEquals(List.of(0, 2, 3), result.getPathTo(3));
    }

    @Test
    void supportsZeroWeightEdges() {
        WeightedGraph graph = new WeightedGraph(3);
        graph.addEdge(0, 1, 0);
        graph.addEdge(1, 2, 0);
        graph.addEdge(0, 2, 2);

        DijkstraResult result = Dijkstra.findShortestPaths(graph, 0);

        assertEquals(0, result.getDistanceTo(1));
        assertEquals(0, result.getDistanceTo(2));
        assertEquals(List.of(0, 1, 2), result.getPathTo(2));
    }

    @Test
    void rejectsInvalidSourceIndices() {
        WeightedGraph graph = new WeightedGraph(2);

        assertThrows(IndexOutOfBoundsException.class,
                () -> Dijkstra.findShortestPaths(graph, -1));
        assertThrows(IndexOutOfBoundsException.class,
                () -> Dijkstra.findShortestPaths(graph, 2));
        assertThrows(IndexOutOfBoundsException.class,
                () -> Dijkstra.findShortestPaths(new WeightedGraph(0), 0));
    }

    @Test
    void accumulatesLargeWeightsWithoutIntegerOverflow() {
        WeightedGraph graph = new WeightedGraph(3);
        graph.addEdge(0, 1, Integer.MAX_VALUE);
        graph.addEdge(1, 2, Integer.MAX_VALUE);

        DijkstraResult result = Dijkstra.findShortestPaths(graph, 0);

        assertEquals(2L * Integer.MAX_VALUE, result.getDistanceTo(2));
        assertEquals(List.of(0, 1, 2), result.getPathTo(2));
    }

    @Test
    void rejectsNegativeWeightsInGraphRepresentation() {
        WeightedGraph graph = new WeightedGraph(2);

        assertThrows(IllegalArgumentException.class, () -> graph.addEdge(0, 1, -1));
    }

    @Test
    void validatesResultVertexAccess() {
        DijkstraResult result = Dijkstra.findShortestPaths(new WeightedGraph(1), 0);

        assertThrows(IndexOutOfBoundsException.class, () -> result.getDistanceTo(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> result.getPredecessorOf(1));
        assertThrows(IndexOutOfBoundsException.class, () -> result.getPathTo(1));
    }
}
