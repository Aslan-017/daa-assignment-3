package com.daa.experiments;

import com.daa.connectivity.TopologicalSort;
import com.daa.graph.DirectedGraph;
import com.daa.graph.Edge;
import com.daa.graph.WeightedGraph;
import com.daa.metrics.OperationCounts;
import com.daa.mst.KruskalMst;
import com.daa.shortestpath.Dijkstra;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GraphGeneratorTest {
    @Test
    void exposesRequiredMstGraphSizes() {
        assertEquals(
                List.of(
                        new GraphGenerator.GraphSize(10, 30),
                        new GraphGenerator.GraphSize(100, 500),
                        new GraphGenerator.GraphSize(1_000, 5_000)),
                GraphGenerator.standardMstSizes());
    }

    @Test
    void generatesConnectedSimpleGraphsWithExactSizesAndBoundedWeights() {
        GraphGenerator generator = new GraphGenerator(17);

        for (GraphGenerator.GraphSize size : GraphGenerator.standardMstSizes()) {
            WeightedGraph graph = generator.generateConnectedWeightedGraph(
                    size.vertexCount(), size.edgeCount(), 100);

            assertEquals(size.vertexCount(), graph.getVertexCount());
            assertEquals(size.edgeCount(), graph.getEdgeCount());
            assertTrue(KruskalMst.findMinimumSpanningTree(graph).isSpanningTree());

            Set<String> uniqueEdges = new HashSet<>();
            for (Edge edge : graph.getEdges()) {
                int first = Math.min(edge.getSource(), edge.getDestination());
                int second = Math.max(edge.getSource(), edge.getDestination());
                assertTrue(uniqueEdges.add(first + ":" + second));
                assertTrue(edge.getWeight() >= 0 && edge.getWeight() < 100);
            }
        }
    }

    @Test
    void producesIdenticalGraphsForIdenticalSeeds() {
        WeightedGraph first = new GraphGenerator(42)
                .generateConnectedWeightedGraph(20, 60, 50);
        WeightedGraph second = new GraphGenerator(42)
                .generateConnectedWeightedGraph(20, 60, 50);

        assertEquals(first.getEdges(), second.getEdges());
    }

    @Test
    void validatesUndirectedSimpleGraphBounds() {
        GraphGenerator generator = new GraphGenerator(3);

        assertThrows(IllegalArgumentException.class,
                () -> generator.generateConnectedWeightedGraph(5, 3, 10));
        assertThrows(IllegalArgumentException.class,
                () -> generator.generateConnectedWeightedGraph(0, 0, 10));
        assertThrows(IllegalArgumentException.class,
                () -> generator.generateWeightedGraph(3, 4, 10));
    }

    @Test
    void generatedDagHasExactEdgeCountAndCanBeTopologicallySorted() {
        DirectedGraph graph = new GraphGenerator(99).generateDag(12, 30);

        assertEquals(30, graph.getEdgeCount());
        assertTrue(TopologicalSort.sort(graph).isAcyclic());
        assertNoDuplicateDirectedEdges(graph);
    }

    @Test
    void generatedCyclicGraphContainsCycleAndHasExactEdgeCount() {
        DirectedGraph graph = new GraphGenerator(99).generateCyclicDirectedGraph(12, 30);

        assertEquals(30, graph.getEdgeCount());
        assertFalse(TopologicalSort.sort(graph).isAcyclic());
        assertNoDuplicateDirectedEdges(graph);
    }

    @Test
    void rejectsCyclicDirectedGraphWithTooFewEdges() {
        assertThrows(IllegalArgumentException.class,
                () -> new GraphGenerator(1).generateCyclicDirectedGraph(4, 3));
    }

    @Test
    void generatedPartialCycleProcessesAcyclicRegionThenStallsAtCycle() {
        GraphGenerator generator = new GraphGenerator(99);

        for (GraphGenerator.GraphSize size : GraphGenerator.standardMstSizes()) {
            DirectedGraph graph = generator.generatePartialCycleDirectedGraph(
                    size.vertexCount(), size.edgeCount());
            OperationCounts counters = new OperationCounts();

            assertEquals(size.vertexCount(), graph.getVertexCount());
            assertEquals(size.edgeCount(), graph.getEdgeCount());
            assertFalse(TopologicalSort.sort(graph, counters).isAcyclic());
            long processedVertices = counters.asMap().get("verticesProcessed");
            assertTrue(processedVertices > 0);
            assertTrue(processedVertices < graph.getVertexCount());
            assertNoDuplicateDirectedEdges(graph);
        }
    }

    @Test
    void partialCycleGenerationIsReproducibleForFixedSeed() {
        DirectedGraph first = new GraphGenerator(42).generatePartialCycleDirectedGraph(20, 70);
        DirectedGraph second = new GraphGenerator(42).generatePartialCycleDirectedGraph(20, 70);

        assertEquals(first.getVertexCount(), second.getVertexCount());
        for (int vertex = 0; vertex < first.getVertexCount(); vertex++) {
            assertEquals(first.getAdjacentVertices(vertex), second.getAdjacentVertices(vertex));
        }
    }

    @Test
    void generatesSingletonCyclicGraphUsingSelfLoop() {
        DirectedGraph graph = new GraphGenerator(99).generateCyclicDirectedGraph(1, 1);

        assertEquals(1, graph.getEdgeCount());
        assertEquals(List.of(0), graph.getAdjacentVertices(0));
        assertFalse(TopologicalSort.sort(graph).isAcyclic());
    }

    @Test
    void generatesExplicitUnreachableVertexForDijkstra() {
        WeightedGraph graph = new GraphGenerator(5)
                .generateWeightedGraphWithUnreachableVertex(8, 12, 10);

        assertEquals(12, graph.getEdgeCount());
        assertTrue(graph.getAdjacentEdges(7).isEmpty());
        assertEquals(Long.MAX_VALUE, Dijkstra.findShortestPaths(graph, 0).getDistanceTo(7));
    }

    private void assertNoDuplicateDirectedEdges(DirectedGraph graph) {
        Set<String> seen = new HashSet<>();
        for (int source = 0; source < graph.getVertexCount(); source++) {
            for (int destination : graph.getAdjacentVertices(source)) {
                assertTrue(seen.add(source + ":" + destination));
            }
        }
    }
}
