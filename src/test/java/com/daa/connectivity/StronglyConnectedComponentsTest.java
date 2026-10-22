package com.daa.connectivity;

import com.daa.graph.DirectedGraph;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StronglyConnectedComponentsTest {
    @Test
    void findsSeveralStronglyConnectedComponents() {
        DirectedGraph graph = new DirectedGraph(8);
        graph.addEdge(0, 1);
        graph.addEdge(1, 2);
        graph.addEdge(2, 0);
        graph.addEdge(2, 3);
        graph.addEdge(3, 4);
        graph.addEdge(4, 5);
        graph.addEdge(5, 3);
        graph.addEdge(6, 5);
        graph.addEdge(6, 7);
        graph.addEdge(7, 6);

        List<List<Integer>> components = StronglyConnectedComponents.find(graph);

        assertEquals(
                Set.of(Set.of(0, 1, 2), Set.of(3, 4, 5), Set.of(6, 7)),
                asComponentSet(components));
        assertEveryVertexAppearsExactlyOnce(components, graph.getVertexCount());
    }

    @Test
    void treatsIsolatedVerticesAndSelfLoopsAsComponents() {
        DirectedGraph graph = new DirectedGraph(4);
        graph.addEdge(1, 1);
        graph.addEdge(3, 3);

        List<List<Integer>> components = StronglyConnectedComponents.find(graph);

        assertEquals(
                Set.of(Set.of(0), Set.of(1), Set.of(2), Set.of(3)),
                asComponentSet(components));
        assertEveryVertexAppearsExactlyOnce(components, graph.getVertexCount());
    }

    @Test
    void handlesEmptyGraphAndSingleVertex() {
        List<List<Integer>> emptyComponents =
                StronglyConnectedComponents.find(new DirectedGraph(0));
        List<List<Integer>> singleComponents =
                StronglyConnectedComponents.find(new DirectedGraph(1));

        assertTrue(emptyComponents.isEmpty());
        assertEquals(List.of(List.of(0)), singleComponents);
    }

    @Test
    void assignsEveryVertexToExactlyOneComponent() {
        DirectedGraph graph = new DirectedGraph(5);
        graph.addEdge(0, 1);
        graph.addEdge(1, 0);
        graph.addEdge(1, 2);
        graph.addEdge(2, 3);
        graph.addEdge(3, 2);

        List<List<Integer>> components = StronglyConnectedComponents.find(graph);

        assertEveryVertexAppearsExactlyOnce(components, graph.getVertexCount());
    }

    private Set<Set<Integer>> asComponentSet(List<List<Integer>> components) {
        Set<Set<Integer>> componentSet = new HashSet<>();
        for (List<Integer> component : components) {
            componentSet.add(Set.copyOf(component));
        }
        return componentSet;
    }

    private void assertEveryVertexAppearsExactlyOnce(
            List<List<Integer>> components, int vertexCount) {
        boolean[] seen = new boolean[vertexCount];
        int totalVertices = 0;
        for (List<Integer> component : components) {
            assertTrue(!component.isEmpty(), "Components must not be empty");
            for (int vertex : component) {
                assertTrue(vertex >= 0 && vertex < vertexCount, "Vertex index must be valid");
                assertTrue(!seen[vertex], "A vertex must appear in exactly one component");
                seen[vertex] = true;
                totalVertices++;
            }
        }
        assertEquals(vertexCount, totalVertices);
    }
}
