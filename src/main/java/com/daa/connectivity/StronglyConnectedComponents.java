package com.daa.connectivity;

import com.daa.graph.DirectedGraph;
import com.daa.metrics.OperationCounts;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

public final class StronglyConnectedComponents {
    private StronglyConnectedComponents() {
    }

    public static List<List<Integer>> find(DirectedGraph graph) {
        Objects.requireNonNull(graph, "Graph cannot be null");

        int vertexCount = graph.getVertexCount();
        List<List<Integer>> adjacencyList = graph.getAdjacencyList();
        boolean[] visited = new boolean[vertexCount];
        int[] nextNeighborIndex = new int[vertexCount];
        List<Integer> finishingOrder = new ArrayList<>(vertexCount);

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

    // Visits count discoveries in each pass; edge scans count original and transpose adjacency entries.
    public static List<List<Integer>> find(
            DirectedGraph graph, OperationCounts counters) {
        Objects.requireNonNull(graph, "Graph cannot be null");
        Objects.requireNonNull(counters, "Counters cannot be null");
        counters.enable(
                OperationCounts.Counter.verticesVisited,
                OperationCounts.Counter.edgesExamined);

        int vertexCount = graph.getVertexCount();
        List<List<Integer>> adjacencyList = graph.getAdjacencyList();
        boolean[] visited = new boolean[vertexCount];
        int[] nextNeighborIndex = new int[vertexCount];
        List<Integer> finishingOrder = new ArrayList<>(vertexCount);

        for (int start = 0; start < vertexCount; start++) {
            if (visited[start]) {
                continue;
            }

            Deque<Integer> search = new ArrayDeque<>();
            visited[start] = true;
            counters.increment(OperationCounts.Counter.verticesVisited);
            search.push(start);
            while (!search.isEmpty()) {
                int vertex = search.peek();
                List<Integer> neighbors = adjacencyList.get(vertex);
                if (nextNeighborIndex[vertex] < neighbors.size()) {
                    int neighbor = neighbors.get(nextNeighborIndex[vertex]++);
                    counters.increment(OperationCounts.Counter.edgesExamined);
                    if (!visited[neighbor]) {
                        visited[neighbor] = true;
                        counters.increment(OperationCounts.Counter.verticesVisited);
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
            counters.increment(OperationCounts.Counter.verticesVisited);
            search.push(start);
            while (!search.isEmpty()) {
                int vertex = search.pop();
                component.add(vertex);
                for (int neighbor : transpose.get(vertex)) {
                    counters.increment(OperationCounts.Counter.edgesExamined);
                    if (!visited[neighbor]) {
                        visited[neighbor] = true;
                        counters.increment(OperationCounts.Counter.verticesVisited);
                        search.push(neighbor);
                    }
                }
            }
            components.add(List.copyOf(component));
        }

        return List.copyOf(components);
    }
}
