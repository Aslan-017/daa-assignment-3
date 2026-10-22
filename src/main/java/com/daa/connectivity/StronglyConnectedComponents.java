package com.daa.connectivity;

import com.daa.graph.DirectedGraph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

/**
 * Finds strongly connected components with Kosaraju's two-pass algorithm.
 *
 * <p>Runs in O(V + E) time and uses O(V + E) additional space for the
 * transpose graph, traversal state, and result.</p>
 */
public final class StronglyConnectedComponents {
    private StronglyConnectedComponents() {
    }

    /**
     * Returns an immutable list of immutable components. Every vertex appears
     * in exactly one component; component and vertex order are traversal
     * dependent and should not be relied upon.
     */
    public static List<List<Integer>> find(DirectedGraph graph) {
        Objects.requireNonNull(graph, "Graph cannot be null");

        int vertexCount = graph.getVertexCount();
        List<List<Integer>> adjacencyList = graph.getAdjacencyList();
        boolean[] visited = new boolean[vertexCount];
        int[] nextNeighborIndex = new int[vertexCount];
        List<Integer> finishingOrder = new ArrayList<>(vertexCount);

        // Iterative DFS with a per-vertex neighbor cursor records postorder
        // without risking a Java call-stack overflow on long paths.
        for (int start = 0; start < vertexCount; start++) {
            if (visited[start]) {
                continue;
            }

            Deque<Integer> search = new ArrayDeque<>();
            visited[start] = true;
            search.push(start);
            while (!search.isEmpty()) {
                int vertex = search.peek();
                List<Integer> neighbors = adjacencyList.get(vertex);
                if (nextNeighborIndex[vertex] < neighbors.size()) {
                    int neighbor = neighbors.get(nextNeighborIndex[vertex]++);
                    if (!visited[neighbor]) {
                        visited[neighbor] = true;
                        search.push(neighbor);
                    }
                } else {
                    search.pop();
                    finishingOrder.add(vertex);
                }
            }
        }

        List<List<Integer>> transpose = new ArrayList<>(vertexCount);
        for (int vertex = 0; vertex < vertexCount; vertex++) {
            transpose.add(new ArrayList<>());
        }
        for (int source = 0; source < vertexCount; source++) {
            for (int destination : adjacencyList.get(source)) {
                transpose.get(destination).add(source);
            }
        }

        List<List<Integer>> components = new ArrayList<>();
        Arrays.fill(visited, false);
        for (int index = finishingOrder.size() - 1; index >= 0; index--) {
            int start = finishingOrder.get(index);
            if (visited[start]) {
                continue;
            }

            List<Integer> component = new ArrayList<>();
            Deque<Integer> search = new ArrayDeque<>();
            visited[start] = true;
            search.push(start);
            while (!search.isEmpty()) {
                int vertex = search.pop();
                component.add(vertex);
                for (int neighbor : transpose.get(vertex)) {
                    if (!visited[neighbor]) {
                        visited[neighbor] = true;
                        search.push(neighbor);
                    }
                }
            }
            components.add(List.copyOf(component));
        }

        return List.copyOf(components);
    }
}
