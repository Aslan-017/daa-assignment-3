package com.daa.shortestpath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.OptionalInt;

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

    public long getDistanceTo(int vertex) {
        validateVertex(vertex);
        return distances[vertex];
    }

    public OptionalInt getPredecessorOf(int vertex) {
        validateVertex(vertex);
        if (predecessors[vertex] < 0) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(predecessors[vertex]);
    }

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
