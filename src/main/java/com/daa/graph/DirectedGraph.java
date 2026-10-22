package com.daa.graph;

import java.util.ArrayList;
import java.util.List;

/**
 * A directed graph with a fixed number of vertices, indexed from 0.
 *
 * <p>Parallel edges and self-loops are allowed. Each call to
 * {@link #addEdge(int, int)} adds one directed edge from source to destination;
 * parallel edges are kept as separate adjacency entries.</p>
 */
public final class DirectedGraph {
    private final List<List<Integer>> adjacencyList;
    private int edgeCount;

    public DirectedGraph(int vertexCount) {
        if (vertexCount < 0) {
            throw new IllegalArgumentException("Vertex count cannot be negative: " + vertexCount);
        }

        adjacencyList = new ArrayList<>(vertexCount);
        for (int vertex = 0; vertex < vertexCount; vertex++) {
            adjacencyList.add(new ArrayList<>());
        }
    }

    public int getVertexCount() {
        return adjacencyList.size();
    }

    public int getEdgeCount() {
        return edgeCount;
    }

    /**
     * Adds one directed edge from {@code source} to {@code destination}.
     */
    public void addEdge(int source, int destination) {
        validateVertex(source);
        validateVertex(destination);
        adjacencyList.get(source).add(destination);
        edgeCount++;
    }

    /**
     * Returns an immutable adjacency-list snapshot.
     */
    public List<List<Integer>> getAdjacencyList() {
        List<List<Integer>> snapshot = new ArrayList<>(adjacencyList.size());
        for (List<Integer> adjacentVertices : adjacencyList) {
            snapshot.add(List.copyOf(adjacentVertices));
        }
        return List.copyOf(snapshot);
    }

    /**
     * Returns the immutable list of vertices reached by outgoing edges from
     * {@code vertex}.
     */
    public List<Integer> getAdjacentVertices(int vertex) {
        validateVertex(vertex);
        return List.copyOf(adjacencyList.get(vertex));
    }

    private void validateVertex(int vertex) {
        if (vertex < 0 || vertex >= adjacencyList.size()) {
            throw new IndexOutOfBoundsException(
                    "Vertex index " + vertex + " is outside [0, " + adjacencyList.size() + ')');
        }
    }
}
