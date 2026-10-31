package com.daa.shortestpath;

import com.daa.graph.Edge;
import com.daa.graph.WeightedGraph;
import com.daa.metrics.OperationCounts;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.PriorityQueue;

/**
 * With duplicate entries in a binary-heap priority queue, runtime is
 * O((V + E) log(E + 1)) and additional space is O(V + E).
 */
public final class Dijkstra {
    private Dijkstra() {
    }

    public static DijkstraResult findShortestPaths(WeightedGraph graph, int source) {
        Objects.requireNonNull(graph, "Graph cannot be null");

        int vertexCount = graph.getVertexCount();
        if (source < 0 || source >= vertexCount) {
            throw new IndexOutOfBoundsException(
                    "Source vertex " + source + " is outside [0, " + vertexCount + ')');
        }

        long[] distances = new long[vertexCount];
        Arrays.fill(distances, Long.MAX_VALUE);
        int[] predecessors = new int[vertexCount];
        Arrays.fill(predecessors, -1);
        distances[source] = 0;

        PriorityQueue<QueueEntry> pending = new PriorityQueue<>(
                Comparator.comparingLong(entry -> entry.distance));
        pending.add(new QueueEntry(source, 0));

        while (!pending.isEmpty()) {
            QueueEntry current = pending.remove();
            if (current.distance != distances[current.vertex]) {
                continue;
            }

            for (Edge edge : graph.getAdjacentEdges(current.vertex)) {
                int weight = edge.getWeight();
                if (weight < 0) {
                    throw new IllegalArgumentException(
                            "Dijkstra cannot process negative edge weights: " + weight);
                }

                if (current.distance > Long.MAX_VALUE - weight) {
                    continue;
                }
                long candidateDistance = current.distance + weight;
                int neighbor = edge.getDestination();
                if (candidateDistance < distances[neighbor]) {
                    distances[neighbor] = candidateDistance;
                    predecessors[neighbor] = current.vertex;
                    pending.add(new QueueEntry(neighbor, candidateDistance));
                }
            }
        }

        return new DijkstraResult(source, distances, predecessors);
    }

    // Counts every queue insertion/removal, removals with outdated distances, and adjacency
    // entries scanned from current removals.
    public static DijkstraResult findShortestPaths(
            WeightedGraph graph, int source, OperationCounts counters) {
        Objects.requireNonNull(graph, "Graph cannot be null");
        Objects.requireNonNull(counters, "Counters cannot be null");
        counters.enable(
                OperationCounts.Counter.priorityQueueInsertions,
                OperationCounts.Counter.priorityQueueRemovals,
                OperationCounts.Counter.staleEntriesSkipped,
                OperationCounts.Counter.edgesExamined);

        int vertexCount = graph.getVertexCount();
        if (source < 0 || source >= vertexCount) {
            throw new IndexOutOfBoundsException(
                    "Source vertex " + source + " is outside [0, " + vertexCount + ')');
        }

        long[] distances = new long[vertexCount];
        Arrays.fill(distances, Long.MAX_VALUE);
        int[] predecessors = new int[vertexCount];
        Arrays.fill(predecessors, -1);
        distances[source] = 0;

        PriorityQueue<QueueEntry> pending = new PriorityQueue<>(
                Comparator.comparingLong(entry -> entry.distance));
        pending.add(new QueueEntry(source, 0));
        counters.increment(OperationCounts.Counter.priorityQueueInsertions);

        while (!pending.isEmpty()) {
            QueueEntry current = pending.remove();
            counters.increment(OperationCounts.Counter.priorityQueueRemovals);
            if (current.distance != distances[current.vertex]) {
                counters.increment(OperationCounts.Counter.staleEntriesSkipped);
                continue;
            }

            for (Edge edge : graph.getAdjacentEdges(current.vertex)) {
                counters.increment(OperationCounts.Counter.edgesExamined);
                int weight = edge.getWeight();
                if (weight < 0) {
                    throw new IllegalArgumentException(
                            "Dijkstra cannot process negative edge weights: " + weight);
                }

                if (current.distance > Long.MAX_VALUE - weight) {
                    // Keep Long.MAX_VALUE reserved for unreachable vertices and avoid overflow.
                    continue;
                }
                long candidateDistance = current.distance + weight;
                int neighbor = edge.getDestination();
                if (candidateDistance < distances[neighbor]) {
                    distances[neighbor] = candidateDistance;
                    predecessors[neighbor] = current.vertex;
                    pending.add(new QueueEntry(neighbor, candidateDistance));
                    counters.increment(OperationCounts.Counter.priorityQueueInsertions);
                }
            }
        }

        return new DijkstraResult(source, distances, predecessors);
    }

    private static final class QueueEntry {
        private final int vertex;
        private final long distance;

        private QueueEntry(int vertex, long distance) {
            this.vertex = vertex;
            this.distance = distance;
        }
    }
}
