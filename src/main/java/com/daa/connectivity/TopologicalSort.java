package com.daa.connectivity;

import com.daa.graph.DirectedGraph;
import com.daa.metrics.OperationCounts;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;

public final class TopologicalSort {
    private TopologicalSort() {
    }

    public static TopologicalSortResult sort(DirectedGraph graph) {
        Objects.requireNonNull(graph, "Graph cannot be null");

        int vertexCount = graph.getVertexCount();
        int[] indegree = new int[vertexCount];
        for (int source = 0; source < vertexCount; source++) {
            for (int destination : graph.getAdjacentVertices(source)) {
                indegree[destination]++;
            }
        }

        Queue<Integer> readyVertices = new ArrayDeque<>();
        for (int vertex = 0; vertex < vertexCount; vertex++) {
            if (indegree[vertex] == 0) {
                readyVertices.add(vertex);
            }
        }

        List<Integer> ordering = new ArrayList<>(vertexCount);
        while (!readyVertices.isEmpty()) {
            int source = readyVertices.remove();
            ordering.add(source);
            for (int destination : graph.getAdjacentVertices(source)) {
                indegree[destination]--;
                if (indegree[destination] == 0) {
                    readyVertices.add(destination);
                }
            }
        }

        boolean acyclic = ordering.size() == vertexCount;
        return new TopologicalSortResult(acyclic ? ordering : List.of(), acyclic);
    }

    // Edge scans include indegree construction and queue processing; vertices count queue removals.
    public static TopologicalSortResult sort(
            DirectedGraph graph, OperationCounts counters) {
        Objects.requireNonNull(graph, "Graph cannot be null");
        Objects.requireNonNull(counters, "Counters cannot be null");
        counters.enable(
                OperationCounts.Counter.verticesProcessed,
                OperationCounts.Counter.edgesExamined);

        int vertexCount = graph.getVertexCount();
        int[] indegree = new int[vertexCount];
        for (int source = 0; source < vertexCount; source++) {
            for (int destination : graph.getAdjacentVertices(source)) {
                counters.increment(OperationCounts.Counter.edgesExamined);
                indegree[destination]++;
            }
        }

        Queue<Integer> readyVertices = new ArrayDeque<>();
        for (int vertex = 0; vertex < vertexCount; vertex++) {
            if (indegree[vertex] == 0) {
                readyVertices.add(vertex);
            }
        }

        List<Integer> ordering = new ArrayList<>(vertexCount);
        while (!readyVertices.isEmpty()) {
            int source = readyVertices.remove();
            counters.increment(OperationCounts.Counter.verticesProcessed);
            ordering.add(source);
            for (int destination : graph.getAdjacentVertices(source)) {
                counters.increment(OperationCounts.Counter.edgesExamined);
                indegree[destination]--;
                if (indegree[destination] == 0) {
                    readyVertices.add(destination);
                }
            }
        }

        boolean acyclic = ordering.size() == vertexCount;
        return new TopologicalSortResult(acyclic ? ordering : List.of(), acyclic);
    }
}
