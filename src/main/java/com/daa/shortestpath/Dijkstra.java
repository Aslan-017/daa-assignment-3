package com.daa.shortestpath;

import com.daa.graph.Edge;
import com.daa.graph.WeightedGraph;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.PriorityQueue;

/**
 * Dijkstra's single-source shortest-path algorithm for non-negative weighted
 * graphs.
 */
public final class Dijkstra {
    private Dijkstra() {
    }

    /**
     * Finds shortest paths from {@code source} to every vertex.
     *
     * <p>With a binary-heap priority queue and duplicate entries for improved
     * distances, this implementation runs in O((V + E) log(E + 1)) time and
     * uses O(V + E) additional space in the worst case.</p>
     *
     * @throws IndexOutOfBoundsException if {@code source} is not a graph vertex
     * @throws IllegalArgumentException if any graph edge has a negative weight
     */
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

                // Keep Long.MAX_VALUE reserved for unreachable vertices and avoid overflow.
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

    private static final class QueueEntry {
        private final int vertex;
        private final long distance;

        private QueueEntry(int vertex, long distance) {
            this.vertex = vertex;
            this.distance = distance;
        }
    }
}
