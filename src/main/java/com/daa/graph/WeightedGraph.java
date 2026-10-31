package com.daa.graph;

import java.util.ArrayList;
import java.util.List;

// Parallel edges are allowed; self-loops appear once in adjacency lists.
public final class WeightedGraph {
    private final int vertexCount;
    private final List<Edge> edges;
    private final List<List<Edge>> adjacencyList;

    public WeightedGraph(int vertexCount) {
        if (vertexCount < 0) {
            throw new IllegalArgumentException("Vertex count cannot be negative: " + vertexCount);
        }

        this.vertexCount = vertexCount;
        this.edges = new ArrayList<>();
        this.adjacencyList = new ArrayList<>(vertexCount);
        for (int vertex = 0; vertex < vertexCount; vertex++) {
            adjacencyList.add(new ArrayList<>());
        }
    }

    public int getVertexCount() {
        return vertexCount;
    }

    public int getEdgeCount() {
        return edges.size();
    }

    public Edge addEdge(int source, int destination, int weight) {
        validateVertex(source);
        validateVertex(destination);
        if (weight < 0) {
            throw new IllegalArgumentException("Edge weight cannot be negative: " + weight);
        }

        Edge edge = new Edge(source, destination, weight);
        edges.add(edge);
        adjacencyList.get(source).add(edge);
        if (source != destination) {
            adjacencyList.get(destination).add(new Edge(destination, source, weight));
        }
        return edge;
    }

    public List<Edge> getEdges() {
        return List.copyOf(edges);
    }

    public List<List<Edge>> getAdjacencyList() {
        List<List<Edge>> snapshot = new ArrayList<>(vertexCount);
        for (List<Edge> adjacentEdges : adjacencyList) {
            snapshot.add(List.copyOf(adjacentEdges));
        }
        return List.copyOf(snapshot);
    }

    public List<Edge> getAdjacentEdges(int vertex) {
        validateVertex(vertex);
        return List.copyOf(adjacencyList.get(vertex));
    }

    private void validateVertex(int vertex) {
        if (vertex < 0 || vertex >= vertexCount) {
            throw new IndexOutOfBoundsException(
                    "Vertex index " + vertex + " is outside [0, " + vertexCount + ')');
        }
    }
}
