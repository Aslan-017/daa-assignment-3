package com.daa.shortestpath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.OptionalInt;

/**
 * The shortest-path distances and predecessor tree produced by Dijkstra's
 * algorithm. An unreachable vertex has distance {@link Long#MAX_VALUE} and
 * no predecessor.
 */
public final class DijkstraResult {
    private final int source;
    private final long[] distances;
    private final int[] predecessors;

    DijkstraResult(int source, long[] distances, int[] predecessors) {
        this.source = source;
        this.distances = distances.clone();
        this.predecessors = predecessors.clone();
    }

    public int getSource() {
        return source;
    }

    /**
     * Returns the distance from the source to {@code vertex}, or
     * {@link Long#MAX_VALUE} if the vertex is unreachable.
     */
    public long getDistanceTo(int vertex) {
        validateVertex(vertex);
        return distances[vertex];
    }

    /**
     * Returns the preceding vertex on a shortest path, if one exists.
     */
    public OptionalInt getPredecessorOf(int vertex) {
        validateVertex(vertex);
        if (predecessors[vertex] < 0) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(predecessors[vertex]);
    }

    /**
     * Returns a shortest path from the source to {@code vertex}. The result
     * includes both endpoints, is a singleton for the source itself, and is
     * empty when the vertex is unreachable.
     */
    public List<Integer> getPathTo(int vertex) {
        validateVertex(vertex);
        if (distances[vertex] == Long.MAX_VALUE) {
            return List.of();
        }

        List<Integer> reversedPath = new ArrayList<>();
        int current = vertex;
        while (current != source) {
            reversedPath.add(current);
            current = predecessors[current];
            if (current < 0) {
                throw new IllegalStateException("Reachable vertex has no predecessor path to source");
            }
        }
        reversedPath.add(source);
        Collections.reverse(reversedPath);
        return List.copyOf(reversedPath);
    }

    /**
     * Returns an immutable snapshot of all distances, indexed by vertex.
     */
    public List<Long> getDistances() {
        List<Long> snapshot = new ArrayList<>(distances.length);
        for (long distance : distances) {
            snapshot.add(distance);
        }
        return List.copyOf(snapshot);
    }

    private void validateVertex(int vertex) {
        if (vertex < 0 || vertex >= distances.length) {
            throw new IndexOutOfBoundsException(
                    "Vertex index " + vertex + " is outside [0, " + distances.length + ')');
        }
    }
}
