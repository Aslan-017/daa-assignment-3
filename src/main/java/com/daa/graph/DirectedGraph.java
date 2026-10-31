package com.daa.graph;

import java.util.ArrayList;
import java.util.List;

// Parallel edges and self-loops are stored as separate adjacency entries.
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

    public void addEdge(int source, int destination) {
        validateVertex(source);
        validateVertex(destination);
        adjacencyList.get(source).add(destination);
        edgeCount++;
    }

    public List<List<Integer>> getAdjacencyList() {
        List<List<Integer>> snapshot = new ArrayList<>(adjacencyList.size());
        for (List<Integer> adjacentVertices : adjacencyList) {
            snapshot.add(List.copyOf(adjacentVertices));
        }
        return List.copyOf(snapshot);
    }

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
