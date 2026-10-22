package com.daa.connectivity;

import com.daa.graph.DirectedGraph;
import com.daa.metrics.OperationCounts;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;

/**
 * Topological ordering using Kahn's indegree-based algorithm.
 *
 * <p>Vertices that become ready together are processed in queue insertion
 * order, so the returned valid ordering is deterministic for a given graph.</p>
 */
public final class TopologicalSort {
    private TopologicalSort() {
    }

    /**
     * Returns a complete topological ordering for a DAG, or an explicit
     * non-acyclic result when not all vertices can be ordered.
     *
     * <p>Runs in O(V + E) time and O(V) additional space.</p>
     */
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

    /**
     * Instrumented variant. {@code edgesExamined} counts adjacency entries
     * inspected during indegree construction and again during Kahn processing;
     * {@code verticesProcessed} counts vertices removed from the ready queue.
     */
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
                // Parallel edges each contribute one indegree and one decrement.
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
